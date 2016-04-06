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

package de.tkip.sbpm.repository

import akka.event.Logging
import akka.util._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.subject.misc.{Agent, AgentAddress}
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.miscellaneous.{SystemProperties, RoleMapper}
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model.{InterfaceImplementation, Role}
import de.tkip.sbpm.persistence.query.Roles
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.application.miscellaneous.SystemProperties._
import spray.json._

import scala.collection.immutable.{Map, Set}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try
import scalaj.http.{Http, HttpOptions}

object RepositoryPersistenceActor {
  case class SaveInterface(json: GraphHeader, roles: Option[RoleMapper] = None)
  case class DeleteInterface(interfaceId: Int)
  case class GetAgentsMapMessage(agentIds: Iterable[SubjectID])
  case class AgentsMappingResponse(possibleAgents: Map[SubjectID, Set[InterfaceImplementation]])

  case class InterfaceSaveResult(id: Int, outgoingSubjectMap: Map[SubjectID, SubjectID], incomingSubjectMap: Map[SubjectID, SubjectID])
  implicit val interfaceSaveResultFormat = jsonFormat3(InterfaceSaveResult)
}

class RepositoryPersistenceActor extends InstrumentedActor {
  import RepositoryPersistenceActor._

  implicit val config = context.system.settings.config
  private val logger = Logging(context.system, this)
  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  private val repoLocation = configString("repo.address")
  private lazy val persistenceActor = ActorLocator.persistenceActor


  implicit val timeout = Timeout(1 seconds)

  def actorRefFactory = context

  def wrappedReceive = {
    case SaveInterface(gHeader, roles) =>
      log.info(s"[SAVE INTERFACE] saving interfaces. interface header: ${gHeader.toInterfaceHeader}")
      implicit val roleMap: RoleMapper = if (roles.isDefined) {
        roles.get
      } else {
        val rolesFuture = Await.result((persistenceActor ?? Roles.Read.All).mapTo[Seq[Role]], 2 seconds)
        val rm = rolesFuture.map(r => (r.name, r)).toMap
        RoleMapper.createRoleMapper(rm)
      }
      gHeader.interfaceId.foreach(deleteInterface) // delete old interface
      gHeader.implementationIds.foreach(deleteImplementation) // delete old implementations

      val resp = gHeader.toInterfaceHeader.right.flatMap(saveInterface)
      val iIdOption: Option[Int] = resp.right.toOption.flatMap(_.map(_.id))
      val port = akkaRemotePort

      val implementationIds: Seq[Int] = gHeader.graph.map { g =>
        val subjects = g.subjects.values.toSeq
        val viewIds = subjects.flatMap(_.implementsViews).flatten
        viewIds.flatMap { vId =>
          val address = AgentAddress("", port)
          val imp = InterfaceImplementation(
            viewId = vId,
            dependsOnInterface = iIdOption,
            ownAddress = address,
            ownProcessId = gHeader.id.get,
            ownSubjectId = "")
          saveImplementation(imp)
        }
      }.toSeq.flatten
      sender !! resp

    case DeleteInterface(interfaceId) =>
      log.debug("[DELETE INTERFACE] delete message received")
      val result = deleteInterface(interfaceId)
      log.debug("[SAVE INTERFACE] repository says: " + result)

    case GetAgentsMapMessage(externalSubjectIds) =>
      // Create a string of all external subjects to query the repository with
      val externalSubjectIdsString = externalSubjectIds.mkString("::")
      // and ask the repository for agents for all unknown external subjects
      val newAgentsString = Http(repoLocation + "implementations")
        .param("subjectIds", externalSubjectIdsString)
        .option(HttpOptions.readTimeout(10000))
        .asString
      log.info("received new agents mapping for subjectIds: {}", externalSubjectIds)
      log.info("String response: {}", newAgentsString)
      val newAgentsMap = newAgentsString.body.parseJson.convertTo[Map[String, Set[InterfaceImplementation]]]
      log.info("JSON parsed mapping: {}", newAgentsMap)
      sender !! AgentsMappingResponse(newAgentsMap)
    case _ => log.debug("[INTERFACE PERSISTENCE ACTOR] invalid message received")
  }

  private def saveInterface(interfaceHeader: InterfaceHeader): Either[Seq[String], Option[InterfaceSaveResult]] = {
    if (interfaceHeader.views.isEmpty) {
      Right(None)
    } else {
      val interfaceString = interfaceHeader.toJson.prettyPrint
      log.debug("[SAVE INTERFACE] sending message to repository... " + repoLocation + "interfaces")
      log.debug("-------------------------------------------------------------")
      log.debug(interfaceString)
      log.debug("-------------------------------------------------------------")
      val tResult = Try(Http(repoLocation + "interfaces")
        .postData(interfaceString)
        .method("POST")
        .charset("UTF-8")
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString)
      tResult.toOption.toRight(Seq("Error while transmitting interfaces to repository.")).right.flatMap { result =>
        if (result.isSuccess) {
          Right(result.body.parseJson.convertTo[Option[InterfaceSaveResult]])
        } else {
          Left(Seq(s"Error while transmitting interfaces to repository. Result: $result"))
        }
      }
    }
  }

  private def saveImplementation(interfaceImplementation: InterfaceImplementation): Option[Int] = {
    val implementationString = interfaceImplementation.toJson.prettyPrint
    log.debug("[SAVE INTERFACE] sending message to repository... " + repoLocation + "implementations")
    log.debug("-------------------------------------------------------------")
    log.debug(implementationString)
    log.debug("-------------------------------------------------------------")
    val tResult = Try(Http(repoLocation + "implementations")
      .postData(implementationString)
      .method("POST")
      .charset("UTF-8")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))
      .asString)
    tResult.toOption.toRight(Seq("Error while transmitting implementation to repository.")).right.toOption.flatMap { result =>
      if (result.isSuccess) {
        Some(result.body.parseJson.convertTo[Int])
      } else {
        None
      }
    }
  }

  private def deleteImplementation(implementationId: Int): Int = {
    Http(repoLocation + "implementations/" + implementationId)
      .method("DELETE")
      .charset("UTF-8")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))
      .asString
      .code
  }

  private def deleteInterface(interfaceId: Int): Int = {
    Http(repoLocation + "interfaces/" + interfaceId)
      .method("DELETE")
      .charset("UTF-8")
      .header("Content-Type", "application/json; charset=UTF-8")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))
      .asString
      .code
  }
}
