package de.tkip.sbpm.rest

import akka.actor.Actor
import scala.language.postfixOps
import akka.pattern.ask
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.Message
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.json.pimpAny
import spray.json.pimpString
import spray.routing._
import spray.routing.Directive.pimpApply
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.FieldDefMagnet.apply
import spray.util.LoggingContext
import de.tkip.sbpm.persistence.query._
import spray.httpx.marshalling.Marshaller
import scala.concurrent.Future

class MessageInterfaceActor extends Actor  with PersistenceInterface {

  def routing = runRoute({
    get {
      //READ
      path(IntNumber) { messageID =>
        //          completeWithQuery[Message](Messages.Read.ById(messageID))
        val m = Message(None, 1, 2, "", false, "", null)
        complete(m)
      } ~
        //LIST
        path("") {
          completeWithQuery[Seq[Message]](Messages.Read())
        }
    } ~
      delete {
        //DELETE
        path(IntNumber) { messageID =>
          //stop and delete given process instance
          // error gets caught automatically by the exception handler
          complete {
            StatusCodes.NoContent
          }
        }
      } ~
      post {
        //CREATE
        pathPrefix("") {
          path("") {
            entity(as[ProcessIdHeader]) { json =>
              complete {
                StatusCodes.NoContent
              }
            }
          }
        }
      }
  })
}