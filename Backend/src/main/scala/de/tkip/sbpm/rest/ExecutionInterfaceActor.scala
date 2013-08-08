/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.rest

import scala.concurrent.duration._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import spray.http.StatusCodes
import spray.routing._
import spray.util.LoggingContext
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.application.history._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.logging.DefaultLogging
import java.net.URL
import java.net.HttpURLConnection
import java.io.DataOutputStream
import scala.collection.mutable.ArrayBuffer
import java.io.ByteArrayInputStream
import java.io.BufferedReader
import com.google.common.io.ByteStreams

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
class ExecutionInterfaceActor extends AbstractInterfaceActor with DefaultLogging {
  implicit val timeout = Timeout(5 seconds)

  implicit def exceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler {
      case e: IllegalArgumentException => ctx => {
        ctx.complete(StatusCodes.BadRequest, e.getMessage)
      }
      case e: Exception => ctx => {
        log.error(e, e.getMessage)
        ctx.complete(StatusCodes.InternalServerError, e.getMessage)
      }
    }

  def actorRefFactory = context

  private lazy val subjectProviderManager = ActorLocator.subjectProviderManagerActor

  private val googleUserId = 5
  private val googleUri = "http://127.0.0.1:8888/"
  import de.tkip.sbpm.proto.{ GAEexecution => msg }

  private def buildProto(action: ExecuteAction): Array[Byte] = {
    val executeActionBuilder = msg.ExecuteAction.newBuilder()

    val actionBuilder = msg.Action.newBuilder()
    actionBuilder.setUserID(1)
      .setProcessInstanceID(action.processInstanceID)
      .setSubjectID(action.subjectID.toInt) //TODO String
      .setStateID(action.stateID)
      .setStateType(action.stateType)
      // TODO stateTexts
      .setStateText("")

    val data = action.actionData
    val actionDataBuilder = msg.ActionData.newBuilder()
    actionDataBuilder.setText(data.text)
      .setExecutable(data.executeAble)
      .setTransitionType(data.transitionType)

    // Add the target users
    if (data.targetUsersData.isDefined) {
      val target = data.targetUsersData.get
      val targetUserBuilder = msg.TargetUserData.newBuilder()
      targetUserBuilder.setMin(target.min)
        .setMax(target.max)
      for (user <- target.targetUsers) {
        targetUserBuilder.addTargetUsers(user)
      }
      actionDataBuilder.setTargetUserData(targetUserBuilder)
    }
    // add the related subject
    if (data.relatedSubject.isDefined) {
      actionDataBuilder.setRelatedSubject(data.relatedSubject.get)
    }
    // add the messageContent (TODO)
    
//    actionBuilder.setActionData(d, actionDataBuilder.build())

    executeActionBuilder.setAction(actionBuilder.build())

    executeActionBuilder.build().toByteArray()
  }

  def buildScala(action: msg.Action): AvailableAction = {
    import scala.collection.JavaConversions._
    AvailableAction(
      action.getUserID().toInt,
      action.getProcessInstanceID(),
      action.getSubjectID().toString,
      action.getStateID(),
      action.getStateText(),
      action.getStateType(),
      (for (data <- action.getActionDataList())
        yield ActionData(
        data.getText(),
        data.getExecutable(),
        data.getTransitionType() //            data.getTa/
        // TODO...
        )).toArray)
  }

  private def routeToGoogle: PartialFunction[RequestContext, Unit] = {
    runRoute({
      get {
        //TODO...
        complete("test")
      } ~
        put {
          //UPDATE
          pathPrefix(IntNumber) { processInstanceID =>
            path("") {
              entity(as[ExecuteAction]) { json =>
                // create the url connection
                val url = new URL(googleUri + "post")
//                val url = new URL(googleUri + "camel/test")
                
                System.err.println("Send to GAE")
                
                val connection: HttpURLConnection =
                  url.openConnection().asInstanceOf[HttpURLConnection]

                connection.setDoInput(true)
                connection.setDoOutput(true)
                connection.setRequestMethod("POST")

                val proto = buildProto(json)
                
                System.err.println("Proto: " + proto)
                
                connection.setRequestProperty("Content-Length", proto.length.toString)
                
                System.err.println("LENGTH: " + proto.length)
                
                connection.setRequestProperty("Content-Type",
                  "plain/text");

                val out = new DataOutputStream(connection.getOutputStream())
                out.write(proto)
                out.flush()
                out.close()
                
                val in = connection.getInputStream()
                val protoResult = ByteStreams.toByteArray(in)
                // TODO convert proto -> case class
                System.err.println("Result:" + protoResult);
                val result = msg.ExecuteAction.parseFrom(protoResult)

                //execute next step
                complete(buildScala(result.getAction()))
              }
            }
          }
        }
    })
  }

  def routing = if (googleUserId == userId) routeToGoogle else runRoute({
    get {
      //READ
      path(IntNumber) { processInstanceID =>
        implicit val timeout = Timeout(5 seconds)
        val future = subjectProviderManager ? ReadProcessInstance(userId, processInstanceID)
        val result = Await.result(future, timeout.duration).asInstanceOf[ReadProcessInstanceAnswer]
        complete(result.answer)
      } ~
        // Show Actions
        path("action") {
          val availableActionsFuture =
            (subjectProviderManager ? GetAvailableActions(userId))
              .mapTo[AvailableActionsAnswer]
          val result = Await.result(availableActionsFuture, timeout.duration)
          complete(result.availableActions)
        } ~
        path("history") {
          //you cannot run statements inside the path-block like above, instead, put them into a block inside the complete statement
          complete({
            val getHistoryFuture = (ActorLocator.processManagerActor ? GetNewHistory()).mapTo[NewHistoryAnswer]
            val result = Await.result(getHistoryFuture, timeout.duration)
            result.history.entries.filter(x => x.userId == Some(userId) || x.userId == None)
          })
        } ~
        //LIST
        path("") {
          implicit val timeout = Timeout(5 seconds)
          val future = (subjectProviderManager ? GetAllProcessInstances(userId)).mapTo[AllProcessInstancesAnswer]
          val result = Await.result(future, timeout.duration)
          complete(result.processInstanceInfo)
        }

    } ~
      delete {
        //DELETE
        path(IntNumber) { processInstanceID =>
          //stop and delete given process instance
          implicit val timeout = Timeout(5 seconds)
          // error gets caught automatically by the exception handler
          val future = subjectProviderManager ? KillProcessInstance(processInstanceID)
          val result = Await.result(future, timeout.duration).asInstanceOf[KillProcessInstanceAnswer]
          complete(StatusCodes.NoContent)
        }
      } ~
      put {
        //UPDATE
        pathPrefix(IntNumber) { processInstanceID =>
          path("") {
            entity(as[ExecuteAction]) { json =>
              //execute next step
              implicit val timeout = Timeout(5 seconds)
              val future = (subjectProviderManager ? mixExecuteActionWithRouting(json))
              val result = Await.result(future, timeout.duration).asInstanceOf[ExecuteActionAnswer]
              complete(result.answer)
            }
          }
        }
      } ~
      post {
        //CREATE
        pathPrefix("") {
          path("") {
            entity(as[ProcessIdHeader]) { json =>
              implicit val timeout = Timeout(5 seconds)
              val future = subjectProviderManager ? CreateProcessInstance(userId, json.processId)
              val result = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated]
              complete(result.answer)
            }
          }
        }
      }
  })
}
