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
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.persistence._

// message to get history of project instance
//case class GetHistory()

// represents the history of the instance
case class History(processName: String,
                   instanceId: ProcessInstanceID,
                   var processStarted: Date = null, // null if not started yet
                   var processEnded: Date = null, // null if not started or still running
                   entries: Buffer[history.Entry] = ArrayBuffer[history.Entry]()) // recorded state transitions in the history

// TODO hier lassen oder woanders hin?
case class AddSubject(userID: UserID, subjectID: SubjectID)

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(id: ProcessInstanceID, processID: ProcessID) extends Actor {

  protected implicit val timeout = Timeout(10 seconds)

  //  val future = ActorLocator.persistenceActor ? GetProcess(Some(2))
  //  val future = ActorLocator.persistenceActor ? GetProcess(Some(processID))
  // TODO irgentwie bekommt man dann die id
  // get and unmarshall the graph

  val processName = "MyProcess"
  val startSubject: String = "Subj1" // TODO wie bekommen?

  val graphID = 2
  val graphFuture = ActorLocator.persistenceActor ? GetGraph(Some(graphID))
  // TODO mit Await oder irgentwie anders?
  val graphString = Await.result(graphFuture, timeout.duration).asInstanceOf[String]

  val graph: ProcessGraph = parseGraph(graphString)

  // TODO wie uebergeben?
  private val contextResolver = context.actorOf(Props(new ContextResolverActor))

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  private var messagePool = Set[(ActorRef, SubjectInternalMessage)]()

  private var subjectCounter = 0
  private val subjectMap = collection.mutable.Map[SubjectName, SubjectRef]()

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(processName, id, new Date()) // TODO start time = creation time?
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])

  // add the first subject
  contextResolver !
    RequestUserID(SubjectInformation(startSubject), AddSubject(_, startSubject))

  def receive = {

    case as: AddSubject => {
      val subject: Subject = getSubject(as.subjectID)

      // create the subject
      val subjectRef =
        context.actorOf(Props(new SubjectActor(as.userID, self, subject)))
      // add the subject to the management map
      subjectMap += subject.id -> subjectRef
      subjectCounter += 1

      println("process " + id + " created subject " + subject.id + " for user " + as.userID)

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

    case st: SubjectTerminated => {
      // log end time in history
      subjectCounter -= 1
      println("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (subjectCounter == 0) {
        executionHistory.processEnded = new Date()
        context.stop(self) // TODO stop process instance?
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
          HistoryTestData.generate(processName, id)(debugMessagePayloadProvider)
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
        System.err.println("ProcessInstance has message for subject " +
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
    graph.subjects.find(_.id == name).get
  }
}
