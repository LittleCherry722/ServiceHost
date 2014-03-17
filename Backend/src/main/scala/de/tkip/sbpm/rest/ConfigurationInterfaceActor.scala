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
import akka.event.Logging
import de.tkip.sbpm.model.Configuration
import de.tkip.sbpm.persistence.query.Configurations._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.directives.CompletionMagnet._
import spray.routing.directives.ContentTypeResolver._
import spray.http.StatusCodes._

/**
 * This Actor is only used to process REST calls regarding "configuration"
 */
class ConfigurationInterfaceActor extends InstrumentedActor with PersistenceInterface {
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
  def wrappedReceive = runRoute({
    get {
      /**
       * get a list of all configurations
       *
       * e.g. GET http://localhost:8080/configuration
       * result: JSON array of entities
       */
      path("") {
        completeWithQuery[Seq[Configuration]](Read.All)
      } ~
        /**
         * get configuration by key
         *
         * e.g. GET http://localhost:8080/configuration/sbpm.debug
         * result: 404 Not Found or JSON of entity
         */
        path(PathElement) { key =>
          completeWithQuery[Configuration](Read.ByKey(key), "Configuration with key '%s' not found.", key)
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
        	completeWithDelete(Delete.ByKey(key), "Configuration could not be deleted. Entity with key '%s' not found.", key)
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
            completeWithSave[Configuration, String](Save(configuration), configuration, pathForEntity(Entity.CONFIGURATION, "%s"))
          }
        }
      }
  })
}
