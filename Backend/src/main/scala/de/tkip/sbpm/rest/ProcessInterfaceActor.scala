package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.http.HttpRequest
import akka.event.Logging

/**
 * This Actor is only used to process REST calls regarding "process"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ProcessInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)
  
  override def preStart() {
    logger.debug(context.self + " starts.")
  }
  
  override def postStop() {
    logger.debug(context.self + " stops.")
  }
  
  def actorRefFactory = context
  
  /**
   * usually a REST Api should at least implement the following functions:
   * - GET withouht parameter => list of entity
   * - GET with id => specific entity
   * - PUT without id => new entity
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
       * get a list of all processes
       * 
       * e.g. GET http://localhost:8080/process
       */
	    path("") {
	      complete("'get' not yet implemented")
	    } ~
	    /**
	     * get a process by id 
			 *
	     * e.g. GET http://localhost:8080/process/8
	     */
	    path(IntNumber) { id =>
	      complete("'get with id' not yet implemented")
	    }
    } ~
    put {
	    /**
	     * save a new process
	     * 
	     * e.g. PUT http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
	     */
	    path("") {
	      parameters("graph", "subjects") { (graph, subjects) =>
	        
	        // TODO insert process
	        
	      	complete("'put' not yet implemented")
	  		}
	    } ~
	    /**
	     * save an existing process
	     * 
	     * e.g. PUT http://localhost:8080/process/12?graph=GraphAsJSON&subjects=SubjectsAsJSON
	     */
	    path(IntNumber) { id =>
	      parameters("graph", "subjects") { (graph, subjects) =>
	        
	        // TODO update process
	        
	      	complete("'put with id' not yet implemented")
	  		}
	    }
    } ~
    delete {
      /**
       * delete a process
       * 
       * e.g. http://localhost:8080/process/12
       */
      path(IntNumber) { id =>
        
        // TODO delete process
        
        complete("'delete with id' not yet implemented")
        
      }
    }
    

  })
  
}