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
import scala.concurrent.duration.DurationInt
import akka.actor.{ActorLogging, actorRef2Scala}
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.subject.behavior.{Transition}
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.Agent

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

case class ChooseAgentStateActor(data: StateData)
  extends BehaviorStateActor(data) {
  protected def stateReceive = {

    case action: ExecuteAction => {
      val input = action.actionData
      val index = indexOfInput(input.text)
      if (index != -1) {
        changeState(exitTransitions(index).successorID, data, null)
        val sid = data.stateModel.chooseAgentSubject.getOrElse(subjectID)
        processInstanceActor ! SetAgentForSubject(sid, action.actionData.selectedAgent.get)
        blockingHandlerActor ! ActionExecuted(action)
      } else {
        val receiver = action.asInstanceOf[AnswerAbleMessage].sender
        val message = Failure(new IllegalArgumentException(
          "Invalid Argument: " + input.text + " is not a valid action."))
        receiver !! message
      }
    }
  }

  override protected def getAvailableAction: Array[ActionData] = {
    val sid = data.stateModel.chooseAgentSubject.getOrElse(subjectID)
    val possibleAgentsFuture = (processInstanceActor ?? GetAgentsListForSubject(sid)).mapTo[Set[Agent]]
    val possibleAgents = Await.result(possibleAgentsFuture, 4.seconds)
    exitTransitions.map((t: Transition) => {
      t.subjectID
      ActionData(t.messageType, true, exitCondLabel, possibleAgents = Some(possibleAgents.toList))
    })
  }


  private def indexOfInput(input: String): Int = {
    var i = 0
    for (t <- exitTransitions) {
      if (t.messageType.equals(input)) {
        return i
      }
      i += 1
    }
    -1
  }
}
