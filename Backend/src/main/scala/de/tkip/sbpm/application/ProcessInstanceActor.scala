package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessModel
import de.tkip.sbpm.model.Subject
import java.util.Date
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer

// message to get history of project instance
case class GetHistory()

// represents the history of the instance
case class History(processName: String,
                   instanceId: ProcessInstanceID,
                   var processStarted: Date = null, // null if not started yet
                   var processEnded: Date = null, // null if not started or still running
                   entries: Buffer[history.Entry] = ArrayBuffer[history.Entry]()) // recorded state transitions in the history

// sub package for history related classes
package history {
  // represents an entry in the history (a state transition inside a subject)
  case class Entry(timestamp: Date, // time transition occurred
                   subject: String, // respective subject
                   fromState: State = null, // transition initiating state (null if start state)
                   toState: State, // end state of transition
                   message: Message = null) // message that was sent in transition (null if none)
  // describes properties of a state
  case class State(name: String, stateType: String)
  // message exchanged in a state transition
  case class Message(id: Int,
                     messageType: String,
                     from: String, // sender subject of message
                     to: String, // receiver subject of message 
                     data: MessagePayloadLink = null, // link to msg payload
                     files: Seq[MessagePayloadLink] = null) // link to file attachments
  // represents a link to a message payload which contains a actor ref 
  // and a payload id that is needed by that actor to identify payload
  case class MessagePayloadLink(actor: ActorRef, payloadId: String)
  // this message can be sent to message payload providing actors referenced in
  // message payload link to retrieve actual payload
  case class GetMessagePayload(messageId: Int, payloadId: String)
}

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(val id: ProcessInstanceID, val process: ProcessModel) extends Actor {

  // TODO wie übergeben?
  private val contextResolver = context.actorOf(Props(new ContextResolverActor))

  // this pool stores the message to the subject, which does not exist,
  // but will be created soon (the UserID is requested)
  private var messagePool = Set[(ActorRef, SubjectMessage)]()

  private var subjectCounter = 0
  private val subjectMap = collection.mutable.Map[SubjectName, SubjectRef]()

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(process.name, id, new Date()) // TODO start time = creation time?
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])
  
  def receive = {

    case as: AddSubject =>
      val subject: Subject = getSubject(as.subjectName)

      println("addsubject" + subject)
      val subjectRef = context.actorOf(Props(new SubjectActor(self, subject)))
      subjectMap += subject.subjectName -> subjectRef
      subjectCounter += 1

      println("process " + id + " created subject " + subject.subjectName + " for user " + as.userID) //TODO
      // if there are messages to deliver to the new subject,
      // forward them to the subject 
      if (!messagePool.isEmpty) {
        for ((orig, sm) <- messagePool if sm.toCond.subjectName == subject.subjectName) {
          subjectRef.!(sm)(orig)
        }
        messagePool = messagePool.filterNot(_._2.toCond.subjectName == subject.subjectName)
      }

      // TODO subjecte direkt ausfuehren?
      subjectRef ! ExecuteRequest(as.userID, id)

    case End =>
      // log end time in history
      executionHistory.processEnded = new Date()

      println("shutting down processInstance " + id)
      subjectCounter -= 1
      if (subjectCounter == 0) {
        context.system.shutdown() // TODO do not shutdown whole system here!
      }

    case sm: SubjectMessage =>
      if (subjectMap.contains(sm.toCond.subjectName)) {
        // if the subject already exist just forward the message
        subjectMap(sm.toCond.subjectName).forward(sm)
      } else {
        // if the subject does not exist create the subject and forward the
        // message afterwards
        // store the message in the message-pool
        messagePool += ((sender, sm))
        // ask the Contextresolver for the userid to answer with an AddSubject
        contextResolver !
          RequestUserID(
            SubjectInformation(sm.toCond.subjectName),
            AddSubject(_, id, sm.toCond.subjectName))
      }

    case pr: ExecuteRequest =>
      println("execute")
      subjectMap.values.map(_ ! pr) // TODO: send to all subjects?
      pr.sender ! id // answer to original sender
      
    case asts: AddState =>
      if (subjectMap.contains(asts.subjectName))
        subjectMap(asts.subjectName) ! asts.behaviourState

    // return current process instance history
    case msg: GetHistory => sender ! {
      if (msg.isInstanceOf[Debug]) HistoryTestData.generate(process.name, id)(debugMessagePayloadProvider)
      else executionHistory
    }

    // add an entry to the history
    // (should be called by subject actors when a transition occurs)
    case he: history.Entry => executionHistory.entries += he

    case ss => println("ProcessInstaceActor: not yet implemented Message: " + ss)
  }

  private def getSubject(name: String): Subject = {
    // TODO increase performance
    process.subjects.find(_.subjectName == name).get
  }
}