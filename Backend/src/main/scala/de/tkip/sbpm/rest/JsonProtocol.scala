package de.tkip.sbpm.rest

import akka.actor._
import java.sql.Timestamp
import de.tkip.sbpm.model._
import spray.json.DefaultJsonProtocol
import spray.json.DeserializationException
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsValue
import spray.json.RootJsonFormat
import spray.json._
import de.tkip.sbpm.application.History
import de.tkip.sbpm.application.history.Entry
import de.tkip.sbpm.application.history.State
import de.tkip.sbpm.application.history.Message
import de.tkip.sbpm.application.history.MessagePayloadLink
import java.util.Date
import de.tkip.sbpm.application.miscellaneous.AvailableActionsAnswer
import de.tkip.sbpm.application.miscellaneous.GetAvailableActions
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceInfo
import spray.routing.authentication.UserPass
import de.tkip.sbpm.application.subject.ActionData

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

  implicit object DateFormat extends RootJsonFormat[Date] {
    def write(obj: Date) = {
      JsObject("date" -> JsNumber(obj.getTime()))
    }
    def read(json: JsValue) = {
      json.asJsObject().getFields("date") match {
        case Seq(JsNumber(date)) => new Date(date.toLong)
        case _ => throw new DeserializationException("Date expected")
      }
    }
  }

  //  TODO so richtig durchgereicht
  implicit object RefFormat extends RootJsonFormat[ActorRef] {
    def write(obj: ActorRef) = obj.toJson
    def read(json: JsValue) = json.convertTo[ActorRef]
  }

  implicit object ValueFormat extends RootJsonFormat[de.tkip.sbpm.model.StateType.StateType] {
    def write(obj: de.tkip.sbpm.model.StateType.StateType) = obj.toJson
    def read(json: JsValue) = json.convertTo[de.tkip.sbpm.model.StateType.StateType]
  }

  implicit def bufferFormat[T: JsonFormat] = new RootJsonFormat[scala.collection.mutable.Buffer[T]] {
    def write(array: scala.collection.mutable.Buffer[T]) = JsArray(array.map(_.toJson).toList)
    def read(value: JsValue) = value match {
      case JsArray(elements) => scala.collection.mutable.Buffer[T]() ++ elements.map(_.convertTo[T])
      case x => deserializationError("Expected Array as JsArray, but got " + x)
    }
  }

  /**
   * header case classes
   */
  case class ProcessIdHeader(processId: Int)
  case class GraphHeader(name: String, graph: String, isCase: Boolean){
    require(name.length() >= 3, "The name hast to contain 3 or more letters!")
  }

  /**
   * case class formater
   */
  implicit val envelopeFormat = jsonFormat2(Envelope)

  // administration
  implicit val configurationFormat = jsonFormat4(Configuration)
  implicit val userFormat = jsonFormat4(User)
  implicit val roleFormat = jsonFormat3(Role)
  implicit val groupFormat = jsonFormat3(Group)
  implicit val groupUserFormat = jsonFormat3(GroupUser)
  implicit val groupRoleFormat = jsonFormat3(GroupRole)
  // used for login
  implicit val userPassFormat = jsonFormat2(UserPass)

  // DomainModel
  implicit val domainGraphFormat = jsonFormat4(Graph)
  implicit val domainProcessFormat = jsonFormat5(Process)
  implicit val actionFormat = jsonFormat2(Action)

  // history
  implicit val stateFormat = jsonFormat2(State)
  implicit val messagePayloadFormat = jsonFormat2(MessagePayloadLink)
  implicit val messageFormat = jsonFormat6(Message)
  implicit val entryFormat = jsonFormat5(Entry)
  implicit val historyFormat = jsonFormat5(History)

  // action execution
  implicit val processInstanceInfoFormat = jsonFormat2(ProcessInstanceInfo)
  implicit val actionDataFormat = jsonFormat4(ActionData)
  implicit val availableActionFormat = jsonFormat8(AvailableAction)

  implicit val createProcessIdFormat = jsonFormat1(ProcessIdHeader)
  implicit val createGraphHeaderFormat = jsonFormat3(GraphHeader)
  implicit val createActionIdHeaderFormat = jsonFormat7(ExecuteAction)
}
