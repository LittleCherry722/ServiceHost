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

/**
 * This Actor is only used to process "get" REST calls regarding "execution" mixed with the debug trait
 */
// TODO when to choose HttpService and when HttpServiceActor
class TestExecutionInterfaceActor extends Actor with HttpService {

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

            //val future1 = subjectProviderManager ? new ReadProcess(userId = 1, processInstanceID = 1) with Debug
            //val graph = Await.result(future1, timeout.duration).asInstanceOf[ReadProcessAnswer].pm;

            /**
             * get request is mixed with the debug trait which is evaluated in the processinstance actor and hardcoded data is sent back
             */
            val future2 = subjectProviderManager ? new GetHistory(userID = 1, processInstanceID = 1) with Debug
            val history = Await.result(future2, timeout.duration).asInstanceOf[History];

            //val future3 = subjectProviderManager ? new GetAvailableActions(userId = 1, processInstanceID = 1) with Debug
            //val actions = Await.result(future3, timeout.duration).asInstanceOf[HistoryAnswer];

            //marshalling not implemented yet
            complete("history.toJson")
          }
        } ~
          //LIST
          path("") {
            //List all executed process (for a given user)
            implicit val timeout = Timeout(5 seconds)
            val future = subjectProviderManager ? new ExecuteRequestAll(userId.toInt)
            val list = Await.result(future, timeout.duration).asInstanceOf[ExecutedListAnswer]
            //marshalling not implemented yet
            complete("list.toJson")
          }

      }

    }
  })

}

