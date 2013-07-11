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

import scala.concurrent.Future

import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Status.Failure
import akka.pattern.pipe

import de.tkip.sbpm.rest.google.GDriveControl


object GDriveActor {
  case class FindFiles(userId: String, query: String, fields: String)
  case class RetrieveCredentials(userId: String)
  case class InitCredentials(userId: String, code: String)
}

class GDriveActor extends Actor {
  import GDriveActor._
  implicit val ec = context.dispatcher

  val driveCtrl = new GDriveControl()

  def receive = {
    case FindFiles(u,q,f) =>
      Future { driveCtrl.findFiles(u,q,f) } pipeTo sender
    case InitCredentials(u,c) =>
      Future { driveCtrl.initCredentials(u,c) } pipeTo sender
    case RetrieveCredentials(u) =>
      Future { driveCtrl.getCredentials(u) } pipeTo sender
  }

}