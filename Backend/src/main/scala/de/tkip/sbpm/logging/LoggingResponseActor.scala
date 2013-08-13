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
package de.tkip.sbpm.logging

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.parsing.json.JSONObject
import scala.util.{Try, Success, Failure}

import akka.actor.{Actor, ActorLogging}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.HttpService

import de.tkip.sbpm
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.logging.LogPersistenceActor.Get
import de.tkip.sbpm.persistence.schema.Log

class LoggingResponseActor extends Actor with HttpService with DefaultLogging {

  import context.dispatcher

  implicit val timeout = Timeout(15 seconds)
  private val logPersistenceActor = sbpm.ActorLocator.logPersistenceActor
  def actorRefFactory = context
  
  def receive = runRoute {
    get {
      path("get_logs") {
        parameters("n") { n => ctx =>
          log.debug(s"received get request for recent $n logs")
          (logPersistenceActor ? Get(n.toInt))
            .mapTo[List[Log]]
            .onComplete {
              case Success(log_list) => ctx.complete(
                log_list.mkString("\n")
              )
              case Failure(e) => ctx.complete(e.toString)
            }
        }
      }
    }
  }
}
