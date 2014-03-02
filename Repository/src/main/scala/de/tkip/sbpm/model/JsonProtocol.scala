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

package de.tkip.sbpm.model

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
import spray.routing.authentication.UserPass
import GraphJsonProtocol.graphJsonFormat
import de.tkip.sbpm.model.ProcessAttributes._

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


  implicit val addressFormat = jsonFormat2(Address)
  implicit object interfaceFormat extends RootJsonFormat[Interface] {
    def write(a: Interface) = JsObject(
      "id" -> JsNumber(a.id),
      "name" -> JsString(a.name),
      "graph" -> a.graph.toJson
    )
    def read(v: JsValue) = v.asJsObject.convertTo[Interface](jsonFormat(Interface,
      "address",
      "id",
      "name",
      "graph"))
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

}
