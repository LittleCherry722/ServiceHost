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
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.ProcessInstance
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.persistence._
import akka.event.Logging

// represents the history of the instance
case class History(processName: String,
                   instanceId: ProcessInstanceID,
                   var processStarted: Option[Date] = None, // None if not started yet
                   var processEnded: Option[Date] = None, // None if not started or still running
                   entries: Buffer[history.Entry] = ArrayBuffer[history.Entry]()) // recorded state transitions in the history

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends Actor {
  val logger = Logging(context.system, this)
  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  import ExecutionContext.Implicits.global // TODO this import or something different?
  implicit val timeout = Timeout(10 seconds)

  val processID = request.processID

  // TODO "None" rueckgaben behandeln
  // Get the ProcessGraph from the database and create the database entry
  // for this process instance
  // TODO momentan wird der process auf instanceid 1 gezwungen 
  val dataBaseAccessFuture = for {
    // get the process
    processFuture <- (ActorLocator.persistenceActor ?
      GetProcess(Some(processID))).mapTo[Option[Process]]
    // save this process instance in the persistence
    processInstanceIDFuture <- (ActorLocator.persistenceActor ?
      SaveProcessInstance(ProcessInstance(None, processID, processFuture.get.graphId, "", "")))
      .mapTo[Option[Int]]
    // save this process instance in the persistence
    processInstanceIDFuture <- (ActorLocator.persistenceActor ?
      SaveProcessInstance(ProcessInstance(Some(1), processID, processFuture.get.graphId, "", "")))
      .mapTo[Option[Int]]
    // get the corresponding graph
    graphFuture <- (ActorLocator.persistenceActor ?
      GetGraph(Some(processFuture.get.graphId))).mapTo[Option[Graph]]
  } yield (if (false) processInstanceIDFuture.get else 1, processFuture.get.name, processFuture.get.startSubjects, graphFuture.get.graph)

  // evaluate the Future
  val (id: ProcessInstanceID, processName: String, startSubjectsString: SubjectID, graphJSON: String) =
    Await.result(dataBaseAccessFuture, timeout.duration)

  // parse the start-subjects into an Array
  val startSubjects: Array[SubjectID] = parseSubjects(startSubjectsString)
  // parse the graph into the internal structure
  val graph: ProcessGraph = parseGraph(graphJSON)

  // TODO wie uebergeben?
  private lazy val contextResolver = ActorLocator.contextResolverActor

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  private var messagePool = Set[(ActorRef, SubjectInternalMessage)]()

  // this map stores all Subjects with their IDs 
  private var subjectCounter = 0 // TODO We dont really need counter
  private val subjectMap = collection.mutable.Map[SubjectID, SubjectRef]()

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(processName, id, Some(new Date())) // TODO start time = creation time?
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])

  // add all start subjects
  for (startSubject <- startSubjects) {
    contextResolver !
      RequestUserID(SubjectInformation(startSubject), AddSubject(_, startSubject))
  }

  // inform the process manager that this process instance has been created
  context.parent ! ProcessInstanceCreated(request, id, self)

  def receive = {

    case as: AddSubject => {
      val subject: Subject = getSubject(as.subjectID)

      // TODO was tun
      if (subject == null) {
        logger.error("ProcessInstance " + id + " -- Subject unknown for " + as)
      } else {
        // create the subject
        val subjectRef =
          context.actorOf(Props(new SubjectActor(as.userID, self, subject)))
        // add the subject to the management map
        subjectMap += subject.id -> subjectRef
        subjectCounter += 1

        logger.info("processinstance " + id + " created subject " + subject.id + " for user " + as.userID)

        // if there are messages to deliver to the new subject,
        // forward them to the subject 
        if (!messagePool.isEmpty) {
          for ((orig, sm) <- messagePool if sm.to == subject.id) {
            subjectRef.!(sm)(orig)
          }
          messagePool = messagePool.filterNot(_._2.to == subject.id)
        }

        // inform the subject provider about his new subject
        context.parent !
          SubjectCreated(as.userID, processID, id, subject.id, subjectRef)

        // start the execution of the subject
        subjectRef ! StartSubjectExecution()
      }
    }

    case st: SubjectTerminated => {
      // log end time in history
      subjectCounter -= 1
      logger.info("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (subjectCounter == 0) {
        executionHistory.processEnded = Some(new Date())
        //        context.stop(self) // TODO stop process instance?
      }
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

    case sm: SubjectInternalMessage => {
      if (subjectMap.contains(sm.to)) {
        // if the subject already exist just forward the message
        subjectMap(sm.to).forward(sm)
      } else {
        // if the subject does not exist create the subject and forward the
        // message afterwards
        // store the message in the message-pool
        messagePool += ((sender, sm))
        // ask the Contextresolver for the userid to answer with an AddSubject
        contextResolver !
          RequestUserID(
            SubjectInformation(sm.to),
            AddSubject(_, sm.to))
      }
    }

    case message: SubjectMessage => {
      if (subjectMap.contains(message.subjectID)) {
        subjectMap(message.subjectID).!(message) // TODO mit forward
      } else {
        logger.error("ProcessInstance has message for subject " +
          message.subjectID + "but it does not exists")
      }
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    case answer: AnswerMessage => {
      context.parent.forward(answer)
    }
  }

  private def getSubject(name: String): Subject = {
    // TODO increase performance
    val subject = graph.subjects.find(_.id == name)
    subject match {
      case None => null
      case _ => subject.get
    }
  }
}
