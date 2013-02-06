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

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class TestExecutionInterfaceActor extends Actor with HttpService {
  implicit val timeout = Timeout(5 seconds)
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

  def receive = runRoute({
    formField("userid") { userID =>
      get {
        //READ
        path(IntNumber) { processInstanceID =>

          implicit val timeout = Timeout(5 seconds)

          //same get "composedFutur" as in ExecutionInterfaceActor but as instance of Debug trait
          val composedFuture = for {
            processInstanceFuture <- (persistanceActor ? GetProcessInstance(Some(processInstanceID.toInt))).mapTo[ProcessInstance]
            graphFuture <- (persistanceActor ? GetGraph(Some(processInstanceFuture.graphId)).asInstanceOf[Debug]).mapTo[Graph]
            historyFuture <- (subjectProviderManager ? GetHistory(userID.toInt, processInstanceID.toInt).asInstanceOf[Debug]).mapTo[HistoryAnswer]
            availableActionsFuture <- (subjectProviderManager ? GetAvailableActions(userID.toInt, processInstanceID.toInt).asInstanceOf[Debug]).mapTo[AvailableActionsAnswer]
          } yield JsObject(
            //            "graph" -> processInstanceFuture.graphs.toJson,
            "graph" -> graphFuture.graph.toJson,
            "history" -> historyFuture.h.toJson,
            "actions" -> availableActionsFuture.availableActions.toJson)

          complete(composedFuture)

        } ~
          //LIST
          path("") {
            implicit val timeout = Timeout(5 seconds)

            val composedFuture = for {
              instanceids <- (subjectProviderManager ? GetAllProcessInstances(userID.toInt).asInstanceOf[Debug]).mapTo[AllProcessInstanceIDsAnswer]
            } yield JsObject("instanceIDs" -> instanceids.processInstanceInfo.toJson)

            complete(composedFuture)
          }
      }
      
    }
  })

}

