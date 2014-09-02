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
import de.tkip.sbpm.persistence.DatabaseAccess
import spray.httpx.SprayJsonSupport
import scala.collection.mutable
import spray.json._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.InterfaceType._
import de.tkip.sbpm.model.GraphJsonProtocol._
import akka.event.Logging
import de.tkip.sbpm.persistence.query.{InterfaceQuery => Query}

object InterfaceActor {
  case object GetAllInterfaces
  case class GetInterface(id: Int)
  case class AddInterface(interface: Interface)
  case class DeleteInterface(interfaceId: Int)
  case class GetImplementations(subjectIds: Seq[String])
  case class GetBlackbox(subjectId: String, blackboxname: String)
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
      log.info("Get all interfaces")
      sender ! Query.loadInterfaces().map(addInterfaceImplementations)
    }

    case GetInterface(interfaceId) => {
      log.info(s"Get interface with id: $interfaceId")
      sender ! Query.loadInterface(interfaceId).map(addInterfaceImplementations)
    }

    case DeleteInterface(interfaceId) => {
      log.info(s"Delete interface with id: $interfaceId")
      Query.deleteInterfaceById(interfaceId)
    }

    case GetImplementations(subjectIds) => {
      log.info(s"Gathering list of implementations for subjects: $subjectIds")
      val implementationsMap = subjectIds.foldLeft(Map[String, Seq[InterfaceImplementation]]()){ (m, s) =>
        m + (s -> implementationsFor(s))
      }
      sender ! implementationsMap
    }

    case GetBlackbox(subjectId, blackboxname) => {
      log.info("GetBlackbox: " + subjectId + "/" + blackboxname)

      val listFuture: Seq[Interface] = Query.loadInterfaces()

      // TODO: move filter to Query
      val filtered = listFuture.filter(impl => (impl.interfaceType == BlackboxcontentInterfaceType && impl.graph.subjects.values.exists(subj => (subj.id == subjectId && subj.blackboxname == Some(blackboxname)))))

      sender ! filtered.map(addInterfaceImplementations).headOption // TODO: list or single one?
    }

    case AddInterface(interface) => {
      log.info(s"Adding new interface")
      val interfaceId = Query.saveInterface(interface)
      log.info(s"Interface adding completed. id: $interfaceId")
      sender ! Some(interfaceId.toString)
    }

    case Reset => {
      log.info("resetting...")
      DatabaseAccess.recreateDatabase()
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

  private def implementationsFor(subjectId: String) : Seq[InterfaceImplementation] = {
    logger.info("implementationsFor called for subjectId {}", subjectId)
    Query.findImplementations(subjectId)
  }
}
