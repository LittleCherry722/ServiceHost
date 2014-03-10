package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._

class ReferenceXMLActor extends Actor{
  
  def receive: Actor.Receive = {
    case createReference: CreateXMLReferenceMessage => {
      //TODO implement 
    }
    case getReference: GetClassReferenceMessage =>{
      getReference.serviceID match {
        case "Staples" =>
        	sender ! new ClassReferenceMessage(getReference.serviceID, classOf[StaplesServiceActor])
        case _ =>
          println(getReference.serviceID)
      }
      
    }
  }

}