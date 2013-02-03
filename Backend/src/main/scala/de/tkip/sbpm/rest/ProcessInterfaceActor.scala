package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.actor.Props
import spray.routing._
import spray.http._
import akka.event.Logging
import akka.actor.ActorSystem
import akka.pattern.ask
import de.tkip.sbpm.rest.ProcessAttribute._
import java.util.concurrent.Future
import de.tkip.sbpm.persistence._
import de.tkip.sbpm.application.miscellaneous._
import spray.http.MediaTypes._
import spray.routing._
import de.tkip.sbpm.model._
import spray.httpx.SprayJsonSupport._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.JsonProtocol._
import spray.json._
import spray.httpx.marshalling.Marshaller
import de.tkip.sbpm.rest.SprayJsonSupport.JsObjectWriter
import de.tkip.sbpm.rest.SprayJsonSupport.JsArrayWriter
import de.tkip.sbpm.application.ProcessManagerActor
import de.tkip.sbpm.model.ProcessModel

/**
 * This Actor is only used to process REST calls regarding "process"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ProcessInterfaceActor extends Actor with PersistenceInterface {
  private lazy val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor

  /**
   *
   * usually a REST Api should at least implement the following functions:
   * - GET withouht parameter => list of entity
   * - GET with id => specific entity
   * - PUT without id => new entity
   * - PUT with id => update entity
   * - DELETE with id => delete entity
   *
   * For more information about how to design a RESTful API see:
   * http://ajaxpatterns.mrg/RESTful_Service#RESTful_Principles
   *
   * Nevertheless: If an URL does not represent a resource, like the "execution" API
   * it makes sense to step away from this general template
   *
   */
  def receive = runRoute({
    get {
      /**
       * get a list of all loadable or loaded processes
       * or load a process
       *
       * e.g. GET http://localhost:8080/process
       */
      // LIST
      path("") {
        formField("userid") { (userid) =>
          // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück
          completeWithQuery[Seq[Process]](GetProcess())
        }
      } ~
        // READ
        path(IntNumber) { id =>
          formField("userid") { userid =>
            completeWithQuery[Process](GetProcess(Some(id)))
          }
        }
    } ~
      post {
        /**
         * create a new process
         *
         * e.g. PUT http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        // CREATE
        path("") {
          formField("id", "name", "graph", "isCase") { (id, name, graph, isCase) =>

            val future = ActorLocator.persistenceActor ? SaveProcess(Process(None, name))
            val processID = future.mapTo[Int]
            val future1 = ActorLocator.persistenceActor ? SaveGraph(Graph(None, graph, new java.sql.Timestamp(System.currentTimeMillis()), processID.asInstanceOf[Int]))
            val graphID = future1.mapTo[Int]
            val future2 = ActorLocator.persistenceActor ? SaveProcess(Process(Option(processID.asInstanceOf[Int]), name,graphID.asInstanceOf[Int]))
            processID onSuccess {
              case id: Int =>
                val obj = JsObject("processID" -> id.toJson)
                complete(StatusCodes.Created, obj)
            }
            processID onFailure {
              case _ =>
                complete(StatusCodes.InternalServerError)
            }
            complete(StatusCodes.InternalServerError)

          }
        }
      } ~
      delete {
        /**
         * delete a process
         *
         * e.g. http://localhost:8080/process/12
         */
        // DELETE
        path(IntNumber) { id =>
          completeWithDelete(DeleteProcess(id), "Process could not be deleted. Entitiy with id %d not found.", id)
        }
      } ~
      put {
        /**
         * update an existing process
         *
         * e.g. PUT http://localhost:8080/process/12?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        //UPDATE
        path(IntNumber) { processID =>
          formField("id", "name", "graph", "isCase") { (id, name, graph, isCase) =>
            //execute next step (chosen by actionID)
            val future = ActorLocator.persistenceActor ? SaveGraph(Graph(Option[Int](id.toInt), graph, new java.sql.Timestamp(System.currentTimeMillis()), processID.asInstanceOf[Int]))
            val result = future.mapTo[Int]
            result onSuccess {
              case id: Int =>
                complete(StatusCodes.OK)
            }
            result onFailure {
              case _ =>
                complete(StatusCodes.InternalServerError)
            }
            complete(StatusCodes.InternalServerError)
          }
        }
      }
  })
}
