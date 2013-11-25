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

package de.tkip.sbpm.bir

import akka.actor.Actor
import de.tkip.sbpm._
import spray.routing.HttpService
import akka.util.Timeout

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

import akka.actor.{Actor, ActorLogging}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.HttpService
import spray.json.JsonFormat
import spray.http.StatusCodes
import spray.http.MediaTypes._

import de.tkip.sbpm.rest.google.GDriveActor._
import de.tkip.sbpm.bir.GoogleBIRActor._


import de.tkip.sbpm.logging.DefaultLogging

import de.tkip.sbpm.bir.BIRJsonProtocol._

class GoogleBIRInterfaceActor extends Actor with HttpService with DefaultLogging{
  import context.dispatcher
  implicit val timeout = Timeout(15 seconds)
  private lazy val driveActor = ActorLocator.googleDriveActor
  private lazy val birActor = ActorLocator.googleBIRActor
  def actorRefFactory = context
  
  def receive = runRoute {
    post {
      // frontend request for creating actor for BIR
      pathPrefix("create_actor") {
        formFields("id") { (userId) => ctx =>
          log.debug(s"${getClass.getName} received create actor post from user: $userId")
          (driveActor ? RetrieveEmail(userId))
            .onComplete {
              case Success(email) => {
                birActor ! CreateActor(email.toString)
                ctx.complete(StatusCodes.OK)
                }
              case Failure(e) => ctx.complete(e)
            }
        }
      } ~
      pathPrefix("create_bi") { formFields("id") { (userId) => ctx =>
          log.debug(s"${getClass.getName} received create BI post from user: $userId")
          val source = """{"process":[{"id":"Staples","name":"Staples","type":"single","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"","relatedProcess":null,"externalType":"external","role":"","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":7,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":5,"text":"","start":false,"end":false,"type":"receive","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":6,"text":"Bestellung aus Lager holen","start":false,"end":false,"type":"action","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"Verfügbarkeit prüfen","start":false,"end":false,"type":"action","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":3,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":4,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":2,"text":"m3","type":"exitcondition","target":{"id":"Großunternehmen","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":4,"text":"Verfügbar","type":"exitcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":6,"text":"Nicht Verfügbar","type":"exitcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":3,"end":1,"text":"m2","type":"exitcondition","target":{"id":"Großunternehmen","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":3,"text":"m2","type":"exitcondition","target":{"id":"Zulieferer","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m1","type":"exitcondition","target":{"id":"Zulieferer","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":4,"text":"","type":"exitcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1},{"id":"Zulieferer","name":"Lieferant","type":"external","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"Zulieferer","relatedProcess":3,"externalType":"interface","role":"PROVIDER","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":3,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":1,"text":"m1","type":"exitcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m2","type":"exitcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1},{"id":"Großunternehmen","name":"Kunde","type":"external","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"Großunternehmen","relatedProcess":1,"externalType":"interface","role":"","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":3,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"send","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":false,"type":"receive","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":1,"text":"m3","type":"exitcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m2","type":"exitcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1}],"conversations":{},"conversationCounter":1,"messages":{"m1":"Abholauftrag","m2":"Lieferdatum","m3":"Bestellung"},"messageCounter":4,"nodeCounter":1}"""
          birActor ! CreateBI("BI FOR TEST", "Lieferant", source)
          ctx.complete(StatusCodes.OK)
          }   
      }
    }
    
  }

}