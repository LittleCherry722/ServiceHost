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
  val (id: ProcessInstanceID, processName: String, startSubjectsString: SubjectID, graphJson: String) =
    Await.result(dataBaseAccessFuture, timeout.duration)

  // parse the start-subjects into an Array
  val startSubjects: Array[SubjectID] = parseSubjects(startSubjectsString)
  // parse the graph into the internal structure
  val graph: ProcessGraph = parseGraph(graphJson)

  // TODO wie uebergeben?
  private lazy val contextResolver = ActorLocator.contextResolverActor

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  //  private var messagePool = Set[(ActorRef, SubjectToSubjectMessage)]()

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  private def isTerminated = runningSubjectCounter == 0
  // this map stores all Subject(Container) with their IDs 
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

  // add all start subjects (only if they exist!)
  for (startSubject <- startSubjects if (graph.hasSubject(startSubject))) {

    // Create the subjectContainer
    subjectMap(startSubject) = SubjectContainer(graph.subject(startSubject))
    // the container shall contain a subject -> request creation
    subjectMap(startSubject).requestSubjectCreation(request.userID)
  }

  def receive = {

    case as: AddSubject if (graph.hasSubject(as.subjectID)) => {
      // if subjectProvider of the new subject is not the same as the one that asked for execution
      // try to forward blocked ExecuteActionAnswer
      val subject: Subject = graph.subject(as.subjectID)

      // safe userID that owns the subject
      subjectsUserIDMap += subject.id -> as.userID

      // Create the subject for the subject container in the given map position
      subjectMap.getOrElseUpdate(subject.id, SubjectContainer(subject))
        .createAndAddSubject(as.userID)

      // increase the subjectcounter
      //      subjectCounter += 1
    }

    case message: SubjectStarted => {
      subjectMap(message.subjectID).handleSubjectStarted(message)
      unblockUserID(message.userID)
      tryToReleaseBlocking(message.userID)
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      //      subjectMap -= st.subjectID
      //      subjectsUserIDMap -= st.subjectID // TODO umbauen fuer multisubjecte
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      // log end time in history TODO
      //      subjectCounter -= 1
      logger.debug("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (isTerminated) {
        executionHistory.processEnded = Some(new Date())
      }
    }

    case message: SubjectInternalMessageProcessed => {
      // println("subjectInternalMessageProcessed")
      unblockUserID(subjectsUserIDMap(message.subjectID))
      tryToReleaseBlocking(subjectsUserIDMap(message.subjectID))
    }

    case sm: SubjectToSubjectMessage if (graph.hasSubject(sm.to)) => {
      val to = sm.to
      // block user that owns the subject
      if (!subjectMap.contains(to)) {
        // if the subjectcontainer does not exists, create it
        subjectMap(to) = SubjectContainer(graph.subject(to))
      }
      // Send the message to the container, it will deal with it
      subjectMap(to).send(sm)
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

    //    case message: SubjectMessage => { //TODO raus fuer multisubjecte problematisch?
    //      if (subjectMap.contains(message.subjectID)) {
    //        subjectMap(message.subjectID).forwardToAll(message)
    //      } else {
    //        logger.error("ProcessInstance has message for subject " +
    //          message.subjectID + "but it does not exists")
    //      }
    //    }

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
          ExecuteActionAnswer(req, processID, isTerminated, graphJson, executionHistory, actions))
  }

  /**
   * This method checks if all subjects are parsed and ready to ask for actions
   * etc.
   */
  private def allSubjectsReady(userID: UserID): Boolean = {
    !waitingForContextResolver.contains(userID) && waitingUserMap.getOrElse(userID, 1) == 0
  }

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
      //      context.parent ! ProcessInstanceCreated(request, id, self, graphJson, executionHistory, Array())
      logger.debug("Send ProcessInstance " + id + " created")
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

  /**
   * This class is responsible to hold a subjects, and can represent
   * a single subject or a multisubject
   */
  private case class SubjectContainer(subject: Subject) {
    import scala.collection.mutable.{ Map => MutableMap }

    val multi = subject.multi
    val external = subject.external

    private case class SubjectInfo(
      ref: SubjectRef,
      userID: UserID,
      var running: Boolean = true)

    private val subjects = MutableMap[SubjectSessionID, SubjectInfo]()
    private var nextSubjectSessionID = 0

    // an entry for the messagepool
    private case class MessagePoolEntry(
      var subjectCount: Int,
      orig: ActorRef,
      message: SubjectToSubjectMessage)
    // this pool contains the unsent messages, which will be sent to subjects
    // which are created first
    private var messagePool: Set[MessagePoolEntry] = Set()

    /**
     * Adds a Subject to this multisubject
     */
    def createAndAddSubject(userID: UserID) {
      val subjectSessionID = nextSubjectSessionID
      nextSubjectSessionID += 1

      // handle blocking
      handleBlockingForSubjectCreation(userID)
      // create subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(userID, subjectSessionID, self, subject)))
      // and store it in the map
      subjects += subjectSessionID -> SubjectInfo(subjectRef, userID)

      // if there are messages to deliver to the new subject,
      // forward them to the subject 
      for (entry <- messagePool) {
        handleBlockingForMessageDelivery(subject.id)
        subjectRef.!(entry.message)(entry.orig)
        entry.subjectCount -= 1
      }
      // remove all entry, which has been sent to enough subjects
      messagePool = messagePool.filter(_.subjectCount > 0)

      logger.debug("Processinstance [" + id + "] created Subject " +
        subject.id + "[" + subjectSessionID + "] for user " + userID)

      // inform the subject provider about his new subject
      context.parent !
        SubjectCreated(userID, processID, id, subject.id, subjectSessionID, subjectRef)

      // start the execution of the subject
      subjectRef ! StartSubjectExecution()
    }

    def handleSubjectStarted(message: SubjectStarted) {

      logger.debug("Processinstance [" + id + "] Subject " + subject.id + "[" +
        message.subjectSessionID + "] started")

      subjects(message.subjectSessionID).running = true
    }

    def handleSubjectTerminated(message: SubjectTerminated) {

      logger.debug("Processinstance [" + id + "] Subject " + subject.id + "[" +
        message.subjectSessionID + "] terminated")

      // decrease the subject counter
      runningSubjectCounter -= 1

      subjects(message.subjectSessionID).running = false
    }

    /**
     * Forwards a message to all Subjects of this MultiSubject
     */
    def send(message: SubjectToSubjectMessage) {
      import scala.util.Random.shuffle
      // TODO start for singlesubjects, of no subjects exists, create one

      if (!multi && subjects.isEmpty) {
        // if its not a multisubject and no subject exists, create 1 subject
        // = singlsubject and send the message to it
        requestSubjectCreation(message, 1)
      } else if (message.target.toAll) {
        // multisubject send to all and singlesubject
        // only restart subjects, which are not multisubjects
        sendTo(subjects.map(_._2).toArray, message, !multi)
      } else if (message.target.toVariable) {
        // TODO send messages to the subjects in the variable
        val targetSubjects =
          for ((subjectID, sessionID) <- message.target.varSubjects)
            yield subjects(sessionID)
        sendTo(targetSubjects, message) // TODO create new?
      } else if (message.target.min <= subjects.filter(_._2.running).size) {
        // Send to <= max random subjects
        // create a random subset by shuffling the subjects randomly,
        // the mapping to the important information
        // creating an array
        // and taking the first max elements
        val partialSubjects =
          shuffle(subjects.filter(_._2.running)).map(_._2).toArray.take(message.target.max)

        // send to random subset of the subjects
        sendTo(partialSubjects, message)
      } else if (message.target.createNew) {
        // Create min new subjects and send to them
        if (message.target.min <= subjects.size) {
          val partialSubjects =
            shuffle(subjects).map(_._2).toArray.take(message.target.max)
          sendTo(partialSubjects, message, true)
        } else {
          // send to all existing subjects
          sendTo(subjects.map(_._2).toArray, message, true)
          // create the other subjects
          requestSubjectCreation(message, message.target.min - subjects.size)
        }
      } else {
        logger.error("Cant send messages " + message + " invalid number of subjects")
      }
    }

    def requestSubjectCreation(userID: UserID, message: Option[SubjectToSubjectMessage] = None, count: Int = 1) {
      // if the subject does not exist create the subject and forward the
      // message afterwards
      // store the message in the message-pool
      if (message.isDefined && count > 0) {
        messagePool += MessagePoolEntry(count, sender, message.get)
      }

      logger.debug("Processinstance [" + id + "] creates Subject " + subject.id)

      for (i <- (1 to count)) {

        // increase the subject counter
        runningSubjectCounter += 1

        // ask the Contextresolver for the userid to answer with an AddSubject
        waitForContextResolver(userID)
        contextResolver !
          RequestUserID(SubjectInformation(subject.id), AddSubject(_, subject.id))
      }
    }

    private def requestSubjectCreation(message: SubjectToSubjectMessage, count: Int) {
      requestSubjectCreation(message.userID, Some(message), count)
    }

    /**
     * Forwards the message to the array of subjects
     */
    private def sendTo(targetSubjects: Array[SubjectInfo],
      message: SubjectToSubjectMessage,
      restartSubject: Boolean = false) {
      for (subjectInfo <- targetSubjects) {
        if (subjectInfo.running) {
          handleBlockingForMessageDelivery(message.to)
          subjectInfo.ref.forward(message)
        } else if (restartSubject) {
          handleBlockingForSubjectCreation(subjectInfo.userID)
          subjectInfo.ref ! StartSubjectExecution()
          handleBlockingForMessageDelivery(message.to)
          subjectInfo.ref.forward(message)
        }
      }
    }
  }
}
