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

package de.tkip.sbpm.application.subject.behavior.state

import akka.actor.Status.Failure
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.{MessageName, SubjectID}
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{AgentsMappingResponse, GetAgentsMapMessage}
import akka.actor.{ActorLogging, ActorRef, actorRef2Scala}
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.subject.behavior.{Target, Transition, Variable}
import de.tkip.sbpm.application.subject.misc.{StartSubjectExecution, _}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

case class ChooseAgentStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  private lazy val repositoryPersistenceActor = ActorLocator.repositoryPersistenceActor

  println("[CHOOSE AGENT STATE ACTOR] Actor created.")

  protected def stateReceive = {
    case action: ExecuteAction =>
      println("[CHOOSE AGENT STATE ACTOR] Execute received.")
      val input = action.actionData
      val index = indexOfInput(input.text)
      if (index != -1) {
        val chooseAgentSubjectId = data.stateModel.chooseAgentSubject.getOrElse(subjectID)
        val agent = action.actionData.selectedAgent.get
        exitTransitions(0).storeVar match {
          case Some(storeVarId) =>
            val storeVar = variables.getOrElseUpdate(storeVarId, Variable(storeVarId))
            val proxyFuture = for {
              processInstanceManager <- (processInstanceActor ?? GetProcessInstanceManager).mapTo[ActorRef]
              processInstanceRef <- (processInstanceManager ?? GetProcessInstanceProxy(agent)).mapTo[ActorRef]
            } yield processInstanceRef
            blockingHandlerActor !! UnBlockUser(userID)
            val proxy = Await.result(proxyFuture, 5.seconds)
            val toChannel = Channel(proxy, agent)
            val target = Target(chooseAgentSubjectId, 1, 1, false, None, true, false)
            val dummyMessage = SubjectToSubjectMessage(-1, agent.processId, ProcessAttributes.ExternalUser,
              subjectID, toChannel, target, MessageName(""), EmptyContent)
            storeVar.addMessage(dummyMessage)
          case _ => ()
        }
        changeState(exitTransitions(index).successorID, data, null)
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
          "Invalid Argument: " + input.text + " is not a valid action."))
        receiver !! message
      }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    println("[CHOOSE AGENT STATE ACTOR] Asking for available actions.")
    val subjectId = data.stateModel.chooseAgentSubject.getOrElse(subjectID)

    val possibleAgents = getAgentsMap(subjectId)
    exitTransitions.map((t: Transition) => {
      t.subjectID
      ActionData(t.messageName.name, true, exitCondLabel, possibleAgents = Some(possibleAgents.toList))
    })
  }

  private def getAgentsMap(subjectId: SubjectID): Set[Agent] = {
    // Get the IDs of all external Subjects, then only take those for which we do not have
    // a agentMap, aka external subjects with unknown agents

    println("[CHOOSE AGENT STATE ACTOR] Asking for available agents.")
    val getAgentsMapMessage = GetAgentsMapMessage(Seq(subjectId))
    val newAgentsMapFuture = (repositoryPersistenceActor ?? getAgentsMapMessage).mapTo[AgentsMappingResponse]
    val newAgentsMap = Await.result(newAgentsMapFuture, 4 seconds)
    newAgentsMap.possibleAgents(subjectId).map { impl =>
      Agent(processId = impl.ownProcessId, address = impl.ownAddress, subjectId = impl.ownSubjectId)
    }
  }


  private def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- exitTransitions) {
      if (t.messageName.name == input) {
        return i
      }
      i += 1
    }
    -1
  }
}
