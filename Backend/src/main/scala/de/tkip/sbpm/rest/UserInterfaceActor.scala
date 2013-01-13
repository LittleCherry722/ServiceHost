package de.tkip.sbpm.rest

import java.util.concurrent.Future
import scala.concurrent.duration._
import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.Envelope
import de.tkip.sbpm.model.User
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.ProcessAttribute._
import spray.http.MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing._
import de.tkip.sbpm.persistence._
import scala.concurrent.Await

/**
 * This Actor is only used to process REST calls regarding "process"
 */
// TODO when to choose HttpService and when HttpServiceActor
class UserInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(context.self + " starts.")
  }

  override def postStop() {
    logger.debug(context.self + " stops.")
  }

  def actorRefFactory = context

  /**
   *
   * usually a REST Api should at least implement the following functions:
   * - GET withouht parameter => list of entity
   * - GET with id => specific entity
   * - PUT without id => new entity
   * - PUT with id => update entity
   * - DELETE with id => delete entity
   *
   * For more information about how to design a RESTful API see:
   * http://ajaxpatterns.org/RESTful_Service#RESTful_Principles
   *
   * Nevertheless: If an URL does not represent a resource, like the "execution" API
   * it makes sense to step away from this general template
   *
   */
  def receive = runRoute({
    parameters("userid") { (userId: String) =>
      get {
        /**
         * get a list of all user
         *
         * e.g. GET http://localhost:8080/user
         */
        path("") {
          complete("not yet implemented")
        } ~
          path(IntNumber) { id: Int =>
            implicit val timeout = Timeout(5 seconds)
            val future = context.actorFor("UserPersistenceActor") ? GetUser(Some(id))
            val result = Await.result(future, timeout.duration)

            // todo get user

            complete("result")
          }
      } ~
        put {
          /**
           * save the passed users
           *
           * e.g. PUT http://localhost:8080/user
           */
          path("") {
            parameters("user") { users: String =>
              
              val userArray = users.asJson.convertTo[Array[User]]

              implicit val timeout = Timeout(5 seconds)
              val actor = context.actorFor("UserPersistenceActor")
              
              val f: Future[Int] =
                for{
                  user <- userArray
                  x = actor ? SaveUser(user)
                } yield x

              f pipeTo actor 
              
              complete(new Envelope(None, "ok"))
            }
          }
        } ~
        delete {
          /**
           * delete an user
           *
           * e.g. DELETE http://localhost:8080/process/12
           */
          path(IntNumber) { id =>

            complete("'delete with id' not yet implemented")

          }
        }
    }
  })
}
