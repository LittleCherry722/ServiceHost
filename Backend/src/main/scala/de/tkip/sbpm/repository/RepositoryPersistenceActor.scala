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

import scala.collection.immutable.{ Map, Set }
import de.tkip.sbpm.application.miscellaneous.{ RoleMapper, SystemProperties }
import de.tkip.sbpm.logging.DefaultLogging
import akka.actor.{ ActorRef, Props }
import akka.util._
import scala.concurrent.duration._
import spray.json._
import de.tkip.sbpm.rest.JsonProtocol.{GraphHeader, createInterfaceHeaderFormat}
import scalaj.http.{Http, HttpOptions}
import scala.concurrent.{ExecutionContext, Future}
import de.tkip.sbpm.persistence.query.Roles
import de.tkip.sbpm.instrumentation.InstrumentedActor
import ExecutionContext.Implicits.global
import de.tkip.sbpm.model.Role
import scala.concurrent.{Await}
import de.tkip.sbpm.ActorLocator
import akka.event.Logging
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.ProcessInstanceActor.{Agent, AgentAddress}

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
                             }
                             else {
                               val rolesFuture = Await.result((persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]], 2 seconds)
                               val rm = rolesFuture.map(r => (r.name, r)).toMap
                               RoleMapper.createRoleMapper(rm)
                             }

      val interface = gHeader.toInterfaceHeader().toJson.toString()
      log.debug("[SAVE INTERFACE] sending message to repository... " + repoLocation + "interfaces")
      log.debug("-------------------------------------------------------------")
      log.debug(interface)
      log.debug("-------------------------------------------------------------")
      val result = Http.postData(repoLocation + "interfaces", interface)
        .charset("UTF-8")
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .asString
      log.debug("[SAVE INTERFACE] repository says: " + result)
      sender !! Some(result.toInt)
    }

    case DeleteInterface(interfaceId) => {
      log.debug("[DELETE INTERFACE] delete message received")
      val result = Http(repoLocation + "interfaces/" + interfaceId)
        .method("DELETE")
        .charset("UTF-8")
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Charset", "UTF-8")
        .option(HttpOptions.readTimeout(10000))
        .responseCode
      log.debug("[SAVE INTERFACE] repository says: " + result)
      sender !! (None)
    }
    case GetAgentsMapMessage(externalSubjectIds) => {
      // Create a string of all external subjects to query the repository with
      val externalSubjectIdsString = externalSubjectIds.mkString("::")
      // and ask the repository for agents for all unknown external subjects
      val newAgentsString = Http(repoLocation + "implementations")
        .param("subjectIds", externalSubjectIdsString)
        .option(HttpOptions.readTimeout(10000))
        .asString
      log.info("received new agents mapping for subjectIds: {}", externalSubjectIds)
      log.info("String response: {}", newAgentsString)
      val newAgentsMap = newAgentsString.asJson.convertTo[Map[String, Set[Agent]]]
      log.info("JSON parsed mapping: {}", newAgentsMap)
      sender !! AgentsMappingResponse(newAgentsMap)
    }
    case _ =>
      log.debug("[INTERFACE PERSISTENCE ACTOR] invalid message received")
  }
}
