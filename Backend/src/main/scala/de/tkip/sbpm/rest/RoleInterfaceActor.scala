package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.event.Logging
import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json.pimpAny
import spray.json.pimpString
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.FieldDefMagnet.apply
import spray.http.StatusCodes._

/**
 * This Actor is only used to process REST calls regarding "role"
 */
class RoleInterfaceActor extends Actor with PersistenceInterface with HttpService {
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
       * get a list of all role
       *
       * e.g. GET http://localhost:8080/role
       */
      path("") {
        val res = request[Seq[Role]](GetRole())
        complete(res)
      } ~
       /**
         * get role by id
         *
         * e.g. GET http://localhost:8080/role/2
         */
        path(IntNumber) { id: Int =>
          val res = request[Option[Role]](GetRole(Some(id)))
          if (res.isDefined)
            complete(res.get)
          else
            complete(NotFound, "Role with id %d not found.".format(id))
        }
    } ~
      delete {
        /**
         * delete a role
         *
         * e.g. DELETE http://localhost:8080/role/12
         */
        path(IntNumber) { id =>
          execute(DeleteRole(id))
          // async call to database -> only send Accepted status code
          complete(Accepted)
        }
      } ~
      post {
        /**
         * create new role
         *
         * e.g. POST http://localhost:8080/role
         * payload: { "name": "abc", "isActive": true }
         */
        path("") {
          entity(as[Role]) { role =>
            role.id = None
            val id = request[Int](SaveRole(role))
            role.id = Some(id)
            // return created role with generated id
            complete(Created, role)
          }
        }
      } ~
      put {
        /**
         * update existing role
         *
         * e.g. PUT http://localhost:8080/role/2
         * payload: { "name": "abc", "isActive": true }
         */
        path(IntNumber) { id =>
          entity(as[Role]) { role =>
            role.id = Some(id)
            execute(SaveRole(role))
            // async call to database -> only send Accepted status code
            complete(Accepted)
          }
        }
      }
  })
}
