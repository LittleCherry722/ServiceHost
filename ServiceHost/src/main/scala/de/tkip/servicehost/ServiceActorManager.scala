package de.tkip.servicehost

import akka.actor.Actor
import Messages._
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import akka.actor.Props

class ServiceActorManager extends Actor{
  
  val referenceXMLActor = ActorLocator.referenceXMLActor
  
  def receive = {
    case execute: ExecuteServiceMessage => {
      implicit val timeout = Timeout(5 seconds)
      val future: Future[Any] = ask(referenceXMLActor, GetClassReferenceMessage(execute.serviceID))
//      val future = referenceXMLActor ? GetClassReferenceMessage(execute.serviceID)
      val classRef: ClassReferenceMessage = Await.result(future, timeout.duration).asInstanceOf[ClassReferenceMessage]
      val actorc = classRef.classReference
      val actor = this.context.actorOf(new Props(actorc))
      actor ! AddService(1, sender)
    }
    case classReferenceResponse: ClassReferenceMessage =>{
      //TODO implement
    }
  }

}