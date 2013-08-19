package de.tkip.sbpm.bir

import spray.json.RootJsonFormat
import akka.actor.ActorRef
import spray.json.JsValue
import spray.json.DefaultJsonProtocol

case class CreateBIHeader(userID: String, name: String, subjectName: String, content: String)

object BIRJsonProtocol extends DefaultJsonProtocol{
  
  implicit val createBIHeaderFormat = jsonFormat4(CreateBIHeader)

}