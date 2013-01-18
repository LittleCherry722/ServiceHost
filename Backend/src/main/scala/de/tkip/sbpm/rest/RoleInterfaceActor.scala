package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.event.Logging
import de.tkip.sbpm.model.Envelope
import de.tkip.sbpm.model.Role
import de.tkip.sbpm.persistence.DeleteRole
import de.tkip.sbpm.persistence.GetRole
import de.tkip.sbpm.persistence.SaveRole
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.json.pimpAny
import spray.json.pimpString
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet.fromObject
import spray.routing.directives.FieldDefMagnet.apply

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
        complete(Envelope(Some(res.toJson), STATUS_OK))
      } ~
        path(IntNumber) { id: Int =>
          val res = request[Option[Role]](GetRole(Some(id)))
          var env = Envelope(None, STATUS_NOT_FOUND)
          if (res.isDefined)
            env = Envelope(Some(res.get.toJson), STATUS_OK)
          complete(env)
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
          complete(Envelope(None, STATUS_OK))
        }
      } ~
      post {
        /**
         * create new role
         *
         * e.g. POST http://localhost:8080/role
         * 	data={ "name": "abc", "isActive": true }
         */
        path("") {
          formFields("data") { implicit data: String =>
            val role = data.asJson.convertTo[Role]
            role.id = None
            val id = request[Int](SaveRole(role))
            complete(Envelope(Some(id.toJson), STATUS_OK))
          }
        }
      } ~
      put {
        /**
         * update existing role
         *
         * e.g. PUT http://localhost:8080/role/2
         * 	data={ "name": "abc", "isActive": true }
         */
        path(IntNumber) { id =>
          formFields("data") { implicit data: String =>
            // unmarshalling of an role (as json) into an object with spray and the defined JsonProtocol
            val role = data.asJson.convertTo[Role]
            role.id = Some(id)
            execute(SaveRole(role))
            complete(Envelope(None, STATUS_OK))
          }
        }
      }
  })
}
