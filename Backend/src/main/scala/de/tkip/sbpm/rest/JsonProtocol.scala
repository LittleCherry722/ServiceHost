package de.tkip.sbpm.rest

import java.sql.Timestamp

import de.tkip.sbpm.model._
import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsValue
import spray.json.RootJsonFormat

/**
 * supplies the marshalling/unmarshalling process with the needed information about how to cast values
 */
object JsonProtocol extends DefaultJsonProtocol {

  /**
   * primitive formater
   */
  implicit object TimestampFormat extends RootJsonFormat[Timestamp] {
    def write(obj: Timestamp) = {
      JsObject("date" -> JsNumber(obj.getTime))
    }
    def read(json: JsValue) = {
      json.asJsObject().getFields("date") match {
        case Seq(JsNumber(time)) => new Timestamp(time.toLong)
        case _ => throw new DeserializationException("Date expected")
      }
    }
  }

  /**
   * case class formater
   */
  implicit val envelopeFormat = jsonFormat2(Envelope)

  implicit val configurationFormat = jsonFormat4(Configuration)
  implicit val userFormat = jsonFormat4(User)
  implicit val roleFormat = jsonFormat3(Role)
  implicit val groupFormat = jsonFormat3(Group)

  implicit val graphFormat = jsonFormat4(Graph)
  implicit val ProcessFormat = jsonFormat5(Process)
  implicit val processInstanceFormat = jsonFormat5(ProcessInstance)
  
}
  
