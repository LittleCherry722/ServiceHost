package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.event.Logging
import de.tkip.sbpm.model.Envelope
import de.tkip.sbpm.model.Group
import de.tkip.sbpm.persistence.DeleteGroup
import de.tkip.sbpm.persistence.GetGroup
import de.tkip.sbpm.persistence.SaveGroup
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.json.pimpAny
import spray.json.pimpString
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet.fromObject
import spray.routing.directives.FieldDefMagnet.apply

/**
 * This Actor is only used to process REST calls regarding "group"
 */
class GroupInterfaceActor extends Actor with PersistenceInterface with HttpService {
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
       * get a list of all groups
       *
       * e.g. GET http://localhost:8080/group
       */
      path("") {
        val res = request[Seq[Group]](GetGroup())
        complete(Envelope(Some(res.toJson), STATUS_OK))
      } ~
        path(IntNumber) { id: Int =>
          val res = request[Option[Group]](GetGroup(Some(id)))
          var env = Envelope(None, STATUS_NOT_FOUND)
          if (res.isDefined)
            env = Envelope(Some(res.get.toJson), STATUS_OK)
          complete(env)
        }
    } ~
      delete {
        /**
         * delete a group
         *
         * e.g. DELETE http://localhost:8080/group/12
         */
        path(IntNumber) { id =>
          execute(DeleteGroup(id))
          complete(Envelope(None, STATUS_OK))
        }
      } ~
      post {
        /**
         * create new Group
         *
         * e.g. POST http://localhost:8080/group
         * 	data={ "name": "abc", "isActive": true }
         */
        path("") {
          formFields("data") { implicit data: String =>
            val group = data.asJson.convertTo[Group]
            group.id = None
            val id = request[Int](SaveGroup(group))
            complete(Envelope(Some(id.toJson), STATUS_OK))
          }
        }
      } ~
      put {
        /**
         * update existing group
         *
         * e.g. PUT http://localhost:8080/group/2
         * 	data={ "name": "abc", "isActive": true }
         */
        path(IntNumber) { id =>
          formFields("data") { implicit data: String =>
            // unmarshalling of a group (as json) into an object with spray and the defined JsonProtocol
            val group = data.asJson.convertTo[Group]
            group.id = Some(id)
            execute(SaveGroup(group))
            complete(Envelope(None, STATUS_OK))
          }
        }
      }
  })
}
