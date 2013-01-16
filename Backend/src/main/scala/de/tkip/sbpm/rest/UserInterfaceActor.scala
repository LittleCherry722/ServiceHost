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
   * - GET withouht parameter => list of entity
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
        complete(Envelope(Some(res.toJson), STATUS_OK))
      } ~
        path(IntNumber) { id: Int =>
          val res = request[Option[User]](GetUser(Some(id)))
          var env = Envelope(None, STATUS_NOT_FOUND)
          if (res.isDefined)
            env = Envelope(Some(res.get.toJson), STATUS_OK)
          complete(env)
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
          complete(Envelope(None, STATUS_OK))
        }
      } ~
      post {
        /**
         * create new user
         *
         * e.g. POST http://localhost:8080/user
         * 	data={ "name": "abc", "isActive": true, "inputPoolSize": 8 }
         */
        path("") {
          formFields("data") { implicit data: String =>
            val user = data.asJson.convertTo[User]
            user.id = None
            val id = request[Int](SaveUser(user))
            complete(Envelope(Some(id.toJson), STATUS_OK))
          }
        }
      } ~
      put {
        /**
         * update existing user
         *
         * e.g. PUT http://localhost:8080/user/2
         * 	data={ "name": "abc", "isActive": true, "inputPoolSize": 8 }
         */
        path(IntNumber) { id =>
          formFields("data") { implicit data: String =>
            // unmarshalling of an user (as json) into an object with spray and the defined JsonProtocol
            val user = data.asJson.convertTo[User]
            user.id = Some(id)
            execute(SaveUser(user))
            complete(Envelope(None, STATUS_OK))
          }
        }
      }
  })
}
