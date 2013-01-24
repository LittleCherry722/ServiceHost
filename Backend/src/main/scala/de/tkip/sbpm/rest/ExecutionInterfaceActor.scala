package de.tkip.sbpm.rest

import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application._
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.routing._
import scala.concurrent.Await
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.json._
import de.tkip.sbpm.application.miscellaneous._

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ExecutionInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(context.self + " starts.")
  }

  override def postStop() {
    logger.debug(context.self + " stops.")
  }

  def actorRefFactory = context

  def receive = runRoute({
    formField("userid") { userId =>

      get {
        //READ
        path(IntNumber) { processID =>
          formField("subject") { (subject) =>
            //return all information for a given process (graph, next actions (unique ID per available action), history)
            implicit val timeout = Timeout(5 seconds)
            var jsonResult: Envelope = null
            val future1 = context.actorFor("/user/SubjectProviderManager") ? ReadProcess(userId.toInt, processID.toInt)
            val future2 = context.actorFor("/user/SubjectProviderManager") ? GetHistory(userId.toInt, processID.toInt)
            val future3 = context.actorFor("/user/SubjectProviderManager") ? GetAvailableActions(userId.toInt, processID.toInt)

            val result = for {
              graph <- future1.mapTo[ProcessModel]
              history <- future2.mapTo[History]
              actions <- future3.mapTo[Action]
            } yield JsObject("graph" -> graph.toJson, "history" -> history.toJson, "actions" -> actions.toJson)

            result onSuccess {
              case obj: JsObject =>
                jsonResult = Envelope(Some(JsObject("result" -> obj)), StatusCodes.OK.toString)
            }

            result onFailure {
              case _ =>
                jsonResult = Envelope(Some(JsObject("result" -> JsObject())), StatusCodes.InternalServerError.toString)
            }
            complete(jsonResult)
          }
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            implicit val timeout = Timeout(5 seconds)
            var jsonResult: Envelope = null
            val future = context.actorFor("/user/SubjectProviderManager") ? ExecuteRequestAll(userId.toInt)

            val result = for {
              instanceids <- future.mapTo[Int]
            } yield JsArray(instanceids.toJson)

            result onSuccess {
              case obj: JsArray =>
                jsonResult = Envelope(Some(JsObject("list" -> obj)), StatusCodes.OK.toString)
            }

            result onFailure {
              case _ =>
                jsonResult = Envelope(Some(JsObject("list" -> JsObject())), StatusCodes.InternalServerError.toString)
            }
            complete(jsonResult)
          }
      } ~
        delete {
          //DELETE
          path(IntNumber) { processID =>
            //stop and delete given process
            context.actorFor("/user/SubjectProviderManager") ! KillProcess(userId.toInt)
            complete(StatusCodes.OK.toString)
          }
        } ~
        put {
          //UPDATE
          path(IntNumber) { processID =>
            formField("actionID") { (actionID) =>
              //execute next step (chosen by actionID)

              val future = context.actorFor("/user/SubjectProviderManager") ! RequestAnswer(processID.toInt, actionID)
              complete(StatusCodes.OK.toString)
              //not yet implemented
            }
          }
        } ~
        post { //CREATE
          path("") {
            formField("processId") { (processId) =>
              implicit val timeout = Timeout(5 seconds)
              val future = context.actorFor("/user/SubjectProviderManager") ? ExecuteRequest(userId.toInt, processId.toInt)
              var jsonResult: Envelope = null

              val result = for {
                instanceid <- future.mapTo[Int]
              } yield JsObject("InstanceID" -> instanceid.toJson)

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
        }

    }
  })

}

