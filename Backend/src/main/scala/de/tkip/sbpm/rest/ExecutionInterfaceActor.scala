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
import spray.http.HttpHeader
import spray.util.LoggingContext
import spray.httpx.marshalling.Marshaller
import de.tkip.sbpm.rest.SprayJsonSupport.JsObjectWriter
import de.tkip.sbpm.rest.SprayJsonSupport.JsArrayWriter
import akka.actor.Props

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

  def receive = runRoute({
    formField("userid") { userId =>

      get {
        //READ
        path(IntNumber) { processID =>
          //return all information for a given process (graph, next actions (unique ID per available action), history)
          implicit val timeout = Timeout(5 seconds)
          val future1 = subjectProviderManager ? ReadProcess(userId.toInt, processID.toInt)
          val future2 = subjectProviderManager ? GetHistory(userId.toInt, processID.toInt)
          val future3 = subjectProviderManager ? GetAvailableActions(userId.toInt, processID.toInt)

          val result = for {
            graph <- future1.mapTo[Process]
            history <- future2.mapTo[History]
            actions <- future3.mapTo[AvailableActionsAnswer]
          } yield JsObject("graph" -> graph.toJson, "history" -> history.toJson, "actions" -> actions.availableActions.toJson)

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
            val future = subjectProviderManager ? ExecuteRequestAll(userId.toInt)

            val result = for {
              instanceids <- future.mapTo[Int]
            } yield JsArray(instanceids.toJson)

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
          path(IntNumber) { processID =>
            //stop and delete given process
            val future = context.actorOf(Props[ProcessManagerActor]) ? KillProcess(userId.toInt)

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
        } ~
        put {
          //UPDATE
          path(IntNumber) { processID =>
            formField("actionID") { (actionID) =>
              //execute next step (chosen by actionID)
              val future = subjectProviderManager ? UpdateRequest(processID.toInt, actionID)
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
            formField("processID") { (processId) =>
              val future = subjectProviderManager ? CreateProcessInstance(userId.toInt, processId.toInt)
              val result = future.mapTo[Int]
              result onSuccess {
                case id: Int =>
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
