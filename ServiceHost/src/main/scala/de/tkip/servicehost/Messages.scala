package de.tkip.servicehost

import akka.actor.ActorRef
import scala.reflect.ClassTag
import de.tkip.servicehost.serviceactor.ServiceActor

object Messages {
  case class RegisterServiceMessage(code: String, subjectJson: String)
  case class ExecuteServiceMessage(processID: String, serviceID: String, payload: Any)
  case class DeploymentMessage(serviceID: String, sourceCode: String)
  case class CreateXMLReferenceMessage(serviceID: String, classPath: String)
  case class GetClassReferenceMessage(serviceID: String)
  case class ClassReferenceMessage(serviceID: String, classReference: Class[_<: ServiceActor])
  case class ServiceResultMessage(result: Any)
 
  // Service Messages
  case class AddService(n: Integer, sender: ActorRef)
  
  
}