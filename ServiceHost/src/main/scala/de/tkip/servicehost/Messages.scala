package de.tkip.servicehost

import akka.actor.ActorRef
import scala.reflect.ClassTag

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.ServiceActor

object Messages {
  case class GenerateService(path: String)

  case class RegisterServiceMessage(code: String, subjectJson: String)
  case class ExecuteServiceMessage(processID: ProcessID, serviceID: ServiceID, payload: Any)
  case class CreateXMLReferenceMessage(subjectId: SubjectID, classPath: String, jsonPath: String)
  case class GetAllClassReferencesMessage()
  case class GetClassReferenceMessage(subjectId: SubjectID)
  case class ClassReferenceMessage(subjectId: SubjectID, classReference: Class[_<: ServiceActor])
  case class ServiceResultMessage(result: Any)
  case class UpdateProcessData(processInstanceID: ProcessInstanceID, remoteProcessID: ProcessInstanceID, manager: Option[ActorRef])
  case class KillProcess(serviceID: ServiceID, processInstanceID: ProcessInstanceID)
  // Service Messages
  case class AddService(id: String, className: String, packagePath: String)
  case class AskForProcessInstanceidentical(key: ProcessInstanceKey)

  
  case class UpdateRepository(host: String, port: String)
  case class UploadService(serviceId: String, serviceClassName: String, serviceClasses: ServiceClasses, serviceJsonName: String, serviceJson: ServiceJson)
  
  type ServiceClasses = scala.collection.mutable.Map[String,Array[Byte]]
  type ServiceJson = Array[Byte]
}

