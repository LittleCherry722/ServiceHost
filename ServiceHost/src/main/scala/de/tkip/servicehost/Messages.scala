package de.tkip.servicehost

import akka.actor.ActorRef
import scala.reflect.ClassTag
import de.tkip.servicehost.serviceactor.ServiceActor

object Messages {
  case class RegisterServiceMessage(code: String, subjectJson: String)
  case class ExecuteServiceMessage(processID: String, serviceID: String, payload: Any)
  case class CreateXMLReferenceMessage(serviceID: String, classPath: String, jsonPath: String)
  case class GetClassReferenceMessage(serviceID: String)
  case class ClassReferenceMessage(serviceID: String, classReference: Class[_<: ServiceActor])
  case class ServiceResultMessage(result: Any)
  case class UpdateProcessData(userID: Int, processID: Int, remoteProcessID: Int, manager: Option[ActorRef])
  case class KillProcess(serviceID: String, processID: Int)
  // Service Messages
  case class AddService(id: String, className: String, packagePath: String)

  
  case class UpdateRepository(host: String, port: String)
  case class UploadService(serviceId: String, serviceClassName: String, serviceClasses: ServiceClasses, serviceJsonName: String, serviceJson: ServiceJson)
  
  type ServiceClasses = scala.collection.mutable.Map[String,Array[Byte]]
  type ServiceJson = Array[Byte]
}

