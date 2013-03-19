package de.tkip.sbpm.rest

import scala.concurrent.Future
import auth.SessionDirectives._
import scala.concurrent.duration._
import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.User
import scala.language.postfixOps
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
import spray.http.StatusCodes
import spray.routing.authentication.UserPass
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.model.UserIdentity
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.model.Credentials
import ua.t3hnar.bcrypt._

/**
 * This Actor is only used to process REST calls regarding "user"
 */
class UserInterfaceActor extends Actor with PersistenceInterface {
  private lazy val userPassAuthActor = ActorLocator.userPassAuthActor

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
      path("^$"r) { regex =>
        completeWithQuery[Seq[User]](Users.Read())
      } ~
        /**
         * Return currently logged in user.
         */
        path("current") {
          user(actorRefFactory) { user =>
            complete(user)
          }
        } ~
        /**
         * get a list of all group <> user associations
         *
         * e.g. GET http://localhost:8080/user/group
         * result: JSON array of entities
         */
        path(Entity.GROUP) {
          completeWithQuery[Seq[GroupUser]](GroupsUsers.Read())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get user by id
           *
           * e.g. GET http://localhost:8080/user/8
           * result: 404 Not Found or entity as JSON
           */
          path("^$"r) { regex =>
            completeWithQuery[User](Users.Read.ById(id), "User with id %d not found.", id)
          } ~
            /**
             * get all groups of the user
             *
             * e.g. GET http://localhost:8080/user/8/group
             * result: JSON array of entities
             */
            pathPrefix(Entity.GROUP) {
              path("^$"r) { regex =>
                completeWithQuery[Seq[GroupUser]](GroupsUsers.Read.ByUserId(id))
              } ~
                /**
                 * get a specific group mapping of the user
                 *
                 * e.g. GET http://localhost:8080/user/8/group/2
                 * result: JSON of entity
                 */
                path(IntNumber) { groupId =>
                  completeWithQuery[GroupUser](GroupsUsers.Read.ById(groupId, id), "User with id %d has no group with id %d.", id, groupId)
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
          path("^$"r) { regex =>
            completeWithDelete(Users.Delete.ById(id), "User could not be deleted. Entity with id %d not found.", id)
          } ~
            /**
             * delete a group of the user
             *
             * e.g. DELETE http://localhost:8080/user/8/group/1
             * result: 204 No Content
             */
            path(Entity.GROUP / IntNumber) { groupId =>
              completeWithDelete(GroupsUsers.Delete.ById(groupId, id), "Group could not be removed from user. User with id %d has no group with id %d.", id, groupId)
            }
        }
      } ~
      post {
        /**
         * Perform user login with username and password and returns
         * the user on success.
         * e.g. POST http://localhost:8080/user/login
         * payload (JSON): { "user": "xxx", "pass": "yyy" }
         * or (form): user=xxx&pass=yyy
         * result: { "name": "xxx", "active": true, ... }
         */
        path("login") {
          (formFields('user, 'pass).as(UserPass) | entity(as[UserPass])) { userPass =>
            login(userPass)(context) { user =>
              complete(user)
            }
          }
        } ~
          /**
           * Performs a user logout by deleting current session.
           * e.g. POST http://localhost:8080/user/logout
           * result: 204 No Content
           */
          (path("logout") & deleteSession) {
            noContent()
          } ~
          /**
           * create new user
           *
           * e.g. POST http://localhost:8080/user
           * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
           * result: 	201 Created
           * 			Location: /user/8
           * 			{ "id": 8, "name": "abc", "isActive": true, "inputPoolSize": 8 }
           */
          path("^$"r) { regex =>
            entity(as[User]) { user =>
              saveUser(user)
            }
          }
      } ~
      put {
        pathPrefix(IntNumber) { id =>
          /**
           * add user to a group
           *
           * e.g. PUT http://localhost:8080/user/2/group/2
           * 	result: 201 Created or 200 OK
           * 			{ "groupId": 2, "userId": 2, "isActive": true }
           */
          path(Entity.GROUP / IntNumber) { groupId: Int =>
            val groupUser = GroupUser(groupId, id)
            saveGroup(groupUser)
          } ~
            /**
             * update existing user
             *
             * e.g. PUT http://localhost:8080/user/2
             * 	payload: { "name": "abc", "isActive": true, "inputPoolSize": 8 }
             * 	result: 200 OK
             * 			{ "id": 2, "name": "abc", "isActive": true, "inputPoolSize": 8 }
             */
            path("^$"r) { regex =>
              entity(as[User]) { user =>
                saveUser(user, Some(id))
              }
            } ~
            /**
             * update credentials of an existing user
             *
             * e.g. PUT http://localhost:8080/user/2
             * 	payload: { "provider": "sbpm", "eMail": "exa@mple.com", "oldPassword": "old", "password": "new" }
             * 	result: 200 OK
             *
             */
            path("^$"r) { regex =>
              entity(as[Credentials]) { credentials =>
                setUserIdentity(id, credentials)
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
    val e = entity.copy(id)
    completeWithSave(Users.Save(entity),
      entity,
      pathForEntity(Entity.USER, "%d"),
      (e: User, i: Int) => { e.copy(Some(i)) })
  }

  def setUserIdentity(id: Int, entity: Credentials) = {
    //check if the user exists
    val userFuture = persistenceActor ? Users.Read.ById(id)
    val user = Await.result(userFuture.mapTo[Option[User]], timeout.duration)

    if (user.isDefined) {
      // check if the old password is correct
      val authFuture = ActorLocator.userPassAuthActor ? UserPass(entity.oldEMail, entity.oldPassword)
      val auth = Await.result(authFuture.mapTo[Option[User]], timeout.duration)

      if (auth.isDefined) {
        // check what has to be changed
        var eMail = entity.oldEMail
        var password = entity.oldPassword

        if (entity.eMail.isDefined)
          eMail = entity.eMail.get
        if (entity.password.isDefined)
          password = entity.password.get

        // set the new password, eMail and provider
        val future = persistenceActor ? Users.Save.Identity(id, entity.provider, eMail, Some(password.bcrypt))
        val res = Await.result(future, timeout.duration)

        complete(StatusCodes.OK)
      } else
        complete(StatusCodes.Unauthorized)
    } else
      throw new Exception("User '" + id + "' does not exist.")
  }

  /**
   * Save user <> group association.
   * completes with either 201 or 200
   */
  def saveGroup(groupUser: GroupUser) =
    completeWithSave[GroupUser, (Int, Int)](
      GroupsUsers.Save(groupUser),
      groupUser,
      pathForEntity(Entity.USER, "%d") + pathForEntity(Entity.GROUP, "%d"),
      (entity, id) => GroupUser(id._1, id._2),
      (id) => Array(id._2, id._1))
}
