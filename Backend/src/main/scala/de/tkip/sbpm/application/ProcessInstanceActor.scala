package de.tkip.sbpm.application

import java.util.Date
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.Future._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.ProcessInstance
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.persistence._
import akka.event.Logging
import scala.collection.mutable.SortedSet
import scala.collection.mutable.Set
import scala.collection.mutable.LinkedList
import scala.collection.mutable.Map

// represents the history of the instance
case class History(processName: String,
  instanceId: ProcessInstanceID,
  var processStarted: Option[Date] = None, // None if not started yet
  var processEnded: Option[Date] = None, // None if not started or still running
  entries: Buffer[history.Entry] = ArrayBuffer[history.Entry]()) // recorded state transitions in the history

import ExecutionContext.Implicits.global // TODO this import or something different?

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends Actor {
  private val logger = Logging(context.system, this)
  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  implicit val timeout = Timeout(30 seconds)

  private val processID = request.processID

  // TODO "None" rueckgaben behandeln
  // Get the ProcessGraph from the database and create the database entry
  // for this process instance
  private val dataBaseAccessFuture = for {
    // get the process
    processFuture <- (ActorLocator.persistenceActor ?
      GetProcess(Some(processID))).mapTo[Option[Process]]
    // save this process instance in the persistence
    processInstanceIDFuture <- (ActorLocator.persistenceActor ?
      SaveProcessInstance(ProcessInstance(None, processID, processFuture.get.graphId, "", "")))
      .mapTo[Option[Int]]

    // get the corresponding graph
    graphFuture <- (ActorLocator.persistenceActor ?
      GetGraph(Some(processFuture.get.graphId))).mapTo[Option[Graph]]
  } yield (processInstanceIDFuture.get, processFuture.get.name, processFuture.get.startSubjects, graphFuture.get.graph)

  // evaluate the Future
  private val (id: ProcessInstanceID, processName: String, startSubjectsString: SubjectID, graphJson: String) =
    Await.result(dataBaseAccessFuture, timeout.duration)

  // parse the start-subjects into an Array
  private val startSubjects: Array[SubjectID] = parseSubjects(startSubjectsString)
  // parse the graph into the internal structure
  private val graph: ProcessGraph = parseGraph(graphJson)

  private lazy val contextResolver = ActorLocator.contextResolverActor

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  private def isTerminated = runningSubjectCounter == 0
  // this map stores all Subject(Container) with their IDs 
  private val subjectMap = collection.mutable.Map[SubjectID, SubjectContainer]()

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(processName, id, Some(new Date()))
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])

  // this actor handles the blocking for answer to the user
  private val blockingHandlerActor = context.actorOf(Props[BlockingActor])

  override def preStart() {
    // TODO modify to the right version
    for (startSubject <- startSubjects if (graph.hasSubject(startSubject))) {
      // Create the subjectContainer
      subjectMap(startSubject) = createSubjectContainer(graph.subject(startSubject))
      // the container shall contain a subject -> create
      subjectMap(startSubject).createSubject(request.userID)
    }
    // send processinstance created, when the block is closed
    blockingHandlerActor ! SendProcessInstanceCreated(request.userID)
  }

  def receive = {
    case _: SendProcessInstanceCreated => {
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      logger.debug("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (isTerminated) {
        // log end time in history
        executionHistory.processEnded = Some(new Date())
      }
    }

    case sm: SubjectToSubjectMessage if (graph.hasSubject(sm.to)) => {
      val to = sm.to
      // Send the message to the container, it will deal with it
      subjectMap.getOrElseUpdate(to, createSubjectContainer(graph.subject(to))).send(sm)
    }

    // add an entry to the history
    // (should be called by subject actors when a transition occurs)
    case he: history.Entry => {
      executionHistory.entries += he
    }

    // return current process instance history
    case msg: GetHistory => {
      sender ! {
        if (msg.isInstanceOf[Debug]) {
          HistoryAnswer(msg, HistoryTestData.generate(processName, id)(debugMessagePayloadProvider))
        } else {
          HistoryAnswer(msg, executionHistory)
        }
      }
    }

    case message: SubjectMessage if (subjectMap.contains(message.subjectID)) => {
      subjectMap(message.subjectID).send(message)
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    // send forward if no subject has to be created else wait
    case message: ActionExecuted => {
      System.err.println("Executed " + message)
      createExecuteActionAnswer(message.ea)
    }

    case answer: AnswerMessage => {
      context.parent.forward(answer)
    }
  }

  private var sendProcessInstanceCreated = true
  private def trySendProcessInstanceCreated() {
    if (sendProcessInstanceCreated) {
      context.parent !
        AskSubjectsForAvailableActions(
          request.userID,
          id,
          AllSubjects,
          (actions: Array[AvailableAction]) =>
            ProcessInstanceCreated(request, id, self, false, graphJson, executionHistory, actions))
      sendProcessInstanceCreated = false
    }
  }

  private def createExecuteActionAnswer(req: ExecuteAction) {
    context.parent !
      AskSubjectsForAvailableActions(req.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ExecuteActionAnswer(req, processID, isTerminated, graphJson, executionHistory, actions))
  }

  private def createSubjectContainer(subject: Subject): SubjectContainer = {
    new SubjectContainer(
      subject,
      processID,
      id,
      logger,
      blockingHandlerActor,
      () => runningSubjectCounter += 1,
      () => runningSubjectCounter -= 1)
  }
}
