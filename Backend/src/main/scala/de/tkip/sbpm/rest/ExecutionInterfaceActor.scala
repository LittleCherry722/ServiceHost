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

          val composedFuture = for {
            processInstanceFuture <- (processManager ? GetProcessInstance(userId.toInt, processInstanceID.toInt)).mapTo[ProcessInstanceAnswer]
            historyFuture <- (processManager ? GetHistory(userId.toInt, processInstanceID.toInt)).mapTo[HistoryAnswer]
            availableActionsFuture <- (subjectProviderManager ? GetAvailableActions(userId.toInt, processInstanceID.toInt)).mapTo[AvailableActionsAnswer]
          } yield JsObject("graph" -> processInstanceFuture.graphs.toJson,
            "history" -> historyFuture.h.toJson,
            "actions" -> availableActionsFuture.availableActions.toJson)

          complete(composedFuture)

        } ~
          //LIST
          path("") {
            implicit val timeout = Timeout(5 seconds)

            val composedFuture = for {
              instanceids <- (processManager ? GetAllProcessInstanceIDs(userId.toInt)).mapTo[AllProcessInstanceIDsAnswer]
            } yield JsObject("instanceIDs" -> instanceids.processInstanceIDs.toJson)

            complete(composedFuture)
          }

      } ~
        delete {
          //DELETE
          path(IntNumber) { processID =>
            //stop and delete given process

            implicit val timeout = Timeout(5 seconds)

            val composedFuture = for {
              kill <- (processManager ? KillProcess(userId.toInt)).mapTo[KillProcessAnswer]
            } yield JsObject("instanceIDs" -> kill.success.toJson)

            complete(composedFuture)
          }
        } ~
        put {
          //UPDATE
          path(IntNumber) { processInstanceID =>
            formField("ExecuteAction") { (action) =>
              //execute next step (chosen by actionID)

              implicit val timeout = Timeout(5 seconds)

              case class ExecuteActionCreator(userID: UserID,
                                              processInstanceID: ProcessInstanceID,
                                              subjectID: SubjectID,
                                              stateID: StateID,
                                              stateType: String,
                                              actionInput: String)
              implicit val executeActionFormat = jsonFormat6(ExecuteActionCreator)

              val composedFuture = for {
                update <- (subjectProviderManager ? ExecuteAction(action.asJson.convertTo[ExecuteActionCreator])).mapTo[ExecuteActionAnswer]
              } yield JsObject()

              //
              complete(composedFuture)

              //              val future = subjectProviderManager ? UpdateRequest(processID.toInt, actionID)
              //              val result = future.mapTo[Boolean]
              //              result onSuccess {
              //                case obj: Boolean =>
              //                  if (obj)
              //                    complete(StatusCodes.OK)
              //                  else
              //                    complete(StatusCodes.InternalServerError)
              //              }
              //              result onFailure {
              //                case _ =>
              //                  complete(StatusCodes.InternalServerError)
              //              }
              //              complete(StatusCodes.InternalServerError)
            }

          }
        } ~
        post { //CREATE
          path("") {
            formField("processID") { (processId) =>

              implicit val timeout = Timeout(5 seconds)

              val composedFuture = for {
                instanceid <- (processManager ? CreateProcessInstance(userId.toInt, processId.toInt)).mapTo[ProcessInstanceCreated]
              } yield JsObject("instanceIDs" -> instanceid.processInstanceID.toJson)

              complete(composedFuture)
            }
          }
        }
    }
  })
}
