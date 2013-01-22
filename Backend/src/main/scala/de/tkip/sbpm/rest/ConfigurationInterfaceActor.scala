package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.event.Logging
import de.tkip.sbpm.model.Configuration
import de.tkip.sbpm.persistence.DeleteConfiguration
import de.tkip.sbpm.persistence.GetConfiguration
import de.tkip.sbpm.persistence.SaveConfiguration
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.ContentTypeResolver._
import spray.http.StatusCodes._
import spray.routing.directives.PathMatchers

/**
 * This Actor is only used to process REST calls regarding "configuration"
 */
class ConfigurationInterfaceActor extends Actor with PersistenceInterface {
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
   * - GET with key => specific entity
   * - PUT with key => create or update entity
   * - DELETE with key => delete entity
   *
   * For more information about how to design a RESTful API see:
   * http://ajaxpatterns.org/RESTful_Service#RESTful_Principles
   *
   */
  def receive = runRoute({
    get {
      /**
       * get a list of all configurations
       *
       * e.g. GET http://localhost:8080/configuration
       * result: JSON array of entities
       */
      path("") {
        val res = request[Seq[Configuration]](GetConfiguration())
        complete(res)
      } ~
        /**
         * get configuration by key
         *
         * e.g. GET http://localhost:8080/configuration/sbpm.debug
         * result: 404 Not Found or JSON of entity
         */
        path(PathElement) { key =>
          val res = request[Option[Configuration]](GetConfiguration(Some(key)))
          if (res.isDefined)
            complete(res.get)
          else
            notFound("Configuration with key '%s' not found.", key)
        }
    } ~
      delete {
        /**
         * delete a configuration entry
         *
         * e.g. DELETE http://localhost:8080/configuration/sbpm.debug
         * result: 202 Accepted -> content deleted asynchronously
         */
        path(PathElement) { key =>
          execute(DeleteConfiguration(key))
          // async call to database -> only send Accepted status code
          accepted()
        }
      } ~
      put {
        /**
         * save configuration entry
         *
         * e.g. PUT http://localhost:8080/configuration
         * payload:	{ "key": "sbpm.debug", "label": "Enable debug mode.", "value": "true", "dataType": "Boolean" }
         * result: 202 Accepted -> content saved asynchronously
         */
        path("") {
          entity(as[Configuration]) { configuration =>
            execute(SaveConfiguration(configuration))
            // async call to database -> only send Accepted status code
            accepted()
          }
        }
      }
  })
}
