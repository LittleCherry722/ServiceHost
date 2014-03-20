package de.tkip.servicehost.serviceactor

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.serviceactor._
import akka.actor.Props
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.sbpm.application.subject.behavior.Target
import akka.actor.ActorRef
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.servicehost.serviceactor.stubgen.State
import de.tkip.sbpm.eventbus._

class StaplesServiceActor extends ServiceActor {

  private var userId = 0
  private var processId = 0
  private var manager: Option[ActorRef] = None
  private val orderMessageBuffer: ArrayBuffer[SubjectToSubjectMessage] = ArrayBuffer()

  val trafficSubscriber = context.actorOf(Props(new Actor {
      def receive = {
        case msg => handleOrders(msg)
      }
    }))
  SbpmEventBus.subscribe(trafficSubscriber, "/traffic")

  def handleOrders(eventBusMessage: Any): Unit = {
    println("handle " + orderMessageBuffer.length + " orders from orderMessageBuffer")

    for {orderMessage <- orderMessageBuffer} {
      val msgToExternal = false // false: it should not leave sbpm
      val target = Target("Großunternehmen", 0, 1, false, None, msgToExternal, true)
      val messageType = "Lieferdatum"
      val remoteUserId = 1 // TODO: context resolver einbinden, um UserID zu bestimmen. resolven sollte jedoch in sbpm, nicht beim service host passieren
      target.insertTargetUsers(Array(remoteUserId))
      val to_actor = manager.get


      val messageContent = eventBusMessage match {
        case SbpmEventBusTrafficFlowMessage(sensorId, count) => "Die Bestellung \"" + orderMessage.messageContent + " (" + Integer.valueOf(orderMessage.messageContent) * 2 + ")" + "\" ist aufgrund der Verkehrslage in " + count + " Tagen fertig."
        case _ => "Die Bestellung \"" + orderMessage.messageContent + " (" + Integer.valueOf(orderMessage.messageContent) * 2 + ")" + "\" ist morgen fertig."
      }

      val answer = SubjectToSubjectMessage(0, processId, remoteUserId, "Staples", target, messageType, messageContent)
      println("sending " + answer)

      to_actor ! answer
    }

    orderMessageBuffer.clear
  }

  def receive: Actor.Receive = {
    case ExecuteServiceMessage =>
      println("here")
    case message: SubjectToSubjectMessage => {

      // TODO: use InputPoolActor ?

      // TODO: check if it is a order
      orderMessageBuffer += message

      // Unlock the sender
      sender ! Stored(message.messageID)
      println("unblocked sender")
    }
    case request: CreateProcessInstance => {
      userId = request.userID
      processId = request.processID
      manager = request.manager

      // TODO implement

      // fake ProcessInstanceActor:

      val persistenceGraph = null
      val processName = ""
      val startedAt = new Date()
      val actions = null
      val processInstanceData = ProcessInstanceData(0, request.name, request.processID, processName, persistenceGraph, false, startedAt, request.userID, actions)
      println("Sender: " + sender)
      println("Receicer: " + self)
      sender ! ProcessInstanceCreated(request, self, processInstanceData)

    }
    case GetProxyActor => {
      println("received GetProxyActor")
      // TODO implement
      // fake ProcessInstanceProxyActor:
      sender ! self
    }

  }

  def changeState() = {}

  def getState(id: Int): State = { ??? }

  def storeMsg(message: Any, tosender: ActorRef): Unit = {}

  def getDestination(): ActorRef = { ??? }

  def terminate(): Unit = {}

  def getUserID(): Int = { ??? }

  def getProcessID(): Int = { ??? }

  def getSubjectID(): String = { ??? }
  
}