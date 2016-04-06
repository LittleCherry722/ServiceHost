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

package de.tkip.sbpm.repo

import akka.actor.{Actor, ActorLogging}
import de.tkip.sbpm.model._
import spray.http.RemoteAddress

import scala.util.Random

object IntermediateInterfaceActor {
  case class ConvertToInterface(interface: IntermediateInterface, ip: RemoteAddress)
}

class IntermediateInterfaceActor extends Actor with ActorLogging {
  import IntermediateInterfaceActor._

  private val idGenerator = new Random

  def receive = {
    case ConvertToInterface(iInterface, ip) => {
      val interface = Interface(
        interfaceType = iInterface.interfaceType,
        id        = iInterface.interfaceId,
        name      = iInterface.name,
        views     = iInterface.views,
        address   = Address(None, ip.value, iInterface.port),
        processId = iInterface.processId)
      sender ! (iInterface.localSubjectId ,interface)
    }
  }

  private def nextId = {
    idGenerator.nextInt()
  }
}
