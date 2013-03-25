package de.tkip.sbpm.rest

import scala.concurrent.Future
import auth.SessionDirectives._
import scala.concurrent.duration._
import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.Envelope
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
import de.tkip.sbpm.model._
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
        GetUsersWithMail()
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
          completeWithQuery[Seq[GroupUser]](GetGroupUser())
        } ~
        pathPrefix(IntNumber) { id: Int =>
          /**
           * get user by id
           *
           * e.g. GET http://localhost:8080/user/8
           * result: 404 Not Found or entity as JSON
           */
          path("^$"r) { regex =>
            GetUserWithMail(id)
          } ~
            /**
             * get all groups of the user
             *
             * e.g. GET http://localhost:8080/user/8/group
             * result: JSON array of entities
             */
            pathPrefix(Entity.GROUP) {
              path("^$"r) { regex =>
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
          path("^$"r) { regex =>
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
             * update an existing user and his credentials
             *
             * e.g. PUT http://localhost:8080/user/2
             * 	payload: {"name":"test","isActive":true,"inputPoolSize":6,"provider":"sbpm","newEmail":"superuser@sbpm.com","oldPassword":"s1234","newPassword":"pass"}
             * 	result: 200 OK
             * 		{ "id": 2, "name":"test", "isActive": true, "inputPoolSize": 6 }
             */
            path("^$"r) { regex =>
              entity(as[UserUpdate]) { userUpdate =>
                setUserIdentity(id, userUpdate)
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

  // completes with all providers and emails of an user and the user information
  def GetUserWithMail(id: Int) = {
    val userFuture = persistenceActor ? GetUser(Some(id), None)
    val user = Await.result(userFuture.mapTo[Option[User]], timeout.duration)
    val identityFuture = persistenceActor ? GetUserWithIdentities(Some(id))
    val identity = Await.result(identityFuture.mapTo[Option[(User, List[UserIdentity])]], timeout.duration)
    if (user.isDefined && identity.isDefined) {
      val pm = for (i <- identity.get._2) yield ProviderMail(i.provider, i.eMail)
      complete(UserWithMail(user.get.id, user.get.name, user.get.isActive, user.get.inputPoolSize, pm))
    } else {
      complete(StatusCodes.NotFound)
    }
  }

  // completes with all providers and emails of all users and the user information
  def GetUsersWithMail() = {
    val usersFuture = persistenceActor ? GetUser(None, None)
    val users = Await.result(usersFuture.mapTo[List[User]], timeout.duration)
    val listOfUsers = for (user <- users) yield {
      val identityFuture = persistenceActor ? GetUserWithIdentities(user.id)
      val identity = Await.result(identityFuture.mapTo[Option[(User, List[UserIdentity])]], timeout.duration)
      val listOfMails = for (i <- identity.get._2) yield ProviderMail(i.provider, i.eMail)
      UserWithMail(user.id, user.name, user.isActive, user.inputPoolSize, listOfMails)
    }
    complete(listOfUsers)
  }

  def setUserIdentity(id: Int, entity: UserUpdate) = {
    //check if the user exists
    val userFuture = persistenceActor ? GetUser(Some(id), None)
    val userIdentityFuture = persistenceActor ? GetUserIdentity(entity.provider, Some(id), None)
    val user = Await.result(userFuture.mapTo[Option[User]], timeout.duration)
    val userIdentity = Await.result(userIdentityFuture.mapTo[Option[UserIdentity]], timeout.duration)

    if (user.isDefined && userIdentity.isDefined) {
      // check if the old password is correct
      val authFuture = ActorLocator.userPassAuthActor ? UserPass(userIdentity.get.eMail, entity.oldPassword)
      val auth = Await.result(authFuture.mapTo[Option[User]], timeout.duration)

      if (auth.isDefined) {
        // check what has to be changed
        var eMail = entity.newEmail.getOrElse(userIdentity.get.eMail)
        var password = entity.newPassword.getOrElse(entity.oldPassword)

        // set the new password, eMail and provider
        val future = persistenceActor ? SetUserIdentity(id, entity.provider, eMail, Some(password.bcrypt))
        val res = Await.result(future, timeout.duration)

        var name = entity.name.getOrElse(user.get.name)
        var isActive = entity.isActive.getOrElse(user.get.isActive)
        var inputPoolSize = entity.inputPoolSize.getOrElse(user.get.inputPoolSize)

        saveUser(new User(None, name, isActive, inputPoolSize), Some(id))
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
      SaveGroupUser(groupUser),
      groupUser,
      pathForEntity(Entity.USER, "%d") + pathForEntity(Entity.GROUP, "%d"),
      (entity, id) => GroupUser(id._1, id._2, entity.isActive),
      (id) => Array(id._2, id._1))
}
