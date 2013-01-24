package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.actor.Props
import spray.routing._
import spray.http._
import MediaTypes._
import spray.http.HttpRequest
import akka.event.Logging
import akka.actor.ActorSystem
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await
import de.tkip.sbpm.rest.ProcessAttribute._
import java.util.concurrent.Future
import de.tkip.sbpm.persistence._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.persistence.GetProcess
import spray.http.MediaTypes._
import spray.routing._
import scala.concurrent.Await
import de.tkip.sbpm.model._
import spray.json.JsObject
import spray.json.JsNumber
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json.JsValue
import de.tkip.sbpm.ActorLocator

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
   * http://ajaxpatterns.org/RESTful_Service#RESTful_Principles
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
          formField("userid", "name", "graph", "isCase") { (userid, name, graph, isCase) =>
            val future = subjectProviderManagerActor ? CreateProcess(userid.toInt, name, graph.asInstanceOf[ProcessGraph])
            var jsonResult: Envelope = null

            val result = for {
              instanceid <- future.mapTo[Int]
            } yield JsObject("InstanceID" -> JsNumber(instanceid))

            //              val aresult =   future.mapTo[Int]
            //               JsObject( "InstanceID" -> aresult.toJson)

            result onSuccess {
              case obj: JsObject =>
                jsonResult = Envelope(Some(obj), StatusCodes.Created.toString)
            }

            result onFailure {
              case _ =>
                jsonResult = Envelope(Some(JsObject("InstanceID" -> JsObject())), StatusCodes.InternalServerError.toString)
            }
            complete(jsonResult)
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
          formField("name", "userid") { (name, userid) =>
            completeWithDelete(DeleteProcess(id), "Process could not be deleted. Entitiy with id %d not found.", id)
          } ~
            formField("userid") { (userid) =>
              subjectProviderManagerActor ! KillProcess(id)

              complete(StatusCodes.NoContent)
            }
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
          formField("actionID") { (actionID) =>
            //execute next step (chosen by actionID)

            val future = subjectProviderManagerActor ! RequestAnswer(processID.toInt, actionID)
            complete(StatusCodes.OK.toString)
            //not yet implemented
          }
        }
      }
  })
}
