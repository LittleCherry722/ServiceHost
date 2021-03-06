/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.rest

import java.sql.Timestamp
import java.util.Date

import GraphJsonProtocol.graphJsonFormat
import akka.actor.{ActorContext, ActorRef}
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.ProcessInstanceActor.{Agent, AgentAddress}
import de.tkip.sbpm.model._
import spray.json._
import spray.routing.authentication.UserPass

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
        case _                   => throw new DeserializationException("Date expected")
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
        case _                   => throw new DeserializationException("Date expected")
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
      case x                 => deserializationError("Expected Array as JsArray, but got " + x)
    }
  }

  /**
   * header case classes
   */
  // TODO name should not be optional
  case class ProcessIdHeader(name: Option[String], processId: Int)
  case class GraphHeader(name: String,
                         interfaceId: Option[Int],
                         publishInterface: Boolean,
                         graph: Option[Graph],
                         isCase: Boolean,
                         id: Option[Int] = None) {
    require(name.length() >= 3, "The name hast to contain 3 or more letters!")
    def toInterfaceHeader() (implicit context : ActorContext) : Option[InterfaceHeader] = {
      val port = SystemProperties.akkaRemotePort(context.system.settings.config)

      val containsBlackbox = if (graph.isDefined) {
         graph.get.subjects.values.exists(subj => (subj.subjectType == "external" && subj.externalType == Some("blackbox")))
      } else false
      val interfaceType = if (containsBlackbox) "blackboxcontent" else "interface"

      toInterfaceHeader(port, interfaceType)
    }

    def toInterfaceHeader(port: Int, interfaceType: String) = { // TODO: value
      if (!id.isDefined) System.err.println("id is None") // TODO: log!

      id.map { pId =>
        InterfaceHeader(
          interfaceType = interfaceType,
          name = name,
          interfaceId = interfaceId,
          graph = graph,
          port = port,
          processId = pId
        )
      }
    }
  }

  case class InterfaceHeader(interfaceType: String,
                             name: String,
                             interfaceId: Option[Int],
                             graph: Option[Graph],
                             port: Int,
                             processId: Int,
                             ip: Option[Int] = None)


  // administration
  implicit val userFormat = jsonFormat5(User)
  implicit val userUpdate = jsonFormat3(UserUpdate)
  implicit val providerMail = jsonFormat2(ProviderMail)
  implicit val userWithMail = jsonFormat5(UserWithMail)
  implicit val userIdentityFormat = jsonFormat4(UserIdentity)
  implicit val roleFormat = jsonFormat3(Role)
  implicit val groupFormat = jsonFormat3(Group)
  implicit val groupUserFormat = jsonFormat2(GroupUser)
  implicit val groupRoleFormat = jsonFormat2(GroupRole)
  implicit val password = jsonFormat2(SetPassword)

  // for message system
  import de.tkip.sbpm.model
  case class SendMessageHeader(toUser: UserID, title: String, content: String)
  implicit val messageFormat = jsonFormat7(model.Message)
  implicit val messageHeaderFormat = jsonFormat3(SendMessageHeader)

  // used for login
  implicit val userPassFormat = jsonFormat2(UserPass)

  // DomainModel
  implicit val domainProcessFormat = jsonFormat7(Process)
  //  implicit val actionFormat = jsonFormat2(Action)

  implicit val configFormat = jsonFormat4(Configuration)

  implicit val processInstanceInfoFormat = jsonFormat3(ProcessInstanceInfo)
  implicit val targetUserFormat = jsonFormat4(TargetUser)
  implicit val messageDataFormat = jsonFormat6(MessageData)
  implicit val agentAddressDataFormat = jsonFormat2(AgentAddress)
  implicit val agentDataFormat = jsonFormat3(Agent)
  implicit val actionDataFormat = jsonFormat10(ActionData)
  implicit val availableActionFormat = jsonFormat9(AvailableAction)
  implicit val processInstanceDataFormat = jsonFormat9(ProcessInstanceData)

  implicit val createProcessIdFormat = jsonFormat2(ProcessIdHeader)
  implicit def createGraphHeaderFormat(implicit roles: RoleMapper) = jsonFormat6(GraphHeader)
  implicit def createInterfaceHeaderFormat(implicit roles: RoleMapper) = jsonFormat7(InterfaceHeader)
  implicit val createActionIdHeaderFormat = jsonFormat8(ExecuteAction)

  implicit val newStateFormat = jsonFormat2(NewHistoryState)
  implicit val newHistoryProcessDataFormat = jsonFormat3(NewHistoryProcessData)
  implicit val newMessageFormat = jsonFormat5(NewHistoryMessage)
  implicit val newHistoryTransitionDataFormat = jsonFormat5(NewHistoryTransitionData)
  implicit val newHistoryEntryFormat = jsonFormat6(NewHistoryEntry)
  implicit val newHistoryFormat = jsonFormat1(NewHistory)

  implicit val processRelatedChangeDataFormat = jsonFormat6(ProcessRelatedChangeData)
  implicit val processRelatedDeleteDataFormat = jsonFormat1(ProcessRelatedDeleteData)
  implicit val processRelatedChangeFormat = jsonFormat3(ProcessRelatedChange)
  
  implicit val actionRelatedChangeDataFormat = jsonFormat9(ActionRelatedChangeData)
  implicit val actionRelatedDeleteDataFormat = jsonFormat1(ActionRelatedDeleteData)
  implicit val actionRelatedChangeFormat = jsonFormat3(ActionRelatedChange)
  
  implicit val historyRelatedChangeDataFormat = jsonFormat6(HistoryRelatedChangeData)
  implicit val historyRelatedChange = jsonFormat1(HistoryRelatedChange)
  
  implicit val processInstanceRelatedChangeDataFormat = jsonFormat4(ProcessInstanceRelatedChangeData)
  implicit val processInstanceRelatedDeleteDataFormat = jsonFormat1(ProcessInstanceRelatedDeleteData)
  implicit val processInstanceRelatedChangeFormat = jsonFormat3(ProcessInstanceRelatedChange)
  
  implicit val messageRelatedChangeDataFormat = jsonFormat7(MessageRelatedChangeData)
  implicit val messageRelatedChangeFormat = jsonFormat1(MessageRelatedChange)
  
  implicit val changeData = jsonFormat5(ChangeRelatedData)

}
