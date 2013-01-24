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
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONArray
import de.tkip.sbpm.ActorLocator

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ExecutionInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)

  override def preStart() {
    logger.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    logger.debug(getClass.getName + " stopped.")
  }

  def actorRefFactory = context

  private lazy val subjectProviderManager = ActorLocator.subjectProviderManagerActor

  def receive = runRoute({
    formField("userid") { userId =>

      get {
        //READ
        path(IntNumber) { processID =>
          formField("subject") { (subject) =>
            //return all information for a given process (graph, next actions (unique ID per available action), history)
            implicit val timeout = Timeout(5 seconds)
            var jsonResult = ""
            val future1 = subjectProviderManager ? new ReadProcess(userId.toInt, processID.toInt)
            val future2 = subjectProviderManager ? new GetHistory(userId.toInt, processID.toInt)
            val future3 = subjectProviderManager ? new GetAvailableActions(userId.toInt, processID.toInt)

            val result = for {
              graph <- future1.mapTo[Process]
              history <- future2.mapTo[History]
              actions <- future3.mapTo[Action]
            } yield List(graph, history, actions)

            result onSuccess {
              case objlist: List[Object] =>
                for (obj <- objlist) {
                  //jsonResult + obj.toJson
                }
            }
            result onFailure {
              case _ =>
                jsonResult = "an error occured"
            }

            complete(jsonResult)
          }
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            implicit val timeout = Timeout(5 seconds)
            val future = subjectProviderManager ? new ExecuteRequestAll(userId.toInt)
            val list = Await.result(future, timeout.duration).asInstanceOf[ExecutedListAnswer]
            complete(s"list.toJson")
          }

      } ~
        delete {
          //DELETE
          path(IntNumber) { processID =>
            //stop and delete given process
            subjectProviderManager ! new KillProcess(userId.toInt)
            complete("Process deleted")
          }
        } ~
        put {
          //CREATE
          path("") {
            formField("processId") { (processId) =>
              implicit val timeout = Timeout(5 seconds)
              val future = subjectProviderManager ? new ExecuteRequest(userId.toInt, processId.toInt)
              val instanceId: Int = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID
              complete(
                //marshalling
                new Envelope(Some(JsObject("instanceId" -> JsNumber(instanceId))), "ok"))
            }
          } ~
            //UPDATE
            path(IntNumber) { processID =>
              formField("actionID") { (actionID) =>
                //execute next step (chosen by actionID)

                subjectProviderManager ! new RequestAnswer(processID.toInt, actionID)
                complete("Process updated")
              }
            }
        }

    }
  })

}

