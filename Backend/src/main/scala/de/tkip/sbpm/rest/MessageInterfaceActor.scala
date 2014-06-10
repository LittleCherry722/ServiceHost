package de.tkip.sbpm.rest

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._

class MessageInterfaceActor extends AbstractInterfaceActor {
  import context.dispatcher
  implicit val timeout = Timeout(5 seconds)

  def actorRefFactory = context

  private lazy val persistence = ActorLocator.persistenceActor
  private lazy val changeActor = ActorLocator.changeActor

  def routing = runRoute({
    get {
      //READ
      path(IntNumber) { messageID =>
        //                  completeWithQuery[Message](Messages.Read.ById(messageID))
        complete {
          val msg = Messages.Read.ById(messageID)
          log.debug("TRACE: from " + this.self + " to " + persistence +" " + msg)
          (persistence ? msg).mapTo[Option[Message]]
        }
      } ~
        path("") {
          complete {
            val fromMsg = Messages.Read.WithSource(userId)
            val toMsg = Messages.Read.WithTarget(userId)

            log.debug("TRACE: from " + this.self + " to " + persistence +" " + fromMsg)
            log.debug("TRACE: from " + this.self + " to " + persistence +" " + toMsg)

            val from = (persistence ? fromMsg).mapTo[Seq[Message]]
            val to = (persistence ? toMsg).mapTo[Seq[Message]]

            for {
              f <- from
              t <- to
            } yield f ++ t
          }
        } ~
        path("outbox") {
          complete {
            val msg = Messages.Read.WithSource(userId)
            log.debug("TRACE: from " + this.self + " to " + persistence +" " + msg)
            (persistence ? msg).mapTo[Seq[Message]]
          }
        } ~
        path("inbox") {
          complete {
            val msg = Messages.Read.WithTarget(userId)
            log.debug("TRACE: from " + this.self + " to " + persistence +" " + msg)
            (persistence ? msg).mapTo[Seq[Message]]
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

                val readAllMsg = Messages.Read.All
                log.debug("TRACE: from " + this.self + " to " + persistence +" " + readAllMsg)
                val future = for {
                  all <- (persistence ? readAllMsg).mapTo[Seq[Message]]
                  length = all.length
                } yield length
                val result = Await.result(future, 5 seconds).asInstanceOf[Int]
                val messageWithID = message.copy(id = Some(result + 1))

                val saveMsg = Messages.Save(message)
                log.debug("TRACE: from " + this.self + " to " + persistence + " " + saveMsg)
                persistence ! saveMsg

                val changeMsg = MessageChange(messageWithID, "insert", new java.util.Date())
                log.debug("TRACE: from " + this.self + " to " + changeActor + " " + changeMsg)
                changeActor ! changeMsg

                StatusCodes.NoContent
              }
            }
          }
        }
      }
  })
}
