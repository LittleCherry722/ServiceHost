package de.tkip.sbpm.rest

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.AvailableAction
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.httpx.marshalling.Marshaller
import spray.json._
import spray.routing._
import spray.util.LoggingContext
import akka.actor.Props
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.ExecuteActionAnswer
import de.tkip.sbpm.persistence.GetProcessInstance
import de.tkip.sbpm.persistence.GetGraph
import scala.concurrent.Await
import de.tkip.sbpm.application.subject.mixExecuteActionWithRouting
import scala.concurrent.ExecutionContext

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ExecutionInterfaceActor extends Actor with HttpService {
  implicit val timeout = Timeout(5 seconds)
  override implicit def executionContext = ExecutionContext.Implicits.global
  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler.fromPF {
      case e: Exception => ctx =>
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
    }

  def actorRefFactory = context

  private lazy val subjectProviderManager = ActorLocator.subjectProviderManagerActor
  private lazy val persistanceActor = ActorLocator.persistenceActor

  private val userID = 1 // TODO erstmal fest setzen spaeter aus cookies 

  def receive = runRoute({
    // TODO: aus cookie auslesen
    //    formField("userid") { userID =>
    get {
      //READ
      path(IntNumber) { processInstanceID =>

        implicit val timeout = Timeout(5 seconds)
        //        val future = persistanceActor ? GetProcessInstance(Some(processInstanceID.toInt))
        //        val result = Await.result(future, timeout.duration).asInstanceOf[Some[ProcessInstance]]
        //        if (result.isDefined) {
        // TODO for testreasons processInstanceID 1 will be mixed with Debug
        val composedFuture = for {
          processInstanceFuture <- (persistanceActor ? GetProcessInstance(Some(processInstanceID.toInt))).mapTo[Option[ProcessInstance]]
          graphFuture <- {
            if (processInstanceFuture.isDefined)
              (persistanceActor ? GetGraph(Some(processInstanceFuture.get.graphId))).mapTo[Option[Graph]]
            else
              throw new Exception("Processinstance '" + processInstanceID + "' does not exist.")
          }
          historyFuture <- (subjectProviderManager ? {
            if (processInstanceID == 1)
              new GetHistory(userID.toInt, processInstanceID.toInt) with Debug
            else
              GetHistory(userID.toInt, processInstanceID.toInt)
          }).mapTo[HistoryAnswer]
          availableActionsFuture <- (subjectProviderManager ? {
            if (processInstanceID == 1)
              new GetAvailableActions(userID.toInt, processInstanceID.toInt) with Debug
            else
              GetAvailableActions(userID.toInt, processInstanceID.toInt)
          }).mapTo[AvailableActionsAnswer]
        } yield JsObject(
          "processId" -> processInstanceFuture.get.processId.toJson,
          "graph" -> {
            if (graphFuture.isDefined)
              graphFuture.get.graph.toJson
            else
              "".toJson
          },
          "history" -> historyFuture.history.toJson,
          "actions" -> availableActionsFuture.availableActions.toJson)
        complete(composedFuture)
        //        } else {
        //        	complete("The requested process is not running")
        //        }
      } ~
        //LIST
        path("") {
          implicit val timeout = Timeout(5 seconds)
          val future = (subjectProviderManager ? GetAllProcessInstances(userID.toInt)).mapTo[AllProcessInstancesAnswer]
          val result = Await.result(future, timeout.duration)

          complete(result.processInstanceInfo)
        }

    } ~
      delete {
        //DELETE
        path(IntNumber) { processInstanceID =>
          //stop and delete given process instance
          implicit val timeout = Timeout(5 seconds)
          // error gets caught automatically by the exception handler
          val future = subjectProviderManager ? KillProcessInstance(processInstanceID)
          val result = Await.result(future, timeout.duration).asInstanceOf[KillProcessInstanceAnswer]
          complete(StatusCodes.OK)
        }
      } ~
      put {
        //UPDATE
        pathPrefix(IntNumber) { processInstanceID =>
          path("^$"r) { regex =>
            entity(as[ExecuteAction]) { json =>
              //execute next step
              implicit val timeout = Timeout(5 seconds)
              val future = (subjectProviderManager ? mixExecuteActionWithRouting(json))
              val result = Await.result(future, timeout.duration).asInstanceOf[ExecuteActionAnswer]
              complete(StatusCodes.OK)
            }
          }
        }
      } ~
      post { //CREATE
        pathPrefix("") {
          path("^$"r) { regex =>
            entity(as[ProcessIdHeader]) { json =>
              implicit val timeout = Timeout(5 seconds)
              val future = subjectProviderManager ? CreateProcessInstance(userID.toInt, json.processId)
              val result = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated]
              complete(
                JsObject(
                  "id" -> result.processInstanceID.toJson,
                  "graph" -> result.graphJson.toJson,
                  "history" -> result.history.toJson,
                  "actions" -> result.availableActions.toJson))
            }
          }
        }
      }
  })
}
