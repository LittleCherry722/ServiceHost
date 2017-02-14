package de.tkip.servicehost

import akka.actor.ActorRef
import de.tkip.sbpm.application.ProcessInstanceActor._
import de.tkip.sbpm.application.subject.behavior.Target
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.sbpm.application.miscellaneous.CreateServiceInstance
import de.tkip.servicehost.serviceactor.stubgen.{ReceiveState, ExitState, State}
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

  case class GetClassReferenceMessageBySubjectID(subjectId: SubjectID)

  case class GetClassReferenceMessageByProcessID(processId: Int)

  case class ClassReferenceMessage(subjectId: SubjectID, classReference: Class[_ <: ServiceActor])

  case class ServiceResultMessage(result: Any)

  case class UpdateProcessData(processInstanceID: ProcessInstanceID, processID: ProcessID, manager: ActorRef)

  case class UpdateServiceInstanceDate(agentsMap: AgentsMap, processInstanceIdentical: String, managerUrl: String)


  // Service Messages
  case class AddService(id: String, className: String, packagePath: String)

  // case class AskForProcessInstanceidentical(processInstanceID: ProcessInstanceID)
  case class AskForServiceInstance(processInstanceIdentical: String, targetSubjectId: SubjectID)

  case class UpdateRepository(host: String, port: String)

  case class ChangeState(id: Int)

  case object ServiceList

  case object AllService

  case class ServiceTerminate(processInstanceIdentical: String, serviceId: ServiceID, serviceName: String)

  case class StateManager(stateId: Int)

  case class ServiceListAnswer(serviceKey: Array[ServiceInstanceKey])

  case class ServiceNameToServiceIdMap(serviceName: String, serviceID: String)

  case class ServiceInstanceQuery(serviceName: String, serviceInstanceId: String)

  case object Info

  case class DeleteService (serviceID: Int)

  case class ServiceInstanceDetailedInfo(
                                          description: String,
                                          serviceName: String,
                                          serviceId: String,
                                          inputPool: Int,
                                          url: AgentAddress,
                                          relatedSubject: Option[List[String]],
                                          currentStates: List[State],
                                          messageInformation: List[Tuple4[String, String, String, String]]
                                          )

  case class UploadService(serviceId: String, serviceClassName: String, serviceClasses: ServiceClasses, serviceJsonName: String, serviceJson: ServiceJson)

  type ServiceClasses = scala.collection.mutable.Map[String, Array[Byte]]
  type ServiceJson = Array[Byte]

}

