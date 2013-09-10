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

package de.tkip.sbpm

import akka.actor.ActorRefFactory

object ActorLocator {
  val processInstanceActorName = "process-instance"
  val frontendInterfaceActorName = "frontend-interface"

  def actor(name: String)(implicit ctx: ActorRefFactory) = ctx.actorFor("/user/" + name)

  def processManagerActor(implicit ctx: ActorRefFactory) = actor(processInstanceActorName)
  def frontendInterfaceActor(implicit ctx: ActorRefFactory) = actor(frontendInterfaceActorName)
}
