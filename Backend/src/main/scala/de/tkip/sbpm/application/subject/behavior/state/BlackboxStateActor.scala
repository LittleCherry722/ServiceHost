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
import de.tkip.sbpm.application.subject.misc.{ActionData, SubjectToSubjectMessage}
import akka.actor.Actor
import spray.json._
import DefaultJsonProtocol._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import scala.util.{Success, Failure}
import akka.actor.actorRef2Scala
import scala.concurrent.ExecutionContext.Implicits.global;

import scalaj.http.Http
import de.tkip.sbpm.persistence.query._

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._

import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.timeoutLabel
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.SubjectID
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.AvailableAction
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.subject.misc.ActionIDProvider
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.subject.behavior._
import de.tkip.sbpm.application.ProcessInstanceActor.{MappingInfo, AgentsMap, RegisterSubjects}
import de.tkip.sbpm.application.subject.CallMacroStates
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.miscellaneous.UnBlockUser
import akka.event.Logging

object BlackboxStateActor {
  private var current = 0
  private def blackboxInstance = {current += 1; current}
}

case class BlackboxStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  val mySubjectID: SubjectID = data.subjectData.subject.id

  val blackboxInstance = BlackboxStateActor.blackboxInstance

  private lazy val persistanceActor = ActorLocator.persistenceActor

  def loadRoles: Future[Seq[Role]] = {
    log.info("loadRoles: asking..")
    (persistanceActor ?? Roles.Read.All).mapTo[Seq[Role]]
  }

  val rolesFuture: Future[Seq[Role]] = loadRoles

  protected def extractUrl: String = {
    // TODO: prüfen, ob es eine valide url ist?
    val msg: SubjectToSubjectMessage = variables("$blackboxurl").messages.last
    val url: String = msg.messageContent
    log.info("extractUrl: " + url)
    url
  }

  // TODO: move to Repo Actor?
  def loadPlaintextGraph: String = {
    log.info("loadPlaintextGraph: starting..")
    val url: String = extractUrl

    // TODO: Fehlerbehandlung?
    log.info("loadPlaintextGraph: fetch url: " + url)
    val plaintextGraph: String = Http(url).asString

    log.info("loadPlaintextGraph: done")

    plaintextGraph
  }

  val plaintextGraph: String = loadPlaintextGraph

  rolesFuture onComplete {
    case Success(res) => {
      log.info("rolesFuture Success")


      val rolesSeq: Seq[Role] = res.asInstanceOf[Seq[Role]]

      implicit val roles: Map[String, Role] = rolesSeq.map(r => (r.name, r)).toMap

      val interface: Interface = plaintextGraph.parseJson.convertTo[Interface]
      val reversedGraph: Graph = reverseExternalSubjects(interface.graph)
      val processGraph: ProcessGraph = de.tkip.sbpm.application.miscellaneous.parseGraph(reversedGraph)

      // TODO: check if own subject exists
      val subject: Subject = processGraph.subjects(mySubjectID).asInstanceOf[Subject]
      val mainMacro: Array[State] = subject.mainMacro.states

      // register all used subjects except the own
      val externalSubjects: Map[SubjectID, SubjectLike] = processGraph.subjects.filterNot(_._1 == mySubjectID)
      val externalGraphSubjects: Map[SubjectID, GraphSubject] = reversedGraph.subjects.filterNot(_._1 == mySubjectID)

      val agents: Map[SubjectID, Set[Agent]] = externalGraphSubjects.values.map(
        subj => {
          val impl: InterfaceImplementation = subj.implementations.get.head // TODO: check existence / how to choose?
          val agent = Agent(impl.processId, AgentAddress(impl.address.ip, impl.address.port), impl.subjectId)
          (subj.id, Set(agent))
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

  def callMacro(mainMacro: Array[State], externalSubjects: Map[SubjectID, SubjectLike], agents: Map[SubjectID, Set[Agent]]): Unit = {
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
