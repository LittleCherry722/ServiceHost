import akka.actor.Actor
import Messages.ExecuteServiceMessage
import Messages.ClassReferenceMessage

class RuntimeActor extends Actor{
  
  def receive = {
    case execute: ExecuteServiceMessage => {
      //TODO implement 
    }
    case classReferenceResponse: ClassReferenceMessage =>{
      //TODO implement
    }
  }

}