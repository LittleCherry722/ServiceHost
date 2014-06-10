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

package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.pattern._
import scala.language.postfixOps
import akka.event.Logging
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.FieldDefMagnet.apply
import spray.http.StatusCodes._
import spray.http.{ StatusCodes, StatusCode, HttpHeader, HttpHeaders }
import scala.concurrent.Future
import akka.actor.Props
import de.tkip.sbpm.persistence.PersistenceActor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.persistence.testdata.Entities
import de.tkip.sbpm.application.miscellaneous.KillAllProcessInstances

/**
 * This Actor is only used to process REST calls regarding "debug"
 */
class DebugInterfaceActor extends Actor with PersistenceInterface {

  implicit val executionContext = context.system.dispatcher

  /**
   *
   * usually a REST Api should at least implement the following functions:
   * - GET without parameter => list of entity
   * - GET with id => specific entity
   * - POST without id => new entity
   * - PUT with id => update entity
   * - DELETE with id => delete entity
   *
   * For more information about how to design a RESTful API see:
   * http://ajaxpatterns.org/RESTful_Service#RESTful_Principles
   *
   */
  def receive = runRoute({
    get {
      complete {
        val onFailure: PartialFunction[Throwable, Any] = {
          case e => log.error(e, e.getMessage)
        }

        var dbFuture = Future[Any]()

        dbFuture = dbFuture flatMap {
          case _ => {
            val msg = Schema.Recreate
            log.debug("TRACE: from " + this.self + " to " + persistenceActor + " " + msg)
            persistenceActor ? msg
          }
        }
        dbFuture.onFailure(onFailure)
        dbFuture.onSuccess { case s => log.debug("Successfully recreated Database Schema") }

        dbFuture = dbFuture flatMap { case _ => Entities.insert(persistenceActor) }
        dbFuture.onFailure(onFailure)
        dbFuture.onSuccess { case s => log.debug("Successfully Inserted Testdata") }

        val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor
        val msg = KillAllProcessInstances()
        log.debug("TRACE: from " + this.self + " to " + subjectProviderManagerActor + " " + msg)
        val killPiFuture = subjectProviderManagerActor ? msg
        killPiFuture.onFailure(onFailure)
        killPiFuture.onSuccess { case s => log.debug("Successfully killed all Process Instances") }

        for (
          x <- dbFuture;
          y <- killPiFuture
        ) yield StatusCodes.OK

      }
    }
  })

}
