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
import de.tkip.sbpm.application.subject.misc.GetProxyActor

class StaplesServiceActor extends ServiceActor {
	
  private var userId = 0
  private var processId = 0
  private var manager: Option[ActorRef] = None
  
  def receive: Actor.Receive = {
    case ExecuteServiceMessage => 
      println("here")
    case message: SubjectToSubjectMessage => {   
      
      // fake InputPoolActor:

      // Unlock the sender
      sender ! Stored(message.messageID)
      println("unblocked sender")

      // reply immediate:
      // TODO: EventBus einbinden


      val msgToExternal = false // false: it should not leave sbpm
      val target = Target("Großunternehmen",0,1,false,None,msgToExternal,true)
      val messageType = "Lieferdatum"
      val messageContent = "Die Bestellung \"" + message.messageContent + "(" + Integer.valueOf(message.messageContent) * 2 + ")" + "\" ist morgen fertig. "
      val remoteUserId = 1 // TODO: context resolver einbinden, um UserID zu bestimmen. resolven sollte jedoch in sbpm, nicht beim service host passieren
      target.insertTargetUsers(Array(remoteUserId))
      val answer = SubjectToSubjectMessage(0, processId, remoteUserId, "Staples", target, messageType, messageContent)
      val to_actor = manager.get
      println("send " + answer + " to " + to_actor)
      to_actor ! answer
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
}