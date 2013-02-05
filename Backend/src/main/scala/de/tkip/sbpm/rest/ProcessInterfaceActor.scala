package de.tkip.sbpm.rest

import akka.actor.Actor
import akka.actor.Props
import spray.routing._
import spray.http._
import akka.event.Logging
import akka.util.Timeout
import scala.concurrent.duration._
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
import scala.concurrent.Await
import spray.util.LoggingContext

/**
 * This Actor is only used to process REST calls regarding "process"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ProcessInterfaceActor extends Actor with PersistenceInterface {
  private lazy val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor
  private lazy val persistanceActor = ActorLocator.persistenceActor

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
        // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück
        completeWithQuery[Seq[Process]](GetProcess())
      } ~
        // READ
        pathPrefix(IntNumber) { id =>

          try {
            val persistenceActor = ActorLocator.persistenceActor
            val dataBaseQueryFuture = for {
              processFuture <- (persistenceActor ? GetProcess(Some(id))).mapTo[Option[Process]]
              graphFuture <- (persistenceActor ? GetGraph(Some(processFuture.get.graphId))).mapTo[Option[Graph]]
            } yield JsObject(
              "id" -> processFuture.get.id.toJson,
              "name" -> processFuture.get.name.toJson,
              "graph" -> graphFuture.get.graph.toJson)

            complete(Await.result(dataBaseQueryFuture, timeout.duration))
          } catch {
            case _ => notFound("Process with id " + id + " not found")
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
        path("^$"r) { regex =>
          entity(as[GraphHeader]) { json =>
          	implicit val timeout = Timeout(5 seconds)
            val composedFuture = for {
              processInstanceFuture <- (persistanceActor ? SaveProcess(Process(None, json.name),
                Option(Graph(None, json.graph, new java.sql.Timestamp(System.currentTimeMillis()), -1)))).mapTo[(Some[Int], Some[Int])]
            } yield JsObject(
              "processID" -> processInstanceFuture._1.get.toJson)
            complete(composedFuture)

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
        pathPrefix(IntNumber) { id =>
          path("^$"r) { regex =>
            entity(as[GraphHeader]) { json =>
          	implicit val timeout = Timeout(5 seconds)
              //execute next step (chosen by actionID)
              val future = (persistanceActor ? GetProcess(Option(id),None))
              val result = Await.result(future, timeout.duration).asInstanceOf[Some[Process]]

              val composedFuture = for {
                grapInstanceFuture <- (persistanceActor ? SaveGraph(Graph(Option(result.get.graphId), json.graph, new java.sql.Timestamp(System.currentTimeMillis()), id)))
              } yield JsObject()
              complete(composedFuture)

              /*
            result onSuccess {
              case id: Int =>
                complete(StatusCodes.OK)
            }
            result onFailure {
              case _ =>
                complete(StatusCodes.InternalServerError)
            }
            * */
            }
          }
        }
      }
  })

}
