/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.rest

import de.tkip.sbpm.instrumentation.InstrumentedActor
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
import spray.routing.directives.FieldDefMagnet.apply
import spray.http.StatusCodes._
import de.tkip.sbpm.persistence.query._

/**
 * This Actor is only used to process REST calls regarding "role"
 */
class RoleInterfaceActor extends InstrumentedActor with PersistenceInterface {
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
  def wrappedReceive = runRoute({
    get {
      /**
       * get a list of all role
       *
       * e.g. GET http://localhost:8080/role
       * result: JSON array with entities
       */
      path("") {
        completeWithQuery[Seq[Role]](Roles.Read())
      } ~
        /**
         * get a list of all group <> role associations
         *
         * e.g. GET http://localhost:8080/role/group
         * result: JSON array of entities
         */
        path(Entity.GROUP) {
          completeWithQuery[Seq[GroupRole]](GroupsRoles.Read())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get role by id
           *
           * e.g. GET http://localhost:8080/role/2
           * result: 404 Not Found or entity as JSON
           */
          path("") {
            completeWithQuery[Role](Roles.Read.ById(id), "Role with id %d not found.", id)
          } ~
            /**
             * get all groups of the role
             *
             * e.g. GET http://localhost:8080/role/8/group
             * result: JSON array of entities
             */
            pathPrefix(Entity.GROUP) {
              path("") {
                completeWithQuery[Seq[GroupRole]](GroupsRoles.Read.ByGroupId(id))
              } ~
                /**
                 * get a specific group mapping of the role
                 *
                 * e.g. GET http://localhost:8080/role/8/group/2
                 * result: JSON of entity
                 */
                path(IntNumber) { groupId =>
                  completeWithQuery[GroupRole](GroupsRoles.Read.ById(groupId, id), "Role with id %d has no group with id %d.", id, groupId)
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
          path("") {
            completeWithDelete(Roles.Delete.ById(id), "Role could not be deleted. Entity with id %d not found.", id)
          } ~
            /**
             * delete a group of the role
             *
             * e.g. DELETE http://localhost:8080/role/8/group/1
             * result: 204 No Content
             */
            path(Entity.GROUP / IntNumber) { groupId =>
              completeWithDelete(GroupsRoles.Delete.ById(groupId, id), "Group could not be removed from role. Role with id %d has no group with id %d.", id, groupId)
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
        path("") {
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
          path("") {
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
    val e = entity.copy(id)
    completeWithSave(Roles.Save(e),
      e,
      pathForEntity(Entity.ROLE, "%d"),
      (e: Role, i: Int) => { e.copy(Some(i))})
  }

  /**
   * Save role <> group association.
   * completes with either 201 or 200
   */
  def saveGroup(groupRole: GroupRole) =
    completeWithSave[GroupRole, (Int, Int)](
      GroupsRoles.Save(groupRole),
      groupRole,
      pathForEntity(Entity.ROLE, "%d") + pathForEntity(Entity.GROUP, "%d"),
      (entity, id) => GroupRole(id._1, id._2),
      (id) => Array(id._2, id._1))

}
