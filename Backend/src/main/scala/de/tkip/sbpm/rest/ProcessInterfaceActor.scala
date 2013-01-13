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
      path("") {
        parameters("load") { load =>
          if (load == "loadable") {

            implicit val timeout = Timeout(5 seconds)
            /*
            
            Musste auskommentiert werden, da GetProcess nicht gefunden werden konnte.
             
            val future = persistenceActorRef ? GetProcess
            val result = Await.result(future, timeout.duration)
			
			*/
            complete("Marshelled result")

          } else if (load == "loaded") {
            //TODO
            complete("error")
          } else complete("'error")
        } ~
          parameters("id", "userid") { (id, userid) =>
            implicit val timeout = Timeout(5 seconds)
            // Anfrage an den Persisence Actor liefert eine Liste von Graphen zurück
            val future = persistenceActorRef ? GetProcess(Option(id.asInstanceOf[Int]))
            val result = Await.result(future, timeout.duration)

            complete("result")
          }
      }
    } ~
      put {
        /**
         * create a new process
         *
         * e.g. PUT http://localhost:8080/process?graph=GraphAsJSON&subjects=SubjectsAsJSON
         */
        path("") {
          parameters("graph", "subjects", "userid") { (graph, subjects, userid) =>
            implicit val timeout = Timeout(5 seconds)
            // TODO Wer vergibt die UserID
            //            val future = subjectProviderManagerActorRef ? de.tkip.sbpm.application.miscellaneous.CreateSubjectProvider(userid.asInstanceOf[Int])
            val future = subjectProviderManagerActorRef ? de.tkip.sbpm.application.miscellaneous.CreateSubjectProvider()
            val result = Await.result(future, timeout.duration)
            complete("Marshelled result")
          }
        } ~
          /**
           * save an existing process
           *
           * e.g. PUT http://localhost:8080/process/12?graph=GraphAsJSON&subjects=SubjectsAsJSON
           */
          path(IntNumber) { id =>
            parameters("graph", "subjects", "userid") { (graph, subjects, userid) =>
              // TODO update process
              // Hier kam ein Fehler 
              //              persistenceActorRef ! SaveProcess(Option(id), "wo kommt der name her?", graph, subjects)

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
        path(IntNumber) { id =>
          parameters("name", "userid") { (name, userid) =>
            persistenceActorRef ! DeleteProcess(name.asInstanceOf[Int])

            complete("error not yet implemented")

          }
          parameters("userid") { (userid) =>
            subjectProviderManagerActorRef ! KillProcess(id)

            complete("error not yet implemented")
          }

          complete("'delete with id' not yet implemented")

        }
      }

  })
}
