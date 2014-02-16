import akka.actor.Actor
import Messages.DeploymentMessage

class DeploymentActor extends Actor {

  def receive = {
    case deploy: DeploymentMessage => {
      //TODO implement deployment 
    }
  }
  
  def addClassRefToXML(){
    // TODO implement writing to xml file
  }
  
  def createClassFile(source: String, path: String): Boolean ={
    //TODO implement. Return true if class creation successful
    false
  }
}