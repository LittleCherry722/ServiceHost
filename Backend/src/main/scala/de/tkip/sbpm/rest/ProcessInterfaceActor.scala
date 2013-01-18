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

/**
 * This Actor is only used to process REST calls regarding "process"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ProcessInterfaceActor(val subjectProviderManagerActorRef: SubjectProviderManagerActorRef,
  val persistenceActorRef: PersistenceActorRef) extends Actor with HttpService {

  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(context.self + " starts.")
  }

  override def postStop() {
    logger.debug(context.self + " stops.")
  }

  def actorRefFactory = context

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
          implicit val timeout = Timeout(5 seconds)
          // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück
          val future = context.actorFor("/usr/PersistenceActor") ? new GetProcess(None, None)
          val list = Await.result(future, timeout.duration)

          complete("not yet marsheld")
        }
      }
      // READ
      path("processID") { processID =>
        formField("userid") { (userid) =>
          implicit val timeout = Timeout(5 seconds)
          val future = context.actorFor("/usr/PersistenceActor") ? new GetProcess(Option(processID.toString.toInt), None)
          val process = Await.result(future, timeout.duration)
          complete("not yet marsheld")
        }
      }
    } ~
      put {
        /**
         * create a new process
         *
         * e.g. PUT http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
    	// CREATE
        path("") {
          formField("userid", "name", "graph", "isCase") { (userid, name, graph, isCase) =>
            implicit val timeout = Timeout(5 seconds)
            val future = context.actorFor("/usr/SubjectProviderManager") ? new CreateProcess(userid.toInt, name, graph.asInstanceOf[ProcessModel])
            val instanceid = Await.result(future, timeout.duration).asInstanceOf[ProcessCreated].processID

              complete(
                //marshalling
                new Envelope(Some(JsObject("instanceId" -> JsNumber(instanceid))), "ok"))
          }
        } ~
          /**
           * update an existing process
           *
           * e.g. PUT http://localhost:8080/process/12?graph=GraphAsJSON&subjects=SubjectsAsJSON
           */
          // UPDATE
          path(IntNumber) { procecssID =>
            formField("name", "graph", "isCase") { (name, graph, isCase) =>
              implicit val timeout = Timeout(5 seconds)
              val future = context.actorFor("/usr/SubjectProviderManager") ? new UpdateProcess(procecssID, name, graph.asInstanceOf[ProcessModel])

              complete("error not yet implemented")
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
            persistenceActorRef ! DeleteProcess(name.asInstanceOf[Int])

            complete("error not yet implemented")

          }
          formField("userid") { (userid) =>
            subjectProviderManagerActorRef ! KillProcess(id)

            complete("error not yet implemented")
          }

          complete("'delete with id' not yet implemented")

        }
      }

  })
}
