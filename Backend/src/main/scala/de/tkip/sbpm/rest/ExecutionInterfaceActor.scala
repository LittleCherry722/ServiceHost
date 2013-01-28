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
          val future1 = subjectProviderManager ? new ReadProcess(userId.toInt, processID.toInt)
          val future2 = subjectProviderManager ? new GetHistory(userId.toInt, processID.toInt)
          val future3 = subjectProviderManager ? new GetAvailableActions(userId.toInt, processID.toInt)

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
            val future = subjectProviderManager ? ExecuteRequestAll(userId.toInt)

            val result = for {
              instanceids <- future.mapTo[Int]
            } yield JsArray(instanceids.toJson)

            result onSuccess {
              case array: JsArray =>
                complete(StatusCodes.OK, array)
            }

            complete(StatusCodes.InternalServerError.toString)
          }

      } ~
        delete {
          //DELETE
          path(IntNumber) { processID =>
            //stop and delete given process
            context.actorOf(Props[ProcessManagerActor]) ? KillProcess(userId.toInt)
            complete(StatusCodes.NoContent)
          }
        } ~
        put {
          //UPDATE
          path(IntNumber) { processID =>
            formField("actionID") { (actionID) =>
              //execute next step (chosen by actionID)

              val future = subjectProviderManager ! RequestAnswer(processID.toInt, actionID)
              complete(StatusCodes.OK.toString)
              //not yet implemented
            }
          }
        } ~
        post { //CREATE
          path("") {
            formField("processId") { (processId) =>

              val future = subjectProviderManager ? ExecuteRequest(userId.toInt, processId.toInt)

              val result = for {
                instanceid <- future.mapTo[Int]
              } yield JsObject("InstanceID" -> instanceid.toJson)

              result onSuccess {
                case obj: JsObject =>
                  complete(StatusCodes.Created, obj)
              }

              complete(StatusCodes.InternalServerError.toString)
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
