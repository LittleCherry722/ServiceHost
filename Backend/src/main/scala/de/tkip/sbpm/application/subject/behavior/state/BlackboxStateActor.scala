/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2014 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.subject.behavior.state

import scala.Array.canBuildFrom
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.mutable.{Map => MutableMap}

// TODO: sortieren / aufräumen
import akka.actor.Actor
import spray.json._
import DefaultJsonProtocol._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.util.{Success, Failure}
import akka.actor.actorRef2Scala
import scala.concurrent.ExecutionContext.Implicits.global;
import scalaj.http._
import scalaj.http.{HttpOptions, Http}
import de.tkip.sbpm.persistence.query._

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._

import de.tkip.sbpm.application.miscellaneous.{ RoleMapper, UnBlockUser }
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.model._
import de.tkip.sbpm.application.ProcessInstanceActor.{RegisterSubjects, AgentAddress, Agent}
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import akka.event.Logging

object BlackboxStateActor {
  private var current = 0
  private def blackboxInstance = {current += 1; current}
}

case class BlackboxStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  val mySubjectID: SubjectID = data.subjectData.subject.id
  val myMacroName: String = data.stateModel.blackboxname.get

  val url = "http://localhost:8181/repo/blackbox/" + mySubjectID + "/" + myMacroName

  val blackboxInstance = BlackboxStateActor.blackboxInstance

  private lazy val persistanceActor = ActorLocator.persistenceActor

  def loadRoles: Future[Seq[Role]] = {
    log.info("loadRoles: asking..")
    (persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]
  }

  val rolesFuture: Future[Seq[Role]] = loadRoles

  // TODO: move to Repo Actor?
  def loadPlaintextGraph(url: String): String = {
    log.info("loadPlaintextGraph: starting..")

    // TODO: Fehlerbehandlung?
    log.info("loadPlaintextGraph: fetch url: " + url)
    val plaintextGraph: HttpResponse[String] = Http(url).option(HttpOptions.connTimeout(500)).option(HttpOptions.readTimeout(1000)).asString

    log.info("loadPlaintextGraph: done")

    plaintextGraph.body

  }

  val plaintextGraph: String = loadPlaintextGraph(url)

  rolesFuture onComplete {
    case Success(res) => {
      log.info("rolesFuture Success")


      val rolesSeq: Seq[Role] = res.asInstanceOf[Seq[Role]]

      val roles: Map[String, Role] = rolesSeq.map(r => (r.name, r)).toMap

      implicit val roleMapper: RoleMapper = RoleMapper.createRoleMapper(roles)

      val interface: Interface = plaintextGraph.parseJson.convertTo[Interface]
      val reversedGraph: Graph = reverseExternalSubjects(interface.graph)
      val processGraph: ProcessGraph = de.tkip.sbpm.application.miscellaneous.parseGraph(reversedGraph)

      // TODO: check if own subject exists
      val subject: Subject = processGraph.subjects(mySubjectID).asInstanceOf[Subject]
      val mainMacro: Array[State] = subject.mainMacro.states

      // register all used subjects except the own
      val externalSubjects: Map[SubjectID, SubjectLike] = processGraph.subjects.filterNot(_._1 == mySubjectID)
      val externalGraphSubjects: Map[SubjectID, GraphSubject] = reversedGraph.subjects.filterNot(_._1 == mySubjectID)

      val agents: Map[SubjectID, Agent] = externalGraphSubjects.values.map(
        subj => {
          val impl: InterfaceImplementation = subj.implementations.get.head // TODO: check existence / how to choose?
          val agent = Agent(impl.processId, AgentAddress(impl.address.ip, impl.address.port), impl.subjectId)
          (subj.id, agent)
        }).toMap

      callMacro(mainMacro, externalSubjects, agents)
    }
    case Failure(e) => {
      e.printStackTrace()
      log.error("error loading roles", e)
      // TODO: weitere Fehlerbehandlung
      exit()
    }
  }

  // TODO: how is this done in frontend when implementing an interface?
  // TODO: move to Repo Actor?
  def reverseExternalSubjects(graph: Graph): Graph = {
    graph.copy(subjects = graph.subjects.map(
      e => (e._1, {
          val subject = e._2

          if (subject.subjectType == "external") {
            if (subject.externalType == Some("blackbox")) {
              log.info("reverseExternalSubjects: found blackbox, switch it to single subject")
              subject.copy(
                subjectType = "single",
                externalType = None
              )
            }
            else {
              log.error("reverseExternalSubjects: found external subject that is no blackbox - currently unsupported!")
              subject
            }
          }
          else {
            log.info("reverseExternalSubjects: found non-external subject, switch it to an external interface")
            subject.copy(
              subjectType = "external",
              externalType = Some("interface")
            )
          }
        })
    ))
  }

  def callMacro(mainMacro: Array[State], externalSubjects: Map[SubjectID, SubjectLike], agents: Map[SubjectID, Agent]): Unit = {
    val macroName = blackboxInstance + "@blackbox"
    log.info("=============================")
    log.info("=============================")
    log.info("callMacro: " + macroName + " => " + mainMacro.mkString(", "))
    log.info("=============================")
    log.info("=============================")

    val msg = RegisterSubjects(externalSubjects, agents)

    // TODO: include subject information in callmacro
    context.parent ! msg

    log.info("registered subjects")

    // TODO: BlockUser ?
    context.parent ! CallMacroStates(this.self, macroName, mainMacro)
    // TODO: UnBlockUser ?
  }

  override protected def getAvailableAction: Array[ActionData] = {
    Array()
  }

  protected def stateReceive = {
    case mt: MacroTerminated => {
      log.info("Macro terminated: " + mt.macroID)

      blockingHandlerActor ! UnBlockUser(userID)

      exit()
    }
  }

  def exit(): Unit = {
    log.info("exit")

    changeState(exitTransitions(0).successorID, data, null)
  }
}
