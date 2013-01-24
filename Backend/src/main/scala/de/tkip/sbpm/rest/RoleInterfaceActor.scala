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
class RoleInterfaceActor extends Actor with PersistenceInterface {
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
   */
  def receive = runRoute({
    get {
      /**
       * get a list of all role
       *
       * e.g. GET http://localhost:8080/role
       * result: JSON array with entities
       */
      path("") {
        completeWithQuery[Seq[Role]](GetRole())
      } ~
        /**
         * get role by id
         *
         * e.g. GET http://localhost:8080/role/2
         * result: 404 Not Found or entity as JSON
         */
        path(IntNumber) { id: Int =>
          completeWithQuery[Role](GetRole(Some(id)), "Role with id %d not found.", id)
        }
    } ~
      delete {
        /**
         * delete a role
         *
         * e.g. DELETE http://localhost:8080/role/12
         * result: 204 No Content
         */
        path(IntNumber) { id =>
          completeWithDelete(DeleteRole(id), "Role could not be deleted. Entity with id %d not found.", id)
        }
      } ~
      post {
        /**
         * create new role
         *
         * e.g. POST http://localhost:8080/role
         * payload: { "name": "abc", "isActive": true }
         * result: 	201 Created
         * 			Location: /role/8
         * 			{ "id": 8, "name": "abc", "isActive": true }
         */
        path("") {
          entity(as[Role]) { role =>
            save(role)
          }
        }
      } ~
      put {
        /**
         * update existing role
         *
         * e.g. PUT http://localhost:8080/role/2
         * payload: { "name": "abc", "isActive": true }
         * result: 	200 OK
         * 			{ "id": 2, "name": "abc", "isActive": true }
         */
        path(IntNumber) { id =>
          entity(as[Role]) { role =>
            save(role, Some(id))
          }
        }
      }
  })

  /**
   * Save given entity with given id to database.
   * id = None -> new entity
   * completes with either 201 or 200 
   */
  def save(entity: Role, id: Option[Int] = None) = {
    // set param from url to entity id 
    // or delete id to create new entity
    entity.id = id
    completeWithSave(SaveRole(entity),
      entity,
       pathForEntity(Entity.ROLE, "%d"),
      (e: Role, i: Int) => { e.id = Some(i); e })
  }
}
