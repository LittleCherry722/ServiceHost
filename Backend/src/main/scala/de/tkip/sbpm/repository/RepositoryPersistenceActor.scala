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
import de.tkip.sbpm.application.ProcessInstanceActor.Agent
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.miscellaneous.RoleMapper
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model.Role
import de.tkip.sbpm.persistence.query.Roles
import de.tkip.sbpm.rest.JsonProtocol.{GraphHeader, createInterfaceHeaderFormat}
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
  case class AgentsMappingResponse(possibleAgents: Map[String, Set[Agent]])
}

class RepositoryPersistenceActor extends InstrumentedActor {
  import RepositoryPersistenceActor._
  import de.tkip.sbpm.repository.RepositoryJsonProtocol._

  private val logger = Logging(context.system, this)
  // akka config prefix
  protected val configPath = "sbpm."

  // read string from akka config
  protected def configString(key: String) =
    context.system.settings.config.getString(configPath + key)

  private val repoLocation = configString("repo.address")
  private lazy val persistanceActor = ActorLocator.persistenceActor


  implicit val timeout = Timeout(1 seconds)

  def actorRefFactory = context

  def wrappedReceive = {
    case SaveInterface(gHeader, roles) => {
      log.debug("[SAVE INTERFACE] save message received")

      implicit val roleMap: RoleMapper = if (roles.isDefined) {
        roles.get
      } else {
        val rolesFuture = Await.result((persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]], 2 seconds)
        val rm = rolesFuture.map(r => (r.name, r)).toMap
        RoleMapper.createRoleMapper(rm)
      }

      val resp = gHeader.toInterfaceHeader().right.flatMap { interface =>
        val interfaceString = interface.toJson.toString()
        log.debug("[SAVE INTERFACE] sending message to repository... " + repoLocation + "interfaces")
        log.debug("-------------------------------------------------------------")
        log.debug(interfaceString)
        log.debug("-------------------------------------------------------------")
        val tResult = Try(Http(repoLocation + "interfaces")
          .postData(interfaceString)
          .charset("UTF-8")
          .header("Content-Type", "application/json; charset=UTF-8")
          .header("Charset", "UTF-8")
          .option(HttpOptions.readTimeout(10000))
          .asString)
        tResult.toOption.toRight(Seq("Error while transmitting interfaces to repository.")).right.flatMap { result =>
          if (result.isSuccess) {
            Right(result.body.toInt)
          } else {
            Left(Seq("Error while transmitting interfaces to repository."))
          }
        }
      }
      log.debug("[SAVE INTERFACE] repository says: " + resp)
      sender !! resp
    }

    case DeleteInterface(interfaceId) => {
      log.debug("[DELETE INTERFACE] delete message received")
      val result = Http(repoLocation + "interfaces/" + interfaceId)
        .method("DELETE")
        .charset("UTF-8")
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString
        .code
      log.debug("[SAVE INTERFACE] repository says: " + result)
      sender !! None
    }
    case GetAgentsMapMessage(externalSubjectIds) => {
      // Create a string of all external subjects to query the repository with
      val externalSubjectIdsString = externalSubjectIds.mkString("::")
      // and ask the repository for agents for all unknown external subjects
      val newAgentsString = Http(repoLocation + "implementations")
        .param("subjectIds", externalSubjectIdsString)
        .option(HttpOptions.readTimeout(10000))
        .asString.body
      log.info("received new agents mapping for subjectIds: {}", externalSubjectIds)
      log.info("String response: {}", newAgentsString)
      val newAgentsMap = newAgentsString.parseJson.convertTo[Map[String, Set[Agent]]]
      log.info("JSON parsed mapping: {}", newAgentsMap)
      sender !! AgentsMappingResponse(newAgentsMap)
    }
    case _ =>
      log.debug("[INTERFACE PERSISTENCE ACTOR] invalid message received")
  }
}
