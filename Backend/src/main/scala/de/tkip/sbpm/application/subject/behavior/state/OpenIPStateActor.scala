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

import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.subject.behavior.InputPoolOpened
import de.tkip.sbpm.application.subject.behavior.OpenInputPool
import de.tkip.sbpm.application.subject.misc.ActionData

protected case class OpenIPStateActor(data: StateData)
  extends BehaviorStateActor(data) {
  val msg = OpenInputPool((stateOptions.subjectId.get, stateOptions.messageType.get))
  log.debug("TRACE: from " + this.self + " to " + inputPoolActor + " " + msg)
  inputPoolActor ! msg

  override protected def stateReceive = {
    case InputPoolOpened => {
      changeState(exitTransition.successorID, data, null)
    }
  }

  private def exitTransition = exitTransitions(0) //TODO more than one exit transition possible?

  override protected def getAvailableAction: Array[ActionData] = Array()
}
