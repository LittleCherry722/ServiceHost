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
import scala.collection.mutable
import spray.json._
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.GraphJsonProtocol._
import spray.http.HttpIp
import akka.event.Logging

object RepoActor {

  case object GetAllInterfaces

  case class GetInterface(id: Int)

  case class AddInterface(ip: HttpIp, entry: String)

  case class GetImplementations(subjectIds: Seq[String])

  case object Reset

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val interfaceFormat = jsonFormat5(Interface)
  }
}

class RepoActor extends Actor with ActorLogging {
  import RepoActor.MyJsonProtocol._

  import RepoActor._

  private val logger = Logging(context.system, this)

  val interfaces = mutable.Map[Int, Interface]()
  var currentId = 1

  def receive = {
    case GetAllInterfaces => {
      val list = interfaces.values.toList

      sender ! list.map{addInterfaceImplementations(_)}.toJson.toString
    }

    case GetInterface(implId) => {
      val list = interfaces.values.toList
      val filtered = list.find { impl => impl.id == implId }.map{_.graph}

      log.info("entries for id: {}", filtered.toJson.toString)

      sender ! filtered.toJson.toString
    }

    case GetImplementations(subjectIds) => {
      val implementationsMap = subjectIds.foldLeft(Map[String, Seq[InterfaceImplementation]]()){ (m, s) =>
        m + (s -> implementationsFor(s))
      }
      log.info("Gathering list of implementations for: {}", implementationsMap.toJson.prettyPrint)

      sender ! implementationsMap.toJson.toString
    }

    case AddInterface(ip, entry) => {
      log.info("adding new interface")
      val entryJs = entry.asJson.asJsObject
      val interface = convertEntry(entryJs, ip)
      val id = interface.id
      log.info("added new interface: {}", interface)
      interfaces(id) = interface
      sender ! Some(id.toString)
    }

    case Reset => {
      log.info("resetting...")
      interfaces.clear()
      currentId = 1
    }
  }

  private def addInterfaceImplementations(interface: Interface) = {
    log.info("addInterfaceImplementations called for interface {}", interface.id)
    interface.copy(graph = interface.graph.copy(
      subjects = interface.graph.subjects.mapValues{
        subject => {
          logger.info("Searching for implementations for subject " + subject.name + " from " + interface.name)
          if (!(subject.subjectType == "external" && subject.externalType == Some("interface"))) {
            logger.info("Subject is not an interface subject, aborting. subject types: " + subject.subjectType + ", " + subject.externalType)
            subject
          } else {
            logger.info("Subject is an interface subject, copy its implementations")
            subject.copy(implementations = implementationsFor(subject.id))
          }
        }
      }
    ))
  }

  private def getAddress(ip: HttpIp, entry: JsObject) = {
    val port = entry.fields("port")
    Address(ip.value, port.toString.toInt)
  }

  private def convertEntry(entry: JsObject, ip: HttpIp) = {
    val fields = entry.fields
    val oldId = fields("id").toString.toInt
    val graph = fields("graph").convertTo[Graph]
    val id = fields.getOrElse[JsValue]("interfaceId", nextId.toJson).convertTo[Int]
    val name = fields.getOrElse[JsValue]("name", "".toJson).convertTo[String]

    new Interface(id = id,
                  name = name,
                  graph = graph,
                  processId = oldId,
                  address = getAddress(ip, entry))
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


  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
