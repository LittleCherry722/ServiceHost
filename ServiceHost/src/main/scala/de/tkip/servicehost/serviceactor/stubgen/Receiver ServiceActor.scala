package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import java.util.Date
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.servicehost.ActorLocator
import de.tkip.servicehost.ServiceAttributes._
import scala.collection.immutable.Map
import scala.collection.mutable.Queue
import de.tkip.sbpm.application.subject.misc.Rejected

class Receiver ServiceActor extends ServiceActor {
  override protected val INPUT_POOL_SIZE: Int = 20
  
  override protected val serviceID: ServiceID = "Subj5:579e2526-d78f-4768-8933-4411b850394e"
  override protected val subjectID: SubjectID = "Subj5:579e2526-d78f-4768-8933-4411b850394e"
  
  
  override protected def states: List[State] = List(
      ReceiveState(0,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 6),""),
      ExitState(5,null,Map(),Map(),null),
      ReceiveState(1,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 3),""),
      ReceiveState(6,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 1),""),
      ReceiveState(2,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 5),""),
      ReceiveState(3,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 4),""),
      ReceiveState(4,"exitcondition",Map("m1" -> Target("Subj6:befe4b61-eb0e-46e1-87ae-a4cf82ebc699",-1,-1,false,"")),Map("m1" -> 2),"")
    )

  // start with first state
  // TODO: that is not always the start state!
  def getStartState(): State = {
    getState(0)
  }

  
  private val messages: Map[MessageType, MessageText] = Map(
      "Message" -> "m1"
    )

  private val inputPool: scala.collection.mutable.Map[Tuple2[MessageType, SubjectID], Queue[SubjectToSubjectMessage]] = scala.collection.mutable.Map()
  
  // this map holds the queue of the income messages for a channel
  private val messageQueueMap = MutableMap[ChannelID, Queue[SubjectToSubjectMessage]]()
  // this map holds the overflow queue of the income messages for a channel
  private val messageOverflowQueueMap = MutableMap[ChannelID, Queue[(ActorRef, SubjectToSubjectMessage)]]()
  

  // Subject default values
  private var target = -1
  private var messageContent: String = "" // will be used in getResult

  override def reset = {
    // TODO: reset custom properties
    super.reset
  }

  def processMsg() {
    log.debug("processMsg")

    state match {
      case rs: ReceiveState => {
        var message: SubjectToSubjectMessage = null

        for ((branch, target) <- state.targets) {
          val messageType: MessageType = branch
          val fromSubjectID: SubjectID = target.target.subjectID
          val key = (messageType, fromSubjectID)
          log.debug("processMsg: key = " + key)

          if (inputPool.contains(key) && inputPool(key).length > 0) {
            message = inputPool(key).dequeue;
          }
        }

        log.debug("processMsg: message = " + message)

        if (message != null) {
          this.messageContent = message.messageContent

          this.branchCondition = message.messageType

          rs.handle(message) // calls changeState
        }
        else log.info("ReceiveState could not find any matching message. ReceiveState will wait until it arrivies")
      }
      case _ =>
        log.info("unable to handle message now, needs to be in ReceiveState. Current state is: " + state)
    }
  }

  def stateReceive = {
    
    //new code:
    
    //this case will be executed, if the inputpoo receives a disabled message and the input pool is not full.
    //the incomming message will be stored and a reply (stored message) will be sent back to the sender
    case message: SubjectToSubjectMessage if (spaceAvailableInMessageQueue(message.from, message.messageType) && !message.enabled) => {
      //store reservation message
      log.debug("InputPool received disabled message from " + sender + " which message.messageID = " +message.messageID)
      //send stored notification back to sender
      sender !! Stored(message.messageID)
      // store the reservation
      enqueueMessage(message)
      log.debug("reservation is done!")
    }
    
    //this case will be executed, if a reable-request is received
    //of so, the message which has to be enabled will be searched in the qeueu and will be enabled responding with a enabled-message
    //if there is no message to enable, the response will be a rejected message
    case message: SubjectToSubjectMessage if (message.enabled) => {
      log.debug("InputPool received enable request from " + sender)
      //replace reservation with real message
      
      if(enableMessage(message)){
    	  //send enabled notification back to sender
    	  sender !! Enabled(message.messageID)
    	  log.debug("Message enabled" + getMessageArray(message.from, message.messageType).mkString("{", ", ", "}"))
    	  
	      // inform the states about this change
	      broadcastChangeFor((message.from, message.messageType))
	      // unblock this user
	      blockingHandlerActor ! UnBlockUser(userID)
      
      }else{
        //no reservation found for thei message! Send reject message
        log.warning("message rejected, no message to enable: {}", message)
        sender !! Rejected(message.messageID)
      }
    }
    
    //this case will be executed, if a disabled message was received and the input queue is full
    //the message will be stored in the overflow queue and a oferflow-message will be sent to the sender
    case message: SubjectToSubjectMessage if (!spaceAvailableInMessageQueue(message.from, message.messageType) && !message.enabled) => {
      //store reservation message
      log.debug("InputPool received reservation from " + sender + " -> but message queue is full! Save sender id and reject reservation")
      //send stored notification back to sender
      sender !! Overflow(message.messageID)
      
      //put message into the overflowQueue
      enqueueOverflowMessage(message)
    }
    
    
    
    //old code:
    case message: SubjectToSubjectMessage => {
      // TODO forward /set variables?
      log.debug("receive message: " + message)
      storeMsg(message, sender)

      state match {
        case rs: ReceiveState =>
          processMsg()
        case _ =>
          log.info("message will be handled when state changes to ReceiveState. Current state is: " + state)
      }
    }
  }

  def changeState() {
    log.debug("changeState: old state: " + state)
    state match {
      case s: ExitState => {
        log.warning("already in ExitState, can not change state")
      }
      case _ => {
        if (state.targetIds.size > 1) {
          if (this.branchCondition != null) {
            state = getState(state.targetIds(this.branchCondition))

          } else log.warning("no branchcodition defined")

        } else state = getState(state.targetIds.head._2)

        // TODO: state könnte null sein, oder auch der alte..
        state.process()
      }
    }
    log.debug("changeState: new state: " + state)
  }

  def getState(id: Int): State = {
    states.find(x => x.id == id).getOrElse(null)
  }

  def storeMsg(message: Any, sender: ActorRef): Unit = {
    log.debug("storeMsg: " + message + " from " + sender)
    message match {
      case message: SubjectToSubjectMessage => {
        val key = (message.messageType, message.from)
        log.debug("storeMsg: key = " + key)

        if (inputPool.contains(key)) {
          if (inputPool(key).size < INPUT_POOL_SIZE) {
            (inputPool(key)).enqueue(message)
            log.debug("storeMsg: Stored")
            sender !! Stored(message.messageID)
          } else {
            log.debug("storeMsg: Rejected")
            sender !! Rejected(message.messageID)
          }
        } else {
          inputPool(key) = Queue(message)
          log.debug("storeMsg: Stored")
          sender !! Stored(message.messageID)
        }
      }
      case message => log.warning("unable to store message: " + message)
    }
  }

  def getBranchIDforType(messageType: String): MessageText = {
    messages(messageType)
  }

  def getDestination(): ActorRef = {
    manager.get
  }

  def terminate() {
    ActorLocator.serviceActorManager !! KillProcess(serviceID, processInstanceID)
  }

  def getProcessID(): ProcessID = {
    processID
  }

  def getSubjectID(): String = {
    serviceID
  }

  def getResult(msg: String): String = {   // handle the messageContent
    msg
  }
  
  
  //new code:
   /**
   * returns true or false if for the condition messageQueue.size < INPUT_POOL_SIZE
   * return true if INPUT_POOL_SIZE is -1 (no limit set)
   */
  private def spaceAvailableInMessageQueue(subjectID: SubjectID, messageType: MessageType) : Boolean = {
     log.debug("Checking for queue space!")
    
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (subjectID, messageType),
        Queue[SubjectToSubjectMessage]())

        log.debug("Limit: " + INPUT_POOL_SIZE + "; current size: " + messageQueue.size)
        
    // check the queue size
    if (messageQueue.size < INPUT_POOL_SIZE || messageLimit == -1) {
      true
    } else {
      false
    }
  }
  
  /**
   * Enqueue a message, add it to the correct queue
   */
  private def enqueueMessage(message: SubjectToSubjectMessage) = {
    // get or create the message queue
    val messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

      messageQueue.enqueue(message)
      log.debug("message has been queued!")
      
      spaceAvailableInMessageQueue(message.from, message.messageType);
  }
  
  /**
   * Enqueue a message in the overflow queue, 
   */
  private def enqueueOverflowMessage(message: SubjectToSubjectMessage) = {
    // get or create the message queue
    val messageOverflowQueue =
      messageOverflowQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[(ActorRef, SubjectToSubjectMessage)]())

      messageOverflowQueue.enqueue((sender, message))
      log.debug("message has been queued to overflow queue!")
  }
  
  /**
   * enables previously received message
   */
  private def enableMessage(message: SubjectToSubjectMessage) : Boolean = {
    // get or create the message queue
    var messageQueue =
      messageQueueMap.getOrElseUpdate(
        (message.from, message.messageType),
        Queue[SubjectToSubjectMessage]())

        //loop over queue and enable message if found
        var counter = 0
    	for (element <- messageQueue){
    	  if(element.messageID == message.messageID && !element.enabled){
    	    element.enabled = true
    	    counter = counter + 1
    	  }
    	}
      
	if(counter != 0){ 
    	true
    }else{
      false
      //throw new exception
    }
  }

  
}
