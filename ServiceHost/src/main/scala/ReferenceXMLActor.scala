import akka.actor.Actor
import Messages.CreateXMLReferenceMessage
import Messages.GetClassReferenceMessage

class ReferenceXMLActor extends Actor{
  
  def receive = {
    case createReference: CreateXMLReferenceMessage => {
      //TODO implement 
    }
    case getReference: GetClassReferenceMessage =>{
      //TODO implement
    }
  }

}