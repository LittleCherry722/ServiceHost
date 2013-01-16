package de.tkip.sbpm.rest

import scala.concurrent.duration._
import akka.actor.Actor
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous.ExecuteRequest
import de.tkip.sbpm.application.miscellaneous.ProcessInstanceCreated
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
            complete("excute not yet implemented (in ProcessManagerActor)")
          }
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            complete("no.")

          }

      } ~
        delete {
          //DELETE
          path(IntNumber) { processID =>
            //stop and delete given process
            complete("error not yet implemented")
          }
        } ~
        put {
          //CREATE
          path("") {
            formField("processId") { (processId) =>
              implicit val timeout = Timeout(5 seconds)
              
              val future = context.actorFor("/user/SubjectProviderManager") ? new ExecuteRequest(userId.toInt, processId.toInt)

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
                complete("error not yet implemented")
              }
            }
        }

    }
  })

}

