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
import spray.json._
import java.util.Date
import GraphJsonProtocol.graphJsonFormat
import de.tkip.sbpm.repo.InterfaceActor.MyJsonProtocol.interfaceTypeFormat

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
      JsObject("date" -> JsNumber(obj.getTime))
    }
    def read(json: JsValue) = {
      json.asJsObject().getFields("date") match {
        case Seq(JsNumber(date)) => new Date(date.toLong)
        case _                   => throw new DeserializationException("Date expected")
      }
    }
  }

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


  implicit val addressFormat = jsonFormat3(de.tkip.sbpm.model.Address)
  implicit object interfaceFormat extends RootJsonFormat[Interface] {
    def write(a: Interface) = JsObject(
      "interfaceType" -> a.interfaceType.toJson,
      "id" -> a.id.toJson,
      "processId" -> JsNumber(a.processId),
      "name" -> JsString(a.name),
      "graph" -> a.graph.toJson
    )
    def read(v: JsValue) = v.asJsObject.convertTo[Interface](jsonFormat(Interface,
      "interfaceType",
      "address",
      "id",
      "processId",
      "name",
      "graph"))
  }


}
