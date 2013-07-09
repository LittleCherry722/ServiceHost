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
package de.tkip.sbpm.rest.google

import akka.actor.{Actor, ActorSystem, Props}
import de.tkip.sbpm.rest.google.DriveControl
import akka.actor.Status.Failure


object DriveActor {
  case class FindFiles(userId: String, query: String, fields: String)
  case class RetrieveCredentials(userId: String)
  case class InitCredentials(userId: String, code: String)
}

class DriveActor extends Actor {
  import DriveActor._

  val driveCtrl = new DriveControl()

  def receive = {
    case FindFiles(u,q,f) =>
      try {
        sender ! driveCtrl.findFiles(u,q,f)
      } catch {
        case e: Throwable => sender ! Failure(e)
      }
    case InitCredentials(u,c) =>
      sender ! driveCtrl.initCredentials(u,c)
    case RetrieveCredentials(u) =>
      try {
        sender ! driveCtrl.getCredentials(u)
      } catch {
        case e: Throwable => sender ! Failure(e)
      }
  }

}