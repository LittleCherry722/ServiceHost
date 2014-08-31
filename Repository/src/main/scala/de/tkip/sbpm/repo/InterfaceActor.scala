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

package de.tkip.sbpm.repo

import akka.actor.{ActorLogging, Actor}
import spray.httpx.SprayJsonSupport
import scala.collection.mutable
import spray.json._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.InterfaceType._
import de.tkip.sbpm.model.GraphJsonProtocol._
import akka.event.Logging

object InterfaceActor {
  case object GetAllInterfaces
  case class GetInterface(id: Int)
  case class AddInterface(interface: Interface)
  case class DeleteInterface(interfaceId: Int)
  case class GetImplementations(subjectIds: Seq[String])
  case class GetBlackbox(subjectId: String, subjectName: String)
  case object Reset

  object MyJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit object interfaceTypeFormat extends RootJsonFormat[InterfaceType] {
      def write(obj: InterfaceType) = JsString(obj.toString)
      def read(v: JsValue) = v match {
        case JsString(str) => try {
          InterfaceType.withName(str)
        }
        case _ => deserializationError("String expected")
      }
    }

    implicit val InterfaceFormat = jsonFormat6(Interface)
    implicit val IntermediateInterfaceFormat = jsonFormat6(IntermediateInterface)

  }
}

class InterfaceActor extends Actor with ActorLogging {
  import InterfaceActor._
  import MyJsonProtocol._

  private val logger = Logging(context.system, this)
  private val interfaces = mutable.Map[Int, Interface]()

  def receive = {
    case GetAllInterfaces => {
      val list = interfaces.values.toList

      sender ! list.map(addInterfaceImplementations)
    }

    case GetInterface(implId) => {
      val list = interfaces.values.toList
      val filtered = list.find { impl => impl.id == implId }.map{_.graph}

      log.info("entries for id: {}", filtered.toJson)

      sender ! filtered
    }

    case DeleteInterface(interfaceId) => {
      interfaces.remove(interfaceId)
    }

    case GetImplementations(subjectIds) => {
      val implementationsMap = subjectIds.foldLeft(Map[String, Seq[InterfaceImplementation]]()){ (m, s) =>
        m + (s -> implementationsFor(s))
      }
      log.info("Gathering list of implementations for: {}", implementationsMap.toJson.prettyPrint)

      sender ! implementationsMap
    }

    case GetBlackbox(subjectId, subjectName) => {
      log.info("GetBlackbox: " + subjectId + "/" + subjectName)

      val list = interfaces.values.toList

      val filtered = list.filter(impl => (impl.interfaceType == BlackboxcontentInterfaceType && impl.graph.subjects.values.exists(subj => (subj.id == subjectId && subj.name == subjectName))))

      sender ! filtered.map(addInterfaceImplementations).headOption // TODO: list or single one?
    }

    case AddInterface(interface) => {
      log.info("adding new interface")
      val id = interface.id
      log.info("added new interface: {}", interface)
      interfaces(id) = interface
      sender ! Some(id.toString)
    }

    case Reset => {
      log.info("resetting...")
      interfaces.clear()
    }
  }

  private def addInterfaceImplementations(interface: Interface) : Interface = {
    log.info("addInterfaceImplementations called for interface {}", interface.id)
    interface.copy(graph = interface.graph.copy(
      subjects = interface.graph.subjects.mapValues{
        subject => {
          logger.info("Searching for implementations for subject " + subject.name + " from " + interface.name)
          if (subject.subjectType == "single") {
            logger.info("Subject is an single subject, copy its implementations")
            subject.copy(implementations = implementationsFor(subject.id))
          } else {
            logger.info("Subject is not an single subject, aborting. subject types: " + subject.subjectType + ", " + subject.externalType)
            subject
          }
        }
      }
    ))
  }

  private def implementationsFor(subjectId: String) : List[InterfaceImplementation] = {
    logger.info("implementationsFor called for subjectId {}", subjectId)
    val someSId = Some(subjectId)
    val implementations: Iterable[InterfaceImplementation] = interfaces.values.flatMap(i => {
      i.graph.subjects.values.toList.map(s => {
        logger.info("test if subject {} is implementation", s.id)
        if (s.id == subjectId && s.subjectType == "single") {
          val impl = InterfaceImplementation(
            processId = i.processId,
            address = i.address,
            subjectId = s.id)
          Some(impl)
        }
        else {
          None
        }
      }).flatten
    })
   implementations.toList
  }
}
