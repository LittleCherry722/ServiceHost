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

import akka.actor.{ActorLogging, Actor}
import spray.http.RemoteAddress
import scala.util.Random
import de.tkip.sbpm.model._
import akka.event.Logging

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
        id            = iInterface.interfaceId.getOrElse(nextId),
        name          = iInterface.name,
        graph         = iInterface.graph,
        address       = Address(ip.value, iInterface.port),
        processId     = iInterface.id)
      sender ! interface
    }
  }

  private def nextId = {
    idGenerator.nextInt()
  }
}
