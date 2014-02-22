package de.tkip.servicehost

import akka.actor.Actor
import de.tkip.servicehost.Messages._
import de.tkip.servicehost.serviceactor._

class ReferenceXMLActor extends Actor{
  
  def receive = {
    case createReference: CreateXMLReferenceMessage => {
      //TODO implement 
    }
    case getReference: GetClassReferenceMessage =>{
      sender ! new ClassReferenceMessage(getReference.serviceID, classOf[AddServiceActor])
    }
  }

}