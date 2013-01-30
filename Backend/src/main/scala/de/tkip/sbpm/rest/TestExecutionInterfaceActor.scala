package de.tkip.sbpm.rest

import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application._
import spray.http.MediaTypes._
import spray.routing._
import scala.concurrent.Await
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import scala.util.parsing.json.JSONArray
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.ActorLocator
import spray.http.StatusCodes
import spray.http.StatusCode
import spray.http.HttpHeader
import spray.util.LoggingContext
import spray.httpx.marshalling.Marshaller
import de.tkip.sbpm.rest.SprayJsonSupport.JsObjectWriter
import de.tkip.sbpm.rest.SprayJsonSupport.JsArrayWriter
import akka.actor.Props

/**
 * This Actor is only used to process "get" REST calls regarding "execution" mixed with the debug trait
 */
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

  def receive = runRoute({
    formField("userid") { userId =>
      get {
        //READ
        path(IntNumber) { processID =>
            //return all information for a given process (graph, next actions (unique ID per available action), history)
            implicit val timeout = Timeout(5 seconds)
            val future1 = subjectProviderManager ? new ReadProcess(userId.toInt, processID.toInt) with Debug
            val future2 = subjectProviderManager ? new GetHistory(userId.toInt, processID.toInt) with Debug
            val future3 = subjectProviderManager ? new GetAvailableActions(userId.toInt, processID.toInt) with Debug

            val result = for {
              graph <- future1.mapTo[Process]
              history <- future2.mapTo[History]
              actions <- future3.mapTo[AvailableActionsAnswer]
            } yield JsObject("graph" -> graph.toJson, "history" -> history.toJson, "actions" -> actions.toJson)

            result onSuccess {
             case obj: JsObject =>
             complete(StatusCodes.OK, obj)
            }
            complete(StatusCodes.InternalServerError.toString)
         } ~
         //LIST
          path("") {
            //List all executed process (for a given user)
            implicit val timeout = Timeout(5 seconds)
            val future = subjectProviderManager ? new ExecuteRequestAll(userId.toInt) with Debug 

            val result = for {
              instanceids <- future.mapTo[Int]
            } yield JsArray(instanceids.toJson)

            result onSuccess {
              case array: JsArray =>
                complete(StatusCodes.OK, array)
            }

            complete(StatusCodes.InternalServerError.toString)
          }

      }

    }
  })

}

