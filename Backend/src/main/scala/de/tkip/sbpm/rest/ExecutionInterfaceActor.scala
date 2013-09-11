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

import scala.concurrent.duration._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.logging.DefaultLogging
import java.net.URL
import java.net.HttpURLConnection
import java.io.DataOutputStream
import scala.collection.mutable.ArrayBuffer
import java.io.ByteArrayInputStream
import java.io.BufferedReader
import com.google.common.io.ByteStreams
import spray.http.StatusCode

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
class ExecutionInterfaceActor extends AbstractInterfaceActor with DefaultLogging {
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: IllegalArgumentException => ctx => {
        ctx.complete(StatusCodes.BadRequest, e.getMessage)
      }
      case e: Exception => ctx => {
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
      }
    }

  def actorRefFactory = context

  private lazy val subjectProviderManager = ActorLocator.subjectProviderManagerActor

  private val googleUserId = 5
  private val googleUri = "http://127.0.0.1:8888/"
  import de.tkip.sbpm.proto.{ GAEexecution => msg }

  private def routeToGoogle: PartialFunction[RequestContext, Unit] = {
    runRoute({
      get {
        //READ
        path(IntNumber) { processInstanceID =>
          complete {
            ProtobufWrapper.buildProcessInstanceData(talkWithGAE("get/" + processInstanceID, "GET"))
          }
        } ~
          // Show Actions
          path("action") {
            complete {
              ProtobufWrapper.buildActions(talkWithGAE("get/action", "GET"))
            }
          } ~
          path("history") {
            //you cannot run statements inside the path-block like above, instead, put them into a block inside the complete statement
            complete {
              Array[NewHistoryEntry]()
            }
          } ~
          //LIST
          path("") {
            complete {
              ProtobufWrapper.buildProcessInstanceInfos(talkWithGAE("get", "GET"))
            }
          }
      } ~
        put {
          //UPDATE
          pathPrefix(IntNumber) { processInstanceID =>
            path("") {
              entity(as[ExecuteAction]) { json =>
                System.err.println("/put/" + processInstanceID)
                // create the url connection
                //              val url = new URL(googleUri + "put/id")
                val proto = ProtobufWrapper.buildProto(json)

                println(proto)

                //execute next step
                complete {
                  ProtobufWrapper.buildProcessInstanceData(talkWithGAE("post/" + processInstanceID, "POST", Some(proto)))
                }
              }
            }
          }
        } ~
        post {
          //CREATE
          pathPrefix("") {
            path("") {
              entity(as[ProcessIdHeader]) { json =>
                complete {
                  System.err.println("/post")
                  import de.tkip.sbpm.persistence.query.{ Processes, Graphs }
                  import de.tkip.sbpm.model
                  val persistenceActor = ActorLocator.persistenceActor
                  val name = json.name.getOrElse("Unnamed")
                  val future =
                    for {
                      process <- (persistenceActor ? Processes.Read.ById(json.processId)).mapTo[Option[model.Process]]
                      graph <- (persistenceActor ? Graphs.Read.ById(process.get.activeGraphId.get)).mapTo[Option[model.Graph]]
                      proto = ProtobufWrapper.buildProto(CreateProcessInstance(userId, json.processId, name), graph.get)

                      result = {
                        System.err.println(graph.get)

                        //execute next step
                        ProtobufWrapper.buildProcessInstanceData(talkWithGAE("post", "POST", Some(proto)))
                      }
                    } yield result

                  future onComplete {
                    s => println(s)
                  }

                  future.map(result => result)
                }
              }
            }
          }
        }
    })
  }

  def routing = if (googleUserId == userId) routeToGoogle else runRoute({
    get {
      //READ
      path(IntNumber) { processInstanceID =>
        complete {
          val future = (subjectProviderManager ? ReadProcessInstance(userId, processInstanceID)).mapTo[ReadProcessInstanceAnswer]
          future.map(result => result.answer)
        }
      } ~
        // Show Actions
        path("action") {
          complete {
            val availableActionsFuture =
              (subjectProviderManager ? GetAvailableActions(userId))
                .mapTo[AvailableActionsAnswer]
            availableActionsFuture.map(result => result.availableActions)
          }
        } ~
        path("history") {
          //you cannot run statements inside the path-block like above, instead, put them into a block inside the complete statement
          complete {
            val getHistoryFuture = (ActorLocator.processManagerActor ? GetNewHistory()).mapTo[NewHistoryAnswer]
            getHistoryFuture.map(result => result.history.entries.filter(x => x.userId == Some(userId) || x.userId == None))
          }
        } ~
        //LIST
        path("") {
          complete {
            val future = (subjectProviderManager ? GetAllProcessInstances(userId)).mapTo[AllProcessInstancesAnswer]
            future.map(result => result.processInstanceInfo)
          }
        }

    } ~
      delete {
        //DELETE
        path(IntNumber) { processInstanceID =>
          //stop and delete given process instance
          // error gets caught automatically by the exception handler
          complete {
            val future = (subjectProviderManager ? KillProcessInstance(processInstanceID))
            future.map(_ => StatusCodes.NoContent)
          }
        }
      } ~
      put {
        //UPDATE
        pathPrefix(IntNumber) { processInstanceID =>
          path("") {
            entity(as[ExecuteAction]) { json =>
              //execute next step
              complete {
                val future = (subjectProviderManager ? mixExecuteActionWithRouting(json)).mapTo[ExecuteActionAnswer]
                future.map(result => result.answer)
              }
            }
          }
        }
      } ~
      post {
        //CREATE
        pathPrefix("") {
          path("") {
            entity(as[ProcessIdHeader]) { json =>
              complete {
                val name = json.name.getOrElse("Unnamed") // TODO not as an Option
                val future = (subjectProviderManager ? CreateProcessInstance(userId, json.processId, name)).mapTo[ProcessInstanceCreated]
                future.map(result => result.answer)
              }
            }
          }
        }
      }
  })

  private def doPost(subUrl: String, protobytes: Option[Array[Byte]]): Array[Byte] = {
    val url = new URL(googleUri + subUrl)
    val connection: HttpURLConnection =
      url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setDoInput(true)
    connection.setDoOutput(true)
    connection.setRequestMethod("POST")
    connection.setRequestProperty("Content-Length", if (protobytes.isDefined) protobytes.get.length.toString else "0")

    val out = new DataOutputStream(connection.getOutputStream())
    if (protobytes.isDefined) out.write(protobytes.get)
    out.flush()
    out.close()

    val in = connection.getInputStream()
    ByteStreams.toByteArray(in)
  }

  private def doGet(subUrl: String): Array[Byte] = {
    val url = new URL(googleUri + subUrl)
    val connection: HttpURLConnection =
      url.openConnection().asInstanceOf[HttpURLConnection]

    connection.setDoInput(true)

    ByteStreams.toByteArray(connection.getInputStream())
  }

  private def talkWithGAE(subUrl: String, httpMode: String, protobytes: Option[Array[Byte]]): Array[Byte] = {
    httpMode match {
      case "POST" => doPost(subUrl, protobytes)
      case "GET"  => doGet(subUrl)
    }
  }

  private def talkWithGAE(subUrl: String, httpMode: String): Array[Byte] = {
    talkWithGAE(subUrl, httpMode, None)
  }
}
