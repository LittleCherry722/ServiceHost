package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.sbpm.application.ProcessInstanceActor
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.ProcessInstanceActor._

import de.tkip.sbpm.instrumentation.InstrumentedActor

import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.stubgen.{ReceiveState, ExitState, State}

import scala.collection.immutable.List
import scala.collection.mutable.{Map, ListBuffer}

abstract class ServiceActor extends InstrumentedActor {
  protected implicit val service = this

  protected def INPUT_POOL_SIZE: Int = 100

  protected  def serviceName: String

  protected def serviceID: ServiceID

  protected def subjectID: SubjectID

  protected def states: List[State]

  protected var state: State = getStartState()

  protected var processID: ProcessID = -1
  protected var processInstanceID: ProcessInstanceID = -1
  protected var remoteProcessID: ProcessInstanceID = -1
  protected var manager: ActorRef = null
  protected var managerUrl: String = ""
  protected var receiver: ActorRef = null
  var branchCondition: String = null
  var returnMessageContent: String = "received message"
  var serviceInstance: ServiceActorRef = null
  protected var variablesOfSubject = Map[String, String]()
  protected var messages = Map[MessageType, MessageText]()
  protected var variablesOfService = collection.mutable.Map[String, ListBuffer[SubjectToSubjectMessage]]()
  protected var tempAgentsMap = collection.mutable.Map[String, ProcessInstanceActor.Agent]()
  protected object TimeoutExpired
  protected var currentStatesMap = Map[Int, State]()
  protected var suspendStatesMap = Map[Int, State]()
  protected var priorityStateList = List[Int]()


  def reset()

  def processMsg(currentStateId: Int): Unit

  def processCloseIP(currentStateId: Int): Unit

  def processOpenIP(currentStateId: Int): Unit

  def processSendState(currentStateId: Int): Unit

  def changeState(oldStateId: Int) {
    val oldState = getState(oldStateId)

    var newState: State = null
    log.debug("==========    CHANGE STATE    ==========   old state: " + oldState)

    oldState match {
      case s: ExitState => {
        log.warning("already in ExitState, can not change state")
      }

      case s: ReceiveState => {
        if (this.branchCondition != null) {
          newState = getState(oldState.targetIds(this.branchCondition))
        } else {
          log.warning("no branchCondition defined !")
        }
      }

      case _ => {
        val oldStateTargetsIdSize = oldState.targetIds.size
        oldStateTargetsIdSize match {
          case 0 => {
            log.debug("no next state")

          }
          case 1 => newState = getState(oldState.targetIds.head._2)
          case _ => {
            if(this.branchCondition != null){
              newState = getState(oldState.targetIds(this.branchCondition))
            } else {
              log.warning("no branchCondition defined")
            }
          }
        }
      }

    }
    if ((!currentStatesMap.isEmpty) && currentStatesMap.contains(oldStateId)) {
      // currentStates isn't empty and includes the oldState.
      currentStatesMap = currentStatesMap - (oldStateId) // delete oldState
      log.debug("Add new State: {} !!!", newState)

      if(((newState != null) && priorityStateList.contains(newState.id)) || (newState == null) ){  // suspendState removes state to currentStates
        log.debug("==========    move suspendState to currentState    ==========" )
        suspendStatesMap.foreach( s => {
          currentStatesMap += s._1 -> s._2
          suspendStatesMap -= s._1
          currentStatesMap(s._1).process()
        })
      }
      if( newState != null){
        currentStatesMap += newState.id -> newState
        log.debug("==========    CHANGE STATE    ==========   new state: " + newState)
        newState.process()
      }

    } else {
      log.warning("The old State didn't exist... The current state is invalid !")
    }
  }


  def getStartState(): State

  def getState(id: Int): State

  def addState(stateId: Int): Unit

  def killState(stateId: Int): Unit

 // def storeMsg(message: Any, sender: ActorRef): Unit


  def getDestination(): ActorRef

  def terminate(): Unit

  def getProcessID(): ProcessID

  def getProcessInstanceID(): ProcessInstanceID

  def getSubjectID(): String

  def getMessage(): String = returnMessageContent

  def getBranchCondition() = branchCondition

  def setMessage(message: String) = returnMessageContent = message

  def isObserverState(id: Int)

  def isNormalRunning(id: Int)


 // def parseState(id: Int): Map[Int, Int]

  def stateReceive: Receive

  def wrappedReceive: Receive = generalReceive orElse stateReceive orElse errorReceive

  def generalReceive: Receive = {
    case GetProxyActor => {
      sender !! self
    }

    case update: UpdateProcessData => {
      this.processInstanceID = update.processInstanceID
      this.manager = update.manager
      this.processID = update.processID
    }

    case message: ExecuteServiceMessage => {
      log.info("received {}", message)
    }
  }

  private def errorReceive: Receive = {
    case x => {
      log.error("unsupported: {}", x)
    }
  }

  def getVariableName(vType: String): String = {
    var vName = ""
    for ((variableName, variableType) <- variablesOfSubject) {
      if (variableType == vType)
        vName = variableName
      else
        vName
    }
    vName
  }

  def addMessage(msg: SubjectToSubjectMessage, variableName: String): Unit = {
    if (this.variablesOfService.contains(variableName)) {
      this.variablesOfService(variableName).append(msg)
    } else {
      this.variablesOfService += variableName -> ListBuffer(msg)
    }
  }

  /*
  Variable's methods
   */
  def vMerge(vName: String): Set[Variable] = {
    var newMsgContent = Set[Variable]()
    if (variablesOfService.contains(vName)) {
      variablesOfService(vName).foreach(message => message.messageContent match {
        case msgContent: TextContent => log.debug("TextContent can't be merged!")

        case msgContent: MessageSet => {
          newMsgContent = newMsgContent + msgContent.messages
        }

        case _ => log.debug("Other Types will be processed in future!")
      })

      var tempDepth = Set[Int]()
      newMsgContent.foreach(messageSet => {
        tempDepth += messageSet.head.depth
      })
      if (tempDepth.size == 1) {
        newMsgContent
      } else {
        log.debug("Because of different depth, messagesContent can't be merged!")
        Set()
      }

    } else {
      log.debug("Variables do not exist!")
      Set()
    }

  }

  def vSplit(variableName: String): Set[Message] = {
    //  type Variable = Set[Message]
    var messageSetContent = Set[Message]()
    if (variablesOfService.contains(variableName)) {
      variablesOfService(variableName).foreach(message => message.messageContent match {
        case msgContent: TextContent => {
          log.debug("TextContent can't be split! ")
        }
        case msgContent: MessageSet => {
          messageSetContent = messageSetContent ++ msgContent.messages
        }
      })
      messageSetContent
    } else {
      log.debug("Messages do not exist!")
      Set.empty
    }
  }

  def vSelection(messagesSet: Variable, category: Option[String]): Set[Message] = {
    // custom condition
    /*
    this is a just an example
     */
    //   case class Message(vName: String, channel: Channel, depth: Int, mType: String, content: MessageContent)
    var senders = Set[String]()
    messagesSet.foreach(message => {
      senders += message.channel.subjectId
    })
    var resultOfSelection = Set[Message]()
    val condition = category.getOrElse("")
    if(condition != "") {
      condition match{

        case variableName if (variablesOfService.contains(condition)) => resultOfSelection = messagesSet.filter(message => (message.vName == condition))

        case messageName if (this.messages.contains(condition)) => resultOfSelection = messagesSet.filter(message => message.mType == this.messages(condition))

        case senderID if (senders.contains(condition)) => resultOfSelection = messagesSet.filter(message => (message.channel.subjectId == condition))

        //case depth if( messagesSet.foreach(message =>(message.depth == condition.toInt))) => resultOfSelection = messagesSet.filter(message => (message.depth == condition.toInt))

        case messageContent => {
          resultOfSelection = messagesSet.filter(message => message.content match {
            case msg: TextContent => msg.content == condition
            case msg: MessageSet => {
              log.debug("MessageContent must be TextContent!")
              false
            }
          })
        }

        case _ => {
          log.debug("custom condition")
        }
      }
    }else{
      // default condition
      resultOfSelection = messagesSet.drop(2)
    }
    resultOfSelection
  }

  def vDifference(variableA: Variable, variableB: Variable): Set[Message] = {
    if ((variableA.head.vName == variableB.head.vName) && (variableA.head.depth == variableB.head.depth)) {
      variableA -- variableB
    } else {
      Set()
    }
  }

  def vEncapsulation(variableName: String): Variable = {
    var newMessageSet = Set[Message]()
    variablesOfService(variableName).foreach(message => {
      val currentAgent = Agent(message.processID, tempAgentsMap(message.from).address, message.from)
      val currentChannel = Channel(subjectID, currentAgent)

      message.messageContent match {
        case msgContent: TextContent => {
          val currentMessage = Message(variableName, currentChannel, 1, message.messageType, message.messageContent)
          newMessageSet = newMessageSet + currentMessage // create a new MessageSet
        }

        case msgContent: MessageSet => {
          val currentMessage = Message(variableName, currentChannel, msgContent.messages.head.depth + 1, message.messageType, msgContent)
          newMessageSet = newMessageSet + currentMessage
        }

      }
    })

    if (newMessageSet.map(_.depth).size != 1) {
      log.debug("Because of different depth, messages can't be merged!")
      Set()
    } else
      newMessageSet // will be used in SubjectTOSubjectMessage
  }

  def vExtraction(variableName: String): Set[Message] = {
    var resultOfMessage = Set[Message]()
    if (variablesOfService.contains(variableName)) {
      variablesOfService(variableName).foreach(subjectToSubjectMessage => subjectToSubjectMessage.messageContent match {
        case msgContent: TextContent => {
          log.debug("TextContent can't be extracted!")
        }

        case msgContent: MessageSet => {
          resultOfMessage ++= extraction(msgContent.messages)
        }
      })
      resultOfMessage
    } else {
      log.debug("Messages do not exist!")
      Set()
    }

  }

  def extraction(messages: Variable): Set[Message] = {
    var tempMessageSet = Set[Message]()
    messages.foreach(message => if (message.depth != 1) {
      message.content match {
        case content: MessageSet => {
          tempMessageSet ++= content.messages
        }
          extraction(tempMessageSet)
      }
    })
    messages
  }

}

