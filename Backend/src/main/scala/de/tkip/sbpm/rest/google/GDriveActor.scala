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
  case class GetUrl(userId: String, fileId: String)
  case class PublishFile(userId: String, fileId: String)
  case class UnpublishFile(userId: String, fileId: String)
  case class ShareFile(userId: String, fileId: String, targetId: String)
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
    case GetUrl(u,f) =>
      Future { driveCtrl.fileUrl(u,f) } pipeTo sender
    case PublishFile(u,f) =>
      Future { driveCtrl.publishFile(u,f) } pipeTo sender
    case UnpublishFile(u,f) =>
      Future { driveCtrl.unpublishFile(u,f) } pipeTo sender
    case ShareFile(u,f,t) =>
      Future { driveCtrl.shareFile(u,f,t) } pipeTo sender
  }

}