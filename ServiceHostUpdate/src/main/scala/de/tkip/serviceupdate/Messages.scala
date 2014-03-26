package de.tkip.serviceupdate


object Messages {
  case class UploadServiceToHost(host:String, port:String, serviceId:String, serviceClass: String, serviceJSON: String)
}
