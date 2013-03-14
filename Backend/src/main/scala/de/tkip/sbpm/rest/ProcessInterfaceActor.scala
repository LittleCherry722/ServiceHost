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
import scala.concurrent.Await
import spray.util.LoggingContext
import de.tkip.sbpm.persistence.query._

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
   * - GET without parameter => list of entity
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
        completeWithQuery[Seq[Process]](Processes.Read())
      } /*~
        // READ
        pathPrefix(IntNumber) { id =>
          try {
            val persistenceActor = ActorLocator.persistenceActor
            val processFuture = (persistenceActor ? Processes.Read.ById(id))
            val processResult = Await.result(processFuture, timeout.duration).asInstanceOf[Option[Process]]
            if (processResult.isDefined) {
              val graphFuture = (persistenceActor ? Graphs.Read.ById(processResult.get.activeGraphId.get))
              val graphResult = Await.result(graphFuture, timeout.duration).asInstanceOf[Option[Graph]]
              complete(JsObject(
                "id" -> processResult.get.id.toJson,
                "name" -> processResult.get.name.toJson,
                "graph" -> graphResult.get.graph.toJson,
                "isCase" -> processResult.get.isCase.toJson,
                "startSubjects" -> processResult.get.startSubjects.toJson))
            } else {
              complete("Process with id " + id + " not found")
            }
          }

        }
    } ~
      post {
        /**
         * create a new process
         *
         * e.g. POST http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        // CREATE
        path("^$"r) { regex =>
          entity(as[GraphHeader]) { json =>

            val persistenceActor = ActorLocator.persistenceActor
            val processFuture = (persistenceActor ? GetProcess(None, Some(json.name)))
            val processResult = Await.result(processFuture, timeout.duration).asInstanceOf[Option[Process]]

            // PrÜfen, ob der Prozess bereits existiert
            // Prüfen, ob der Name 3 oder mehr Buchstaben enthält
            validate(!processResult.isDefined, "The processes name has to be unique!") {
              validate(json.name.length() >= 3, "The name hast to contain 3 or more letters!") {
                implicit val timeout = Timeout(5 seconds)
                val future = (persistanceActor ? SaveProcess(Process(None, json.name, -1, json.isCase, ""),
                  Option(Graph(None, json.graph, new java.sql.Timestamp(System.currentTimeMillis()), -1))))
                val result = Await.result(future, timeout.duration).asInstanceOf[(Some[Int], Some[Int])]
                complete(JsObject("id" -> result._1.get.toJson))
              }
            }
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
        path(IntNumber) { processID =>
          completeWithDelete(
            DeleteProcess(processID),
            "Process could not be deleted. Entitiy with id %d not found.",
            processID)
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
              // TODO warum macht man es bei POST mit einem befehl und bei PUT
              // mit 2 Befehlen?
              implicit val timeout = Timeout(5 seconds)
              //execute next step (chosen by actionID)
              val processFuture = (persistanceActor ? GetProcess(Option(id), None))
              val result = Await.result(processFuture, timeout.duration).asInstanceOf[Some[Process]]

              // Pr�fen, ob der Prozess existiert
              // Pr�fen, ob der Name 3 oder mehr Buchstaben enth�lt
              validate(result.isDefined, "The requested process does not exist") {
                validate(json.name.length() >= 3, "The name hast to contain 3 or more letters!") {
                  val graphFuture = (persistanceActor ? SaveGraph(Graph(Option(result.get.graphId), json.graph, new java.sql.Timestamp(System.currentTimeMillis()), id)))
                  Await.result(graphFuture, timeout.duration)
                  complete(StatusCodes.OK)
                }
              }
            }
          }
        }*/
      }
  })
}
