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

import scala.language.postfixOps

import akka.actor.Actor
import akka.event.Logging

import de.tkip.sbpm.model._
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.rest.JsonProtocol._

import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.FieldDefMagnet.apply
import spray.http.{HttpHeader, HttpHeaders}
import spray.http.StatusCodes._

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
        completeWithQuery[Seq[Group]](Groups.Read())
      } ~
        /**
         * get a list of all group <> role associations
         *
         * e.g. GET http://localhost:8080/group/role
         * result: JSON array of entities
         */
        path(Entity.ROLE) {
          completeWithQuery[Seq[GroupRole]](GroupsRoles.Read())
        } ~
        /**
         * get a list of all group <> user associations
         *
         * e.g. GET http://localhost:8080/group/user
         * result: JSON array of entities
         */
        path(Entity.USER) {
          completeWithQuery[Seq[GroupUser]](GroupsUsers.Read())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get group by id
           *
           * e.g. GET http://localhost:8080/group/2
           * result: 404 Not Found or entity as JSON
           */
          path("") {
            completeWithQuery[Group](Groups.Read.ById(id),
              "Group with id %d not found.", id)
          } ~
            /**
             * get a specific role mapping of the group
             *
             * e.g. GET http://localhost:8080/group/8/role/2
             * result: JSON of entity
             */
            path(Entity.ROLE / IntNumber) { roleId =>
              completeWithQuery[GroupRole](GroupsRoles.Read.ById(id, roleId),
                "Group with id %d has no role with id %d.", id, roleId)
            } ~
            /**
             * get a specific user mapping of the group
             *
             * e.g. GET http://localhost:8080/group/8/user/2
             * result: JSON of entity
             */
            path(Entity.USER / IntNumber) { userId =>
              completeWithQuery[GroupUser](GroupsUsers.Read.ById(id, userId),
                "Group with id %d has no user with id %d.", id, userId)
            }
        }
    } ~
      delete {
        /**
         * delete a group
         *
         * e.g. DELETE http://localhost:8080/group/12
         * result: 204 No Content
         */
        pathPrefix(IntNumber) { id =>
          path("") {
            completeWithDelete(Groups.Delete.ById(id),
              "Group could not be deleted. Entity with id %d not found.",
              id)
          } ~
            /**
             * delete a user from the group
             *
             * e.g. DELETE http://localhost:8080/group/8/user/1
             * result: 204 No Content
             */
            path(Entity.GROUP / IntNumber) { userId =>
              completeWithDelete(GroupsUsers.Delete.ById(id, userId),
                "User could not be removed from group. Group with id %d has no user with id %d.",
                id, userId)
            } ~
            /**
             * delete a role from the group
             *
             * e.g. DELETE http://localhost:8080/group/8/role/1
             * result: 204 No Content
             */
            path(Entity.ROLE / IntNumber) { roleId =>
              completeWithDelete(GroupsRoles.Delete.ById(id, roleId),
                "Role could not be removed from group. Group with id %d has no role with id %d.",
                id, roleId)
            }
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
        pathPrefix(IntNumber) { id =>
          /**
           * add user to a group
           *
           * e.g. PUT http://localhost:8080/group/2/user/2
           * 	result: 201 Created or 200 OK
           * 			{ "groupId": 2, "userId": 2, "isActive": true }
           */
          path(Entity.USER / IntNumber) { userId: Int =>
            val groupUser = GroupUser(id, userId)
            saveUser(groupUser)
          } ~
            /**
             * add role to a group
             *
             * e.g. PUT http://localhost:8080/group/2/role/2
             * 	result: 201 Created or 200 OK
             * 			{ "groupId": 2, "roleId": 2, "isActive": true }
             */
            path(Entity.ROLE / IntNumber) { roleId: Int =>
              val groupRole = GroupRole(id, roleId)
              saveRole(groupRole)
            } ~
            /**
             * update existing group
             *
             * e.g. PUT http://localhost:8080/group/2
             * payload: { "name": "abc", "isActive": true }
             * result: 	200 OK
             * 			{ "id": 2, "name": "abc", "isActive": true }
             */
            path("") {
              entity(as[Group]) { group =>
                save(group, Some(id))
              }
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
    val e = entity.copy(id)
    completeWithSave(Groups.Save(e),
      e,
      pathForEntity(Entity.GROUP, "%d"),
      (e: Group, i: Int) => { e.copy(Some(i)) })
  }

  /**
   * Save user <> group association.
   * completes with either 201 or 200
   */
  def saveUser(groupUser: GroupUser) =
    completeWithSave[GroupUser, (Int, Int)](
      GroupsUsers.Save(groupUser),
      groupUser,
      pathForEntity(Entity.GROUP, "%d") + pathForEntity(Entity.USER, "%d"),
      (entity, id) => GroupUser(id._1, id._2),
      (id) => Array(id._1, id._2))

  /**
   * Save role <> group association.
   * completes with either 201 or 200
   */
  def saveRole(groupRole: GroupRole) =
    completeWithSave[GroupRole, (Int, Int)](
      GroupsRoles.Save(groupRole),
      groupRole,
      pathForEntity(Entity.GROUP, "%d") + pathForEntity(Entity.ROLE, "%d"),
      (entity, id) => GroupRole(id._1, id._2),
      (id) => Array(id._1, id._2))
}
