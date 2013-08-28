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
import java.util.Date
import de.tkip.sbpm.application.miscellaneous.AvailableActionsAnswer
import de.tkip.sbpm.application.miscellaneous.GetAvailableActions
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceInfo
import spray.routing.authentication.UserPass
import GraphJsonProtocol.graphJsonFormat
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceData
import de.tkip.sbpm.application.history._

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
  // TODO name should not be optional
  case class ProcessIdHeader(name: Option[String], processId: Int)
  case class GraphHeader(name: String, graph: Option[Graph], isCase: Boolean, id: Option[Int] = None){
    require(name.length() >= 3, "The name hast to contain 3 or more letters!")
  }

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
  
  // used for login
  implicit val userPassFormat = jsonFormat2(UserPass)

  // DomainModel
  implicit val domainProcessFormat = jsonFormat5(Process)
  implicit val actionFormat = jsonFormat2(Action)
  
  implicit val configFormat = jsonFormat4(Configuration)


  implicit val processInstanceInfoFormat = jsonFormat3(ProcessInstanceInfo)
  implicit val targetUserFormat = jsonFormat3(TargetUser)
  implicit val messageDataFormat = jsonFormat6(MessageData)
  implicit val actionDataFormat = jsonFormat8(ActionData)
  implicit val availableActionFormat = jsonFormat8(AvailableAction)
  implicit val processInstanceDataFormat = jsonFormat9(ProcessInstanceData)

  implicit val createProcessIdFormat = jsonFormat2(ProcessIdHeader)
  implicit def createGraphHeaderFormat(implicit roles: Map[String, Role]) = jsonFormat4(GraphHeader)
  implicit val createActionIdHeaderFormat = jsonFormat8(ExecuteAction)

  implicit val newStateFormat = jsonFormat2(NewHistoryState)
  implicit val newHistoryProcessDataFormat = jsonFormat3(NewHistoryProcessData)
  implicit val newMessageFormat = jsonFormat5(NewHistoryMessage)
  implicit val newHistoryTransitionDataFormat = jsonFormat5(NewHistoryTransitionData)
  implicit val newHistoryEntryFormat = jsonFormat6(NewHistoryEntry)
  implicit val newHistoryFormat = jsonFormat1(NewHistory)
  

}
