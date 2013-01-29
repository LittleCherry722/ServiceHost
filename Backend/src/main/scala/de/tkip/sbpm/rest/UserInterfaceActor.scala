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
import de.tkip.sbpm.model.GroupUser
import de.tkip.sbpm.model.Activatable

/**
 * This Actor is only used to process REST calls regarding "user"
 */
class UserInterfaceActor extends Actor with PersistenceInterface {
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
       * get a list of all user
       *
       * e.g. GET http://localhost:8080/user
       * result: JSON array of entities
       */
      path("") {
        completeWithQuery[Seq[User]](GetUser())
      } ~
        /**
         * get a list of all group <> user associations
         *
         * e.g. GET http://localhost:8080/user/group
         * result: JSON array of entities
         */
        path(Entity.GROUP) {
          completeWithQuery[Seq[GroupUser]](GetGroupUser())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get user by id
           *
           * e.g. GET http://localhost:8080/user/8
           * result: 404 Not Found or entity as JSON
           */
          path("") {
            completeWithQuery[User](GetUser(Some(id)), "User with id %d not found.", id)
          } ~
            /**
             * get all groups of the user
             *
             * e.g. GET http://localhost:8080/user/8/group
             * result: JSON array of entities
             */
            pathPrefix(Entity.GROUP) {
              path("") {
                completeWithQuery[Seq[GroupUser]](GetGroupUser(None, Some(id)))
              } ~
                /**
                 * get a specific group mapping of the user
                 *
                 * e.g. GET http://localhost:8080/user/8/group/2
                 * result: JSON of entity
                 */
                path(IntNumber) { groupId =>
                  completeWithQuery[GroupUser](GetGroupUser(Some(groupId), Some(id)), "User with id %d has no group with id %d.", id, groupId)
                }
            }
        }
    } ~
      delete {
        pathPrefix(IntNumber) { id =>
          /**
           * delete an user
           *
           * e.g. DELETE http://localhost:8080/user/12
           * result: 204 No Content
           */
          path("") {
            completeWithDelete(DeleteUser(id), "User could not be deleted. Entity with id %d not found.", id)
          } ~
            /**
             * delete a group of the user
             *
             * e.g. DELETE http://localhost:8080/user/8/group/1
             * result: 204 No Content
             */
            path(Entity.GROUP / IntNumber) { groupId =>
              completeWithDelete(DeleteGroupUser(groupId, id), "Group could not be removed from user. User with id %d has no group with id %d.", id, groupId)
            }
        }
      } ~
      post {
        /**
         * create new user
         *
         * e.g. POST http://localhost:8080/user
         * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
         * result: 	201 Created
         * 			Location: /user/8
         * 			{ "id": 8, "name": "abc", "isActive": true, "inputPoolSize": 8 }
         */
        path("") {
          entity(as[User]) { user =>
            saveUser(user)
          }
        }
      } ~
      put {
        /**
         * update existing user
         *
         * e.g. PUT http://localhost:8080/user/2/group/2
         * 	parameter: isActive=true
         * 	result: 201 Created or 200 OK
         * 			{ "groupId": 2, "userId": 2, "isActive": true }
         */
        pathPrefix(IntNumber) { id =>
          path(Entity.GROUP / IntNumber) { groupId: Int =>
            entity(as[Activatable]) { activatable =>
              val groupUser = GroupUser(groupId, id, activatable.isActive)
              saveGroup(groupUser)
            }
          } ~
            /**
             * update existing user
             *
             * e.g. PUT http://localhost:8080/user/2
             * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
             * 	result: 200 OK
             * 			{ "id": 2, "name": "abc", "isActive": true, "inputPoolSize": 8 }
             */
            path("") {
              entity(as[User]) { user =>
                saveUser(user, Some(id))
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
  def saveUser(entity: User, id: Option[Int] = None) = {
    // set param from url to entity id 
    // or delete id to create new entity
    entity.id = id
    completeWithSave(SaveUser(entity),
      entity,
      pathForEntity(Entity.USER, "%d"),
      (e: User, i: Int) => { e.id = Some(i); e })
  }

  /**
   * Save user <> group association.
   * completes with either 201 or 200
   */
  def saveGroup(groupUser: GroupUser) =
    completeWithSave[GroupUser, (Int, Int)](
      SaveGroupUser(groupUser),
      groupUser,
      pathForEntity(Entity.USER, "%d") + pathForEntity(Entity.GROUP, "%d"),
      (entity, id) => GroupUser(id._1, id._2, entity.isActive),
      (id) => Array(id._1, id._2))
}
