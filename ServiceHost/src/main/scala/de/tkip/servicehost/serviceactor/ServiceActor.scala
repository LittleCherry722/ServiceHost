package de.tkip.servicehost.serviceactor

import akka.actor._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

import de.tkip.sbpm.instrumentation.InstrumentedActor

import de.tkip.servicehost.Messages._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.stubgen.State

abstract class ServiceActor extends InstrumentedActor {
  protected implicit val service = this

  protected def INPUT_POOL_SIZE: Int = 100

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


  val selectedMessages = collection.mutable.ListBuffer[Tuple2[ActorRef, SubjectToSubjectMessage]]()

  private val variablesOfSubject = scala.collection.mutable.Map[String, Variable]()


  protected case class Variable(id: String) {
    //abstract  class Variable(id: String) {
    val vId = id
    var tiefe: Int = 0
    private val receivedMessages = collection.mutable.ListBuffer[Tuple2[ActorRef, SubjectToSubjectMessage]]()
    private val receivedVariables = collection.mutable.ListBuffer[Tuple2[ActorRef, Variable]]()


    def addMessage(absender: ActorRef, message: SubjectToSubjectMessage) {
      receivedMessages.append(Tuple2(absender, message))
    }

    //def messages = receivedMessages.toArray

    var messages = receivedMessages.toArray

    def addVariable(absender: ActorRef, vmsg: Variable) {
      receivedVariables.append(Tuple2(absender, vmsg))
    }

    override def toString() = {
      "{%s <- %s}".format(id, receivedMessages.mkString("[", ", ", "]"))
    }
  }

  def vVereinigung() = {

  }

  def vSchnitt() = {

  }

  def vSelection(v: Variable) = {
    for (i <- 0 until v.messages.size) {
      if (v.messages(i)._2.messageContent == "conditions") {
        selectedMessages.append(v.messages(i))
      }
    }
    selectedMessages.toArray
  }

  def vDifference() = {

  }

  def vExtract() = {

  }

  // create newVariable
  def vEncapsulation(selectedMessage: Array[(ActorRef, SubjectToSubjectMessage)], v: Variable): Variable = {
    val newVariable = Variable(v.id)
    newVariable.tiefe = 0
    newVariable.messages = selectedMessage
    newVariable
  }


  def reset(): Unit = {
    state = getStartState()
  }

  def processMsg(): Unit

  def processSendState(): Unit

  def changeState()

  def getStartState(): State

  def getState(id: Int): State

  def storeMsg(message: Any, tosender: ActorRef): Unit

  def getDestination(): ActorRef

  def terminate(): Unit

  def getProcessID(): ProcessID

  def getProcessInstanceID(): ProcessInstanceID

  def getSubjectID(): String

  def getMessage(): String = returnMessageContent

  def getBranchCondition() = branchCondition

  def setMessage(message: String) = returnMessageContent = message

  def stateReceive: Receive

  def wrappedReceive: Receive = generalReceive orElse stateReceive orElse errorReceive

  def generalReceive: Receive = {
    case GetProxyActor => {
      sender !! self
    }

    case update: UpdateProcessData => {
      this.processInstanceID = update.processInstanceID
      this.remoteProcessID = update.remoteProcessID
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

}

