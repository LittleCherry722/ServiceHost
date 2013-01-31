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
  private lazy val processManager = ActorLocator.processManagerActor
  
  def receive = runRoute({
    formField("userid") { userId =>
      get {
        //READ
        path(IntNumber) { processInstanceID =>

          implicit val timeout = Timeout(5 seconds)
          
          case class Result(pm: ProcessModel, history: History, actions: Array[AvailableAction])
          
          val composedFuture = for {
            processInstanceFuture <- (processManager ? GetProcessInstance(userId.toInt, processInstanceID.toInt)).mapTo[ProcessInstanceAnswer]
            historyFuture <- (processManager ? GetHistory(userId.toInt, processInstanceID.toInt)).mapTo[HistoryAnswer]
            availableActionsFuture <- (processManager ? GetAvailableActions(userId.toInt, processInstanceID.toInt)).mapTo[AvailableActionsAnswer]
          } yield JsObject("graph" -> processInstanceFuture.graphs.toJson, 
        		  		   "history" -> historyFuture.h.toJson,
        		  		   "actions" -> availableActionsFuture.availableActions.toJson)
          
          complete(composedFuture)
          
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            val future = subjectProviderManager ? GetAllProcessInstanceIDs(userId.toInt)

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
