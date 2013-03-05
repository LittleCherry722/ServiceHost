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

// TODO This object just exists for debug reasons
protected object firstrun {
  private var start = true
  def apply() = {
    val temp = start
    start = false
    temp
  }
}

import ExecutionContext.Implicits.global // TODO this import or something different?

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends Actor {
  private val logger = Logging(context.system, this)
  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  implicit val timeout = Timeout(30 seconds)

  val processID = request.processID

  // TODO just for debug reasons, delete later
  val isStart: Boolean = firstrun()

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
    // Just of debug reasons
    processInstanceIDFuture1 <- if (isStart)
      (ActorLocator.persistenceActor ?
        SaveProcessInstance(ProcessInstance(Some(1), processID, processFuture.get.graphId, "", "")))
        .mapTo[Option[Int]]
    else
      (ActorLocator.persistenceActor ?
        SaveProcessInstance(ProcessInstance(processInstanceIDFuture, processID, processFuture.get.graphId, "", "")))
        .mapTo[Option[Int]]
    //      } else {
    //        processInstanceIDFuture
    //      }

    // get the corresponding graph
    graphFuture <- (ActorLocator.persistenceActor ?
      GetGraph(Some(processFuture.get.graphId))).mapTo[Option[Graph]]
  } yield (if (isStart) 1 else processInstanceIDFuture.get, processFuture.get.name, processFuture.get.startSubjects, graphFuture.get.graph)

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
  private var messagePool = Set[(ActorRef, SubjectToSubject)]()

  // this map stores all Subjects with their IDs 
  private var subjectCounter = 0 // TODO We dont really need counter
  private val subjectMap = collection.mutable.Map[SubjectID, SubjectContainer]()

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(processName, id, Some(new Date())) // TODO start time = creation time?
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])

  // variables to help blocking of ActionExecuted messages
  private var subjectsUserIDMap = Map[SubjectID, UserID]()
  private var waitingForContextResolver = ArrayBuffer[UserID]()
  private var waitingUserMap = Map[UserID, Int]()
  private var blockedAnswers = collection.mutable.Map[UserID, ActionExecuted]()

  // add all start subjects
  for (startSubject <- startSubjects) {
    waitForContextResolver(request.userID)
    contextResolver !
      RequestUserID(SubjectInformation(startSubject), AddSubject(_, startSubject))
  }
  // inform the process manager that this process instance has been created
  //  context.parent ! ProcessInstanceCreated(request, id, self, graphJSON, executionHistory, Array())

  //  context.parent !
  //    AskSubjectsForAvailableActions(request.userID,
  //      id,
  //      AllSubjects,
  //      (actions: Array[AvailableAction]) =>
  //        ProcessInstanceCreated(request, id, self, graphJSON, executionHistory, actions))

  def receive = {

    case as: AddSubject => {
      // if subjectProvider of the new subject is not the same as the one that asked for execution
      // try to forward blocked ExecuteActionAnswer

      val subject: Subject = getSubject(as.subjectID)

      // TODO was tun
      if (subject == null) {
        logger.error("ProcessInstance " + id + " -- Subject unknown for " + as)

        waitingForContextResolver = waitingForContextResolver.tail
      } else {
        // safe userID that owns the subject
        subjectsUserIDMap += subject.id -> as.userID

        // handle blocking
        handleBlockingForSubjectCreation(as.userID)

        val container = subjectMap.getOrElse(subject.id, SubjectContainer())
        val subjectRef = container.createAndAddSubject(as.userID, subject)
        // add the subject to the management map
        subjectMap += subject.id -> container
        subjectCounter += 1

        logger.info("processinstance " + id + " created subject " + subject.id + " for user " + as.userID)

        // if there are messages to deliver to the new subject,
        // forward them to the subject 
        if (!messagePool.isEmpty) {
          for ((orig, sm) <- messagePool if sm.to == subject.id) {
            handleBlockingForMessageDelivery(as.subjectID)
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

    case message: SubjectStarted => {
      // println("subjectstarted")
      subjectMap(message.subjectID).hasStarted(message)
      unblockUserID(message.userID)
      tryToReleaseBlocking(message.userID)
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      //      subjectMap -= st.subjectID
      subjectsUserIDMap -= st.subjectID // TODO umbauen fuer multisubjecte
      subjectMap(st.subjectID).hasTerminated(st)

      // log end time in history TODO
      subjectCounter -= 1
      logger.info("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (subjectCounter == 0) {
        executionHistory.processEnded = Some(new Date())
        //        context.stop(self) // TODO stop process instance?
      }
    }

    case message: SubjectInternalMessageProcessed => {
      // println("subjectInternalMessageProcessed")
      unblockUserID(subjectsUserIDMap(message.subjectID))
      tryToReleaseBlocking(subjectsUserIDMap(message.subjectID))
    }

    case sm: SubjectToSubject => {
      // block user that owns the subject
      if (subjectMap.contains(sm.to)) {
        handleBlockingForMessageDelivery(sm.to)
        // if the subject already exist just forward the message
        subjectMap(sm.to).forward(sm)
      } else {
        waitForContextResolver(sm.userID)

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

    case message: SubjectMessage => {
      if (subjectMap.contains(message.subjectID)) {
        subjectMap(message.subjectID).forward(message)
      } else {
        logger.error("ProcessInstance has message for subject " +
          message.subjectID + "but it does not exists")
      }
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    // send forward if no subject has to be created else wait
    case message: ActionExecuted => {
      System.err.println("Executed " + message)
      if (allSubjectsReady(message.ea.userID)) {
        createExecuteActionAnswer(message.ea)
      } else {
        // println("store message")
        blockedAnswers += message.ea.userID -> message
      }
    }

    case answer: AnswerMessage => {
      context.parent.forward(answer)
    }
  }

  private def createExecuteActionAnswer(req: ExecuteAction) {
    context.parent !
      AskSubjectsForAvailableActions(req.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ExecuteActionAnswer(req, processID, graphJSON, executionHistory, actions))
  }

  /**
   * This method checks if all subjects are parsed and ready to ask for actions
   * etc.
   */
  private def allSubjectsReady(userID: UserID): Boolean = {
    !waitingForContextResolver.contains(userID) && waitingUserMap.getOrElse(userID, 1) == 0
  }

  private def getSubject(id: SubjectID): Subject = graph.subject(id)

  /**
   * adds the userID to the waiting list for answers of the contextResolver
   */
  private def waitForContextResolver(userID: UserID) {
    // set userID on waiting list for answer of contextresolver
    waitingForContextResolver += userID
  }

  /**
   * increases the number of tasks that are blocking the given userID from sending ExecuteActionAnswers by one
   */
  private def blockUserID(userID: UserID) {
    waitingUserMap += userID -> (waitingUserMap.getOrElse(userID, 0) + 1)
    // println("blockuser: " + waitingUserMap.mkString(","))
  }

  /**
   * decrease the number of tasks that are blocking the given userID from sending ExecuteActionAnswers by one
   */
  private def unblockUserID(userID: UserID) {
    val numberOfTasks = (waitingUserMap.getOrElse(userID, 1) - 1)
    waitingUserMap += userID -> (if (numberOfTasks < 0) 0 else numberOfTasks)
    // println("after unblocked: " + waitingUserMap.mkString(","))
  }

  /**
   * handle contextResolverAnswer to ensure blocking of ExecuteActionAnswers until all subjects (owned by the
   * subjectProvider that created the ExecuteAction request) have been created and started
   */
  private def handleBlockingForSubjectCreation(userID: UserID) {
    if (waitingForContextResolver.size == 0) {
      return
    }
    // block user twice. once for subject creation and once for message delivery  
    blockUserID(userID)
    //    blockUserID(userID)

    // println("contextResolver: " + waitingForContextResolver.mkString(","))

    tryToReleaseBlocking(waitingForContextResolver.head)

    // delete userID from waiting list for answers of the contextresolver
    waitingForContextResolver = waitingForContextResolver.tail

    // println("contextResolver: " + waitingForContextResolver.mkString(","))
  }

  private def handleBlockingForMessageDelivery(to: SubjectID) {
    blockUserID(subjectsUserIDMap(to))
    // println("blockingForDelivery: " + waitingUserMap.mkString(","))
  }

  /**
   * handles SubjectStartedMessages and checks if no other task is blocking
   * if thats the case -> forward message else wait
   */
  private def tryToReleaseBlocking(userID: UserID) {
    // if the given userID has no tasks that are blocking it -> forward message if one exists
    if (allSubjectsReady(userID) && blockedAnswers.contains(userID)) {
      createExecuteActionAnswer(blockedAnswers(userID).ea)
      // println("forward: " + blockedAnswers.mkString(","))
      blockedAnswers -= userID
    }
  }

  private var sendProcessInstanceCreated = true
  private def trySendProcessInstanceCreated() {
    if (sendProcessInstanceCreated && allSubjectsReady(request.userID)) {
      //      context.parent ! ProcessInstanceCreated(request, id, self, graphJSON, executionHistory, Array())
      context.parent !
        AskSubjectsForAvailableActions(
          request.userID,
          id,
          AllSubjects,
          (actions: Array[AvailableAction]) =>
            ProcessInstanceCreated(request, id, self, graphJSON, executionHistory, actions))
      sendProcessInstanceCreated = false
    }
  }

  /**
   * This class is responsible to hold a subjects, and can represent
   * a single subject or a multisubject
   */
  private case class SubjectContainer(val multi: Boolean = false) {
    import scala.collection.mutable.{ Map => MutableMap }

    private case class SubjectInfo(
      ref: SubjectRef,
      userID: UserID,
      var running: Boolean = true)

    private val subjects = MutableMap[SubjectSessionID, SubjectInfo]()
    private var nextSubjectSessionID = 0
    //    subjects += subject

    /**
     * Adds a Subject to this multisubject
     */
    def createAndAddSubject(userID: UserID, subject: Subject) = {
      val subjectSessionID = nextSubjectSessionID
      val subjectRef =
        context.actorOf(Props(new SubjectActor(userID, subjectSessionID, self, subject)))
      subjects += subjectSessionID -> SubjectInfo(subjectRef, userID)

      nextSubjectSessionID += 1
      subjectRef
    }

    def hasStarted(message: SubjectStarted) {
      subjects(message.subjectSessionID).running = true
    }

    def hasTerminated(message: SubjectTerminated) {
      subjects(message.subjectSessionID).running = false
    }

    /**
     * Forwards a message to this Subject
     */
    def forward(message: Any) {
      // TODO wie bei multisubjecten?
      //      subject.forward(message)
      forwardToAll(message)
    }

    /**
     * Forwards a message to all Subjects of this MultiSubject
     */
    def forwardToAll(message: Any) {
      for ((k, subjectInfo) <- subjects) {
        if (subjectInfo.running) {
          subjectInfo.ref.forward(message)
        } else {
          subjectInfo.ref ! StartSubjectExecution()
          handleBlockingForSubjectCreation(subjectInfo.userID)
          subjectInfo.ref.forward(message)
        }
      }
    }
  }
}
