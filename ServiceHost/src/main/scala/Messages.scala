object Messages {
  case class RegisterServiceMessage(code: String, subjectJson: String)
  case class ExecuteServiceMessage(processID: String, serviceID: String, payload: Any)
  case class DeploymentMessage(serviceID: String, sourceCode: String)
  case class CreateXMLReferenceMessage(serviceID: String, classPath: String)
  case class GetClassReferenceMessage(serviceID: String)
  case class ClassReferenceMessage(serviceID: String, classReference: String)
  case class ServiceResultMessage(result: Any)
  
}
