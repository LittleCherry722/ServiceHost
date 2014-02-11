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

import akka.actor.FSM
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.SubjectTerminated
import de.tkip.sbpm.application.subject.misc.MacroTerminated
import de.tkip.sbpm.application.subject.misc.MacroTerminated

protected case class EndStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // Inform the processinstance that this subject has terminated
  logger.debug("TRACE: from " + this.self + " to " + internalBehaviorActor + " " + MacroTerminated(macroID).toString)
  internalBehaviorActor ! MacroTerminated(macroID)

  // nothing to receive for this state
  protected def stateReceive = FSM.NullFunction

  override def postStop() {
    logger.debug("End@" + userID + ", " + subjectID + "stops...")
  }

  override protected def getAvailableAction: Array[ActionData] =
    Array()
}