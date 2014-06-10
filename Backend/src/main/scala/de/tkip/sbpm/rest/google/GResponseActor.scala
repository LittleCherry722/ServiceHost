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

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.parsing.json.JSONObject
import scala.util.{ Try, Success, Failure }
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import spray.routing.HttpService
import spray.json.JsonFormat
import spray.http.StatusCodes
import spray.http.MediaTypes._
import spray.io.CommandWrapper
import spray.http._
import HttpHeaders._
import parser.HttpParser
import de.tkip.sbpm.rest.google.{
  GDriveActor,
  GAuthCtrl,
  GDriveControl,
  GCalendarActor
}
import GDriveActor.{ FindFiles, InitCredentials, RetrieveCredentials, UploadFile }
import GAuthCtrl.{ NoCredentialsException }
import GCalendarActor.{ CreateEvent }
import de.tkip.sbpm
import de.tkip.sbpm.logging.DefaultLogging
import com.google.api.services.calendar.model.Event
import java.util.UUID

class GResponseActor extends Actor with HttpService with DefaultLogging {

  import context.dispatcher

  implicit val timeout = Timeout(15 seconds)
  private lazy val driveActor = sbpm.ActorLocator.googleDriveActor
  private lazy val calendarActor = sbpm.ActorLocator.googleCalendarActor
  def actorRefFactory = context

  def receive = runRoute {
    post {
      // frontend request for authentication of SBPM app gainst Google account
      pathPrefix("init_auth") {
        formFields("id") { (userId) =>
          ctx => {
            log.debug(s"received authentication init post from user: $userId")
            log.debug("TRACE: from " + this.self + " to " + driveActor + " " + RetrieveCredentials(userId))
            (driveActor ? RetrieveCredentials(userId))
              .onComplete {
                case Success(files) => ctx.complete(StatusCodes.NoContent)
                case Failure(NoCredentialsException(auth_url)) =>
                  ctx.complete(StatusCodes.OK, auth_url)
                case Failure(e) => ctx.complete(e)
              }
          }
        }
      } ~
        path("create_event") {
          formFields("id", "summary", "location", "year", "month", "day") {
            (id, s, l, y, m, d) =>
              ctx => {
                log.debug(s"received get request for google calendar event from user: $id")
                val createMsg = CreateEvent(id, s, l, y.toInt, m.toInt, d.toInt)
                log.debug("TRACE: from " + this.self + " to " + calendarActor + " " + createMsg)
                (calendarActor ? createMsg)
                  .mapTo[Event]
                  .onComplete {
                    case Success(calEvent) => ctx.complete(calEvent.toString)
                    case Failure(e)        => ctx.complete(e.toString)
                  }
              }
          }
        } ~
        path("file-upload") {
          formFields('userId.as[String], 'datafile.as[Array[Byte]], 'fname.as[String], 'mimeType.as[String]) {
            (userId, fileArray, fname, mimeType) =>
              ctx => {
                val path = "gDriveFileUpload.tmp"
                val out = new java.io.FileOutputStream(path)
                out.write(fileArray)
                out.close()

                val uploadMsg = UploadFile(userId, fname, "", mimeType, path)
                log.debug("TRACE: from " + this.self + " to " + driveActor + " " + uploadMsg)
                (driveActor ? uploadMsg)
                  .onComplete {
                    case Success(gFileJson) => ctx.complete(gFileJson.toString)
                    case Failure(e)         => ctx.complete(e)
                  }
              }
          }
        }
    } ~
      get {
        // callback endpoint called by Google after an authentication request
        path("") {
          parameters("code", "state") { (code, userId) =>
            respondWithMediaType(`text/html`) { ctx => {
              log.debug(s"received from google response: name: $userId, code: $code")
              val initMsg = InitCredentials(userId, code)
              log.debug("TRACE: from " + this.self + " to " + driveActor + " " + initMsg)
              (driveActor ? initMsg)
                .onComplete {
                  case Success(_) =>
                    ctx.complete("<!DOCTYPE html>\n<html><head><script>window.close();</script></head><body>OK</body></html>")
                  case Failure(e) => ctx.complete(e)
                }
            } }
          }
        } ~
          path("get_files") {
            parameters("id") { id =>
              ctx => {
                log.debug(s"received get request for google drive files from user: $id")
                val findMsg = FindFiles(id, "", GDriveControl.default_fields)
                log.debug("TRACE: from " + this.self + " to " + driveActor + " " + findMsg)
                (driveActor ? findMsg)
                  .mapTo[String]
                  .onComplete {
                    case Success(files) => ctx.complete(files)
                    case Failure(NoCredentialsException(auth_url)) =>
                      ctx.complete(StatusCodes.Forbidden, auth_url)
                    case Failure(e) => ctx.complete(e.toString)
                  }
              }
            }
          }
      }
  }
}

class DirectHttpRequestHandler extends Actor with DefaultLogging {

  var chunkHandlers = Map.empty[ActorRef, ActorRef]

  def receive = {
    case s @ ChunkedRequestStart(HttpRequest(_, _, _, _, _)) =>
      require(!chunkHandlers.contains(sender))
      val client = sender
      val handler = context.actorOf(Props(new FileUploadHandler(client, s)), "FileUploadHandler____" + UUID.randomUUID().toString())
      chunkHandlers += (client -> handler)
      log.debug("TRACE: from " + this.self + " to " + handler + " " + s)
      handler.tell(s, client)
    case c: MessageChunk =>
      log.debug("TRACE: from " + this.self + " to " + chunkHandlers(sender) + " " + c)
      chunkHandlers(sender).tell(c, sender)
    case e: ChunkedMessageEnd =>
      log.debug("TRACE: from " + this.self + " to " + chunkHandlers(sender) + " " + e)
      chunkHandlers(sender).tell(e, sender)
      chunkHandlers -= sender
  }

}

class FileUploadHandler(client: ActorRef, start: ChunkedRequestStart) extends Actor with DefaultLogging {
  import start.request._
  import context.dispatcher

  implicit val timeout = Timeout(15 seconds)
  private lazy val driveActor = sbpm.ActorLocator.googleDriveActor

  val timeoutMsg = CommandWrapper(SetRequestTimeout(Duration.Inf))
  log.debug("TRACE: from " + this.self + " to " + client + " " + timeoutMsg)
  client ! timeoutMsg

  val tmpFile = java.io.File.createTempFile("chunked-receiver", ".tmp", new java.io.File("/tmp"))
  tmpFile.deleteOnExit()
  val output = new java.io.FileOutputStream(tmpFile)
  val Some(HttpHeaders.`Content-Type`(ContentType(multipart: MultipartMediaType, _))) = header[HttpHeaders.`Content-Type`]
  val boundary = multipart.parameters("boundary")

  def receive = {
    case c: MessageChunk =>
      output.write(c.body)

    case e: ChunkedMessageEnd =>
      output.close()

      val uploadMsg = UploadFile("abc", "test.txt", "", "text/plain", "gDriveFileUpload.tmp")
      log.debug("TRACE: from " + this.self + " to " + driveActor + " " + uploadMsg)
      (driveActor ? uploadMsg)
        .onComplete {
          case Success(gFileJson) => {
            val response = HttpResponse(status = 200, entity = gFileJson.toString)
            log.debug("TRACE: from " + this.self + " to " + client + " " + response)
            client ! response
            tmpFile.delete()
          }
          case Failure(e) => {
            val response = HttpResponse(status = 200, entity = e.toString)
            log.debug("TRACE: from " + this.self + " to " + client + " " + response)
            client ! response
          }
        }


      val timeoutMsg = CommandWrapper(SetRequestTimeout(2.seconds))
      log.debug("TRACE: from " + this.self + " to " + client + " " + timeoutMsg)
      client ! timeoutMsg

      context.stop(self)
  }

}
