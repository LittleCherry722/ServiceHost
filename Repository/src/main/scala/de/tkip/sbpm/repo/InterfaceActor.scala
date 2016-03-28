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

import akka.actor.{Actor, ActorLogging}
import akka.event.Logging
import de.tkip.sbpm.model.GraphJsonProtocol._
import de.tkip.sbpm.model.InterfaceType._
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.DatabaseAccess
import de.tkip.sbpm.persistence.query.InterfaceQuery.IdResult
import de.tkip.sbpm.persistence.query.{InterfaceQuery => Query}
import spray.httpx.SprayJsonSupport
import spray.json._

object InterfaceActor {
  case object GetAllInterfaces
  case class GetInterface(id: Int)
  case class AddInterface(interface: Interface)
  case class AddImplementation(implementation: InterfaceImplementation)
  case class DeleteImplementation(implementationId: Int)
  case class DeleteInterface(interfaceId: Int)
  case class GetImplementations(subjectIds: Seq[String])
  case class GetBlackbox(subjectId: String, blackboxname: String)
  case object Reset

  object MyJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
    implicit object interfaceTypeFormat extends RootJsonFormat[InterfaceType] {
      def write(obj: InterfaceType) = JsString(obj.toString)
      def read(v: JsValue) = v match {
        case JsString(str) => InterfaceType.withName(str)
        case _ => deserializationError("String expected")
      }
    }

    implicit val InterfaceFormat = jsonFormat6(Interface)
    implicit val IntermediateInterfaceFormat = jsonFormat6(IntermediateInterface)

  }
}

class InterfaceActor extends Actor with ActorLogging {
  import InterfaceActor._

  private val logger = Logging(context.system, this)
//  private val interfaces = mutable.Map[Int, Interface]()

  def receive = {
    case GetAllInterfaces => {
      log.info("Get all interfaces")
      sender ! Query.loadInterfaces() //.map(withInterfaceImplementations)
    }

    case GetInterface(interfaceId) => {
      log.info(s"Get interface with id: $interfaceId")
      sender ! Query.loadInterface(interfaceId) //.map(withInterfaceImplementations)
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

    case AddInterface(interface) => {
      log.info(s"Adding new interface")
      val interfaceSaveResult = Query.saveInterface(interface)
      log.info(s"Interface adding completed. Result: $interfaceSaveResult")
      sender ! Some(interfaceSaveResult)
    }

    case AddImplementation(implementation) => {
      val implementationId = Query.saveImplementation(implementation)
      sender ! Some(IdResult(implementationId))
    }

    case DeleteImplementation(implementationId) => {
      Query.deleteImplementation(implementationId)
    }

    case Reset => {
      log.info("resetting...")
      DatabaseAccess.recreateDatabase()
      log.info("successfully resetted")
    }
  }


  private def implementationsFor(subjectId: String) : Seq[InterfaceImplementation] = {
    logger.info("implementationsFor called for subjectId {}", subjectId)
    Query.findImplementations(subjectId)
  }
}
