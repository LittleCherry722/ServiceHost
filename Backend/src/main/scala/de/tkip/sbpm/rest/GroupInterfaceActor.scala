package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.event.Logging
import de.tkip.sbpm.model.Group
import de.tkip.sbpm.persistence.DeleteGroup
import de.tkip.sbpm.persistence.GetGroup
import de.tkip.sbpm.persistence.SaveGroup
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.FieldDefMagnet.apply
import spray.http.StatusCodes._
import spray.http.HttpHeader
import spray.http.HttpHeaders

/**
 * This Actor is only used to process REST calls regarding "group"
 */
class GroupInterfaceActor extends Actor with PersistenceInterface {
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
       * get a list of all groups
       *
       * e.g. GET http://localhost:8080/group
       * result: JSON array of entities
       */
      path("") {
        completeWithQuery[Seq[Group]](GetGroup())
      } ~
        /**
         * get group by id
         *
         * e.g. GET http://localhost:8080/group/2
         * result: 404 Not Found or entity as JSON
         */
        path(IntNumber) { id: Int =>
          completeWithQuery[Group](GetGroup(Some(id)), "Group with id %d not found.", id)
        }
    } ~
      delete {
        /**
         * delete a group
         *
         * e.g. DELETE http://localhost:8080/group/12
         * result: 204 No Content
         */
        path(IntNumber) { id =>
          completeWithDelete(DeleteGroup(id), "Group could not be deleted. Entity with id %d not found.", id)
        }
      } ~
      post {
        /**
         * create new Group
         *
         * e.g. POST http://localhost:8080/group
         * payload: { "name": "abc", "isActive": true }
         * result: 	201 Created
         * 			Location: /group/8
         * 			{ "id": 8, "name": "abc", "isActive": true }
         */
        path("") {
          entity(as[Group]) { group =>
            save(group)
          }
        }
      } ~
      put {
        /**
         * update existing group
         *
         * e.g. PUT http://localhost:8080/group/2
         * payload: { "name": "abc", "isActive": true }
         * result: 	200 OK
         * 			{ "id": 2, "name": "abc", "isActive": true }
         */
        path(IntNumber) { id =>
          entity(as[Group]) { group =>
            save(group, Some(id))
          }
        }
      }
  })

  /**
   * Save given entity with given id to database.
   * id = None -> new entity
   * completes with either 201 or 200 
   */
  def save(entity: Group, id: Option[Int] = None) = {
    // set param from url to entity id 
    // or delete id to create new entity
    entity.id = id
    completeWithSave(SaveGroup(entity),
      entity,
       pathForEntity(Entity.GROUP, "%d"),
      (e: Group, i: Int) => { e.id = Some(i); e })
  }
}
