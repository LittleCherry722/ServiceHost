package de.tkip.sbpm.rest

import scala.concurrent.Future
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
import spray.http.StatusCodes._

/**
 * This Actor is only used to process REST calls regarding "user"
 */
class UserInterfaceActor extends Actor with PersistenceInterface with HttpService {
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(context.self + " starts.")
  }

  override def postStop() {
    logger.debug(context.self + " stops.")
  }

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
   * Nevertheless: If an URL does not represent a resource, like the "execution" API
   * it makes sense to step away from this general template
   *
   */
  def receive = runRoute({
    get {
      /**
       * get a list of all user
       *
       * e.g. GET http://localhost:8080/user
       */
      path("") {
        val res = request[Seq[User]](GetUser())
        complete(res)
      } ~
      /**
         * get user by id
         *
         * e.g. GET http://localhost:8080/user/8
         */
        path(IntNumber) { id: Int =>
          val res = request[Option[User]](GetUser(Some(id)))
          if (res.isDefined)
            complete(res.get)
          complete(NotFound, "User with id %d not found.".format(id))
        }
    } ~
      delete {
        /**
         * delete an user
         *
         * e.g. DELETE http://localhost:8080/user/12
         */
        path(IntNumber) { id =>
          execute(DeleteUser(id))
          // async call to database -> only send Accepted status code
          complete(Accepted)
        }
      } ~
      post {
        /**
         * create new user
         *
         * e.g. POST http://localhost:8080/user
         * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
         */
        path("") {
          entity(as[User]) { user =>
            user.id = None
            val id = request[Int](SaveUser(user))
            user.id = Some(id)
            // return created user with generated id
            complete(Created, user)
          }
        }
      } ~
      put {
        /**
         * update existing user
         *
         * e.g. PUT http://localhost:8080/user/2
         * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
         */
        path(IntNumber) { id =>
          entity(as[User]) { user =>
            user.id = Some(id)
            execute(SaveUser(user))
            // async call to database -> only send Accepted status code
            complete(Accepted)
          }
        }
      }
  })
}
