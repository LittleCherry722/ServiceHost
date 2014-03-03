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

  case class GetImplementations(subjectId: String)

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

      sender ! filtered.toJson
    }

    case GetImplementations(subjectId) => {
      val implementations = implementationsFor(subjectId).toJson
      log.info("Gathering list of implementations for: {}: {}", subjectId, implementations.prettyPrint)

      sender ! implementations.toString
    }

    case AddInterface(ip, entry) => {
      log.info("adding new interface")
      val entryJs = entry.asJson.asJsObject
      val interface = convertEntry(entryJs, ip)
      val id = interface.id
      interfaces(id) = interface
      sender ! Some(id.toString)
    }

    case Reset => {
      log.info("resetting...")
      interfaces.clear()
    }
  }

  private def addInterfaceImplementations(interface: Interface) = {
    interface.copy(graph = interface.graph.copy(
      subjects = interface.graph.subjects.mapValues{
        subject => {
          logger.info("Searching for implementations for subject " + subject.name + " from " + interface.name)
          if (!(subject.subjectType == "external" && subject.externalType == Some("interface"))) {
            logger.info("Subject is not an interface subject, aborting. subject types: " + subject.subjectType + ", " + subject.externalType)
            subject
          } else {
            subject.copy(implementations = implementationsFor(subject.id))
          }
        }
      }
    ))
  }

  private def getAddress(ip: HttpIp, entry: JsObject) = {0
    val port = entry.fields("port")
    Address(ip.value, port.toString.toInt)
  }

  private def convertEntry(entry: JsObject, ip: HttpIp) = {
    var fields = entry.fields
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
    val someSId = Some(subjectId)
    val implementations: List[InterfaceImplementation] = interfaces.values.toList.flatMap(i => {
      i.graph.subjects.values.toList.filter(x => {
        val impl = (x.relatedSubjectId == someSId
          && (x.relatedInterfaceId.isDefined && interfaces.contains(x.relatedInterfaceId.get)
             || x.relatedInterfaceId.isEmpty))
        if (impl) {
          logger.info("Subject [" + i.name + "/" + x.name + "] is Implementation! ")
        }
        impl
      }).map(s => {
        val relatedInterface = if (s.relatedInterfaceId.isDefined) {
          interfaces(s.relatedInterfaceId.get)
        } else i
        InterfaceImplementation(
          processId = relatedInterface.processId,
          interfaceId = relatedInterface.id,
          address = i.address,
          subjectId = s.relatedSubjectId.get)
      })
    })
   implementations
  }

  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
