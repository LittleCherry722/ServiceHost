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
import de.tkip.sbpm.model._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.persistence.query._

class MessageInterfaceActor extends AbstractInterfaceActor with DefaultLogging {
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  def actorRefFactory = context

  private lazy val persistence = ActorLocator.persistenceActor

  def routing = runRoute({
    get {
      //READ
      path(IntNumber) { messageID =>
        //                  completeWithQuery[Message](Messages.Read.ById(messageID))
        complete {
          (persistence ? Messages.Read.ById(messageID)).mapTo[Option[Message]]
        }
      } ~
        path("") {
          complete {
            val from = (persistence ? Messages.Read.WithSource(userId)).mapTo[Seq[Message]]
            val to = (persistence ? Messages.Read.WithTarget(userId)).mapTo[Seq[Message]]
            for {
              f <- from
              t <- to
            } yield f ++ t
          }
        } ~
        path("outbox") {
          complete {
            (persistence ? Messages.Read.WithSource(userId)).mapTo[Seq[Message]]
          }
        } ~
        path("inbox") {
          complete {
            (persistence ? Messages.Read.WithTarget(userId)).mapTo[Seq[Message]]
          }
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
            entity(as[SendMessageHeader]) { json =>
              complete {
                val message = Message(None, userId, json.toUser, json.title, false, json.content, new java.sql.Timestamp(System.currentTimeMillis()))
                persistence ! Messages.Save(message)
                StatusCodes.NoContent
              }
            }
          }
        }
      }
  })
}