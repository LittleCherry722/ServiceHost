package de.tkip.sbpm.rest

import akka.actor.Actor
import scala.language.postfixOps
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
      path("^$"r) { regex => 
        completeWithQuery[Seq[Role]](GetRole())
      } ~
        /**
         * get a list of all group <> role associations
         *
         * e.g. GET http://localhost:8080/role/group
         * result: JSON array of entities
         */
        path(Entity.GROUP) {
          completeWithQuery[Seq[GroupRole]](GetGroupRole())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get role by id
           *
           * e.g. GET http://localhost:8080/role/2
           * result: 404 Not Found or entity as JSON
           */
          path("^$"r) { regex => 
            completeWithQuery[Role](GetRole(Some(id)), "Role with id %d not found.", id)
          } ~
            /**
             * get all groups of the role
             *
             * e.g. GET http://localhost:8080/role/8/group
             * result: JSON array of entities
             */
            pathPrefix(Entity.GROUP) {
              path("^$"r) { regex => 
                completeWithQuery[Seq[GroupRole]](GetGroupRole(None, Some(id)))
              } ~
                /**
                 * get a specific group mapping of the role
                 *
                 * e.g. GET http://localhost:8080/role/8/group/2
                 * result: JSON of entity
                 */
                path(IntNumber) { groupId =>
                  completeWithQuery[GroupRole](GetGroupRole(Some(groupId), Some(id)), "Role with id %d has no group with id %d.", id, groupId)
                }
            }
        }
    } ~
      delete {
        pathPrefix(IntNumber) { id =>
          /**
           * delete a role
           *
           * e.g. DELETE http://localhost:8080/role/12
           * result: 204 No Content
           */
          path("^$"r) { regex => 
            completeWithDelete(DeleteRole(id), "Role could not be deleted. Entity with id %d not found.", id)
          } ~
            /**
             * delete a group of the role
             *
             * e.g. DELETE http://localhost:8080/role/8/group/1
             * result: 204 No Content
             */
            path(Entity.GROUP / IntNumber) { groupId =>
              completeWithDelete(DeleteGroupRole(groupId, id), "Group could not be removed from role. Role with id %d has no group with id %d.", id, groupId)
            }
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
        path("^$"r) { regex => 
          entity(as[Role]) { role =>
            save(role)
          }
        }
      } ~
      put {
        pathPrefix(IntNumber) { id =>
          /**
           * update existing role
           *
           * e.g. PUT http://localhost:8080/role/2
           * payload: { "name": "abc", "isActive": true }
           * result: 	200 OK
           * 			{ "id": 2, "name": "abc", "isActive": true }
           */
          path("^$"r) { regex => 
            entity(as[Role]) { role =>
              save(role, Some(id))
            }
          } ~
            /**
             * add role to a group
             *
             * e.g. PUT http://localhost:8080/role/2/group/2
             * 	result: 201 Created or 200 OK
             * 			{ "groupId": 2, "roleId": 2, "isActive": true }
             */
            path(Entity.GROUP / IntNumber) { groupId: Int =>
              val groupRole = GroupRole(groupId, id)
              saveGroup(groupRole)
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

  /**
   * Save role <> group association.
   * completes with either 201 or 200
   */
  def saveGroup(groupRole: GroupRole) =
    completeWithSave[GroupRole, (Int, Int)](
      SaveGroupRole(groupRole),
      groupRole,
      pathForEntity(Entity.ROLE, "%d") + pathForEntity(Entity.GROUP, "%d"),
      (entity, id) => GroupRole(id._1, id._2, entity.isActive),
      (id) => Array(id._2, id._1))

}
