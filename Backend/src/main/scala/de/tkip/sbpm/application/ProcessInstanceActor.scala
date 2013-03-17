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

  val processID = request.processID

  // TODO "None" rueckgaben behandeln
  // Get the ProcessGraph from the database and create the database entry
  // for this process instance
  val dataBaseAccessFuture = for {
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
  val (id: ProcessInstanceID, processName: String, startSubjectsString: SubjectID, graphJson: String) =
    Await.result(dataBaseAccessFuture, timeout.duration)

  // parse the start-subjects into an Array
  val startSubjects: Array[SubjectID] = parseSubjects(startSubjectsString)
  // parse the graph into the internal structure
  val graph: ProcessGraph = parseGraph(graphJson)

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

  private val blockingHandler = new BlockingHandler(createExecuteActionAnswer _)

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

      // Create the subject for the subject container in the given map position
      subjectMap.getOrElseUpdate(subject.id, SubjectContainer(subject))
        .createAndAddSubject(as.userID)
    }

    case message: SubjectStarted => {
      subjectMap(message.subjectID).handleSubjectStarted(message)
      handleSubjectStarted(message.userID)
    }

    case st: SubjectTerminated => {
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      logger.debug("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (isTerminated) {
        // log end time in history
        executionHistory.processEnded = Some(new Date())
      }
    }

    case message: SubjectInternalMessageProcessed => {
      // println("subjectInternalMessageProcessed")
      handleSubjectInternalMessageProcessed(message.userID)
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
        storeActionExecuted(message)
      }
    }

    case answer: AnswerMessage => {
      context.parent.forward(answer)
    }
  }

  
  private def allSubjectsReady = blockingHandler.allSubjectsReady _
  private def handleSubjectStarted(userID: UserID) = {
      blockingHandler.handleUnblocking(userID)
      trySendProcessInstanceCreated()
   }
  private def handleSubjectInternalMessageProcessed = blockingHandler.handleUnblocking _
  private def handleBlockingForSubjectCreation = blockingHandler.handleBlockingForSubjectCreation _
  private def handleBlockingForMessageDelivery = blockingHandler.handleBlockingForMessageDelivery _
  private def waitForContextResolver = blockingHandler.waitForContextResolver _
  private def storeActionExecuted = blockingHandler.storeActionExecuted _
  
  private def createExecuteActionAnswer(req: ExecuteAction) {
    context.parent !
      AskSubjectsForAvailableActions(req.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ExecuteActionAnswer(req, processID, isTerminated, graphJson, executionHistory, actions))
  }

  private var sendProcessInstanceCreated = true
  private def trySendProcessInstanceCreated() {
    if (sendProcessInstanceCreated && blockingHandler.allSubjectsReady(request.userID)) {
      //      context.parent ! ProcessInstanceCreated(request, id, self, graphJson, executionHistory, Array())
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

    private val subjects = MutableMap[UserID, SubjectInfo]()

    // an entry for the messagepool
    private case class MessagePoolEntry(
      var subjectCount: Int,
      orig: ActorRef,
      message: SubjectToSubjectMessage)
    // this pool contains the unsent messages, which will be sent to subjects
    // which are created first
    private var messagePool: Set[MessagePoolEntry] = Set()

    
    def insertUserIDs(userIDs: Array[UserID]) {
      
    }
    
    /**
     * Adds a Subject to this multisubject
     */
    def createAndAddSubject(userID: UserID) {
      // handle blocking
      handleBlockingForSubjectCreation(userID)
      // create subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(userID, self, subject)))
      // and store it in the map
      subjects += userID -> SubjectInfo(subjectRef, userID)

      // if there are messages to deliver to the new subject,
      // forward them to the subject 
      for (entry <- messagePool) {
        handleBlockingForMessageDelivery(userID)
        subjectRef.!(entry.message)(entry.orig)
        entry.subjectCount -= 1
      }
      // remove all entry, which has been sent to enough subjects
      messagePool = messagePool.filter(_.subjectCount > 0)

      logger.debug("Processinstance [" + id + "] created Subject " +
        subject.id +  " for user " + userID)

      // inform the subject provider about his new subject
      context.parent !
        SubjectCreated(userID, processID, id, subject.id, subjectRef)

      // start the execution of the subject
      subjectRef ! StartSubjectExecution()
    }

    def handleSubjectStarted(message: SubjectStarted) {

      logger.debug("Processinstance [" + id + "] Subject " + subject.id + "[" +
        message.userID + "] started")

      subjects(message.userID).running = true
    }

    def handleSubjectTerminated(message: SubjectTerminated) {

      logger.debug("Processinstance [" + id + "] Subject " + subject.id + "[" +
        message.userID + "] terminated")

      // decrease the subject counter
      runningSubjectCounter -= 1

      subjects(message.userID).running = false
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
        // TODO whom is the first subject????
        waitForContextResolver(userID)
        contextResolver !
          // TODO userID ist in hier falsch gesetzt
          RequestUserID(SubjectInformation(subject.id), s => AddSubject(request.userID, subject.id))
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
          handleBlockingForMessageDelivery(message.userID)
          subjectInfo.ref.forward(message)
        } else if (restartSubject) {
          // subjectcreation = subjectrestart
          // increase the subject counter
          runningSubjectCounter += 1
          handleBlockingForSubjectCreation(subjectInfo.userID)
          // start the execution
          subjectInfo.ref ! StartSubjectExecution()

          handleBlockingForMessageDelivery(message.userID)
          subjectInfo.ref.forward(message)
        }
      }
    }
  }
}
