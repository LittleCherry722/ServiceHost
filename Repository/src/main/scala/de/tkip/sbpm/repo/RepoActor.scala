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
import DefaultJsonProtocol._
import spray.http.HttpIp

object RepoActor {
  case object GetAllImplementations

  case object GetOffers

  case class GetOffer(id: ID)

  case class GetImplementation(id: ID)

  case class GetOfferImplementations(offerId: ID)

  case class AddImplementation(ip: HttpIp, entry: String)

  case class AddOffer(ip: HttpIp, entry: String)

  case object Reset

  case class Offer(address: Address, id: ID, graph: JsObject)
  case class Implementation(address: Address,
                            id: ID,
                            offerId: ID,
                            fixedSubjectId: String,
                            interfaceSubjects: List[String],
                            graph: JsObject)
  case class Address(ip: String, port: Int)
  type ID = Int

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val addressFormat = jsonFormat2(Address)
    implicit val offerFormat = jsonFormat3(Offer)
    implicit val implementationFormat = jsonFormat6(Implementation)
  }
}

class RepoActor extends Actor with ActorLogging {
  import RepoActor.MyJsonProtocol._

  import RepoActor._

  val offers = mutable.Set[Offer]()
  val implementations = mutable.Map[ID, mutable.Set[Implementation]]()
  var currentId = 1

  def receive = {
    case GetAllImplementations => {
      val list = implementations.values.fold(mutable.Set.empty) { (a, b) => a ++ b }.toList

      log.info("entries: {}", list.map{_.graph}.toJson.prettyPrint)

      sender ! list.toJson.prettyPrint
    }

    case GetOfferImplementations(offerId) => {
      val list = implementations.get(offerId).toList.fold(mutable.Set.empty) { (a, b) => a ++ b }.map{_.graph}.toList

      log.info("entries: {}", list.toJson.prettyPrint)

      sender ! list.toJson.prettyPrint
    }

    case GetImplementation(implId) => {
      val list = implementations.values.fold(mutable.Set.empty) { (a, b) => a ++ b }.toList
      val filtered = list.find { impl => impl.id == implId }.map{_.graph}

      log.info("entries: {}", filtered.toJson.prettyPrint)

      sender ! filtered.toJson.prettyPrint
    }

    case AddImplementation(ip, entry) => {
      val id = nextId
      val entryJs = entry.asJson.asJsObject
      val offerId = entryJs.fields("offerId").convertTo[Int]
      val interfaceSubjects = entryJs.fields("interfaceSubjects").convertTo[List[String]]
      val fixedSubjectId = entryJs.fields("fixedSubjectId").toString()
      val ce = convertEntry(entryJs, id)
      val implementation = Implementation(getAddress(ip, entryJs), id, offerId, fixedSubjectId, interfaceSubjects, ce)
      implementations(offerId) = (implementations.getOrElse(offerId, mutable.Set[Implementation]()) += implementation)
      sender ! Some(implementation.graph.toJson.prettyPrint)
    }

    case GetOffers => {
      sender ! offers.toList.map{_.graph}.toJson.prettyPrint
    }

    case AddOffer(ip, entry) => {
      val id = nextId
      val entryJs = entry.asJson.asJsObject
      val ce = convertEntry(entryJs, id)
      val offer = Offer(getAddress(ip, entryJs), id, ce)
      offers.add(offer)
      sender ! Some(offer.graph.toJson.prettyPrint)
    }

    case Reset => {
      log.info("resetting...")
      implementations.clear()
      offers.clear()
    }
  }

  private def getAddress(ip: HttpIp, entry: JsObject) = {
    val port = entry.fields("port")
    Address(ip.value, port.toString.toInt)
  }

  private def convertEntry(entry: JsObject, id: Int) = {
    val processId = entry.fields("processId")
    val graph = entry.fields("graph").asJsObject
    val convertedGraph = convertGraph(id, graph, processId)

    var fields = entry.fields
    fields -= "port"
    fields -= "url"
    fields -= "id"
    fields += ("id" -> id.toJson)
    fields += ("date" -> System.currentTimeMillis.toJson)
    fields += ("graph" -> convertedGraph)

    entry.copy(fields)
  }

  private def convertGraph(id: Int, graph: JsObject, processId: JsValue) = {
    var fields = graph.fields
    fields -= "id"
    fields += ("id" -> nextId.toJson)
    graph.copy(fields)
  }

  private def nextId = {
    val id = currentId
    currentId += 1
    id
  }
}
