package de.tkip.sbpm.rest

import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.util.parsing.json.JSONArray

import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.http.HttpHeader
import spray.httpx.marshalling.Marshaller
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.util.LoggingContext
import spray.routing._

import de.tkip.sbpm.application._
import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.rest.SprayJsonSupport.JsObjectWriter
import de.tkip.sbpm.rest.SprayJsonSupport.JsArrayWriter
import de.tkip.sbpm.persistence.GetProcess

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ExecutionInterfaceActor extends Actor with HttpService {
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
  private lazy val persistenceActor = ActorLocator.persistenceActor

  def receive = runRoute({
    formField("userid") { userId =>

      get {
        //READ
        path(IntNumber) { processInstanceID =>
          //return all information for a given process (graph, next actions (unique ID per available action), history)
          implicit val timeout = Timeout(5 seconds)
          // TODO the execution has no extra graph

          val result = for {
            //            graph <- (persistenceActor ? GetProcess(Some(processInstanceID.toInt))).mapTo[Process]
            history <- (subjectProviderManager ? GetHistory(userId.toInt, processInstanceID.toInt)).mapTo[History]
            actions <- (subjectProviderManager ? GetAvailableActions(userId.toInt, processInstanceID.toInt)).mapTo[AvailableActionsAnswer]
          } yield JsObject( /*"graph" -> graph.toJson, */ "history" -> history.toJson, "actions" -> actions.availableActions.toJson)

          result onSuccess {
            case obj: JsObject =>
              complete(StatusCodes.OK, obj)
          }
          result onFailure {
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
          complete(StatusCodes.InternalServerError)
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            val future = subjectProviderManager ? GetAllProcessInstanceIDs(userId.toInt)

            val result = for {
              instanceids <- future.mapTo[AllProcessInstanceIDsAnswer]
            } yield JsArray(instanceids.processInstanceIDs.toJson)

            result onSuccess {
              case array: JsArray =>
                complete(StatusCodes.OK, array)
            }
            result onFailure {
              case _ =>
                complete(StatusCodes.InternalServerError)
            }
            complete(StatusCodes.InternalServerError)
          }

      } ~
        delete {
          //DELETE
          path(IntNumber) { processInstanceID =>
            //stop and delete given process
            val future = subjectProviderManager ? KillProcess(processInstanceID)

            val result = future.mapTo[KillProcessAnswer]

            result onSuccess {
              case KillProcessAnswer(_, obj: Boolean) =>
                if (obj)
                  complete(StatusCodes.OK)
                else
                  complete(StatusCodes.InternalServerError)
            }
            result onFailure {
              case _ =>
                complete(StatusCodes.InternalServerError)
            }
            complete(StatusCodes.InternalServerError)
          }
        } ~
        put {
          //UPDATE
          path(IntNumber) { processInstanceID =>
            formField("actionID") { (actionID) =>
              // TODO change Action execution
              //execute next step (chosen by actionID)
              val future = subjectProviderManager ? UpdateRequest(processInstanceID.toInt, actionID)
              val result = future.mapTo[Boolean]
              result onSuccess {
                case obj: Boolean =>
                  if (obj)
                    complete(StatusCodes.OK)
                  else
                    complete(StatusCodes.InternalServerError)
              }
              result onFailure {
                case _ =>
                  complete(StatusCodes.InternalServerError)
              }
              complete(StatusCodes.InternalServerError)
            }

          }
        } ~
        post { //CREATE
          path("") {
            formField("processID") { (processID) =>
              val future = subjectProviderManager ? CreateProcessInstance(userId.toInt, processID.toInt)
              val result = future.mapTo[ProcessInstanceCreated]
              result onSuccess {
                case ProcessInstanceCreated(_, id: Int) =>
                  val obj = JsObject("InstanceID" -> id.toJson)
                  complete(StatusCodes.Created, obj)
              }
              result onFailure {
                case _ =>
                  complete(StatusCodes.InternalServerError)
              }
              complete(StatusCodes.InternalServerError)
            }
          }
        }
    }
  })

  //  case class Status(status: StatusCode) extends HttpHeader {
  //    def name = "Status"
  //    def lowercaseName = "status"
  //    def value = status.toString
  //  }
  //
  //  protected def complete[A](entity: A, status: StatusCode)(implicit marshaller: Marshaller[A]) =
  //    respondWithHeader(Status(status)) {
  //      _.complete(entity)
  //    }

}
