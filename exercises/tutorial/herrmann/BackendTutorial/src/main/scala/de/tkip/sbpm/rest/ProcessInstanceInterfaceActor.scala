package de.tkip.sbpm.rest

import scala.concurrent.Await

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout

import spray.routing.HttpService

import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._

class ProcessInstanceInterfaceActor extends Actor with HttpService {

  private lazy val processInstanceActor =
    ActorLocator.actor(ActorLocator.processInstanceActorName)

  def actorRefFactory = context

  implicit val timeout = Timeout(3000)
  def receive = runRoute({
    get {
      path("") {
        complete("Use /n, to get the n-th subject")
      } ~
        pathPrefix(IntNumber) { id =>
          println("request: " + ReadSubject(id))
          val future = (processInstanceActor ? ReadSubject(id))
            .mapTo[SubjectAnswer]
          val result = Await.result(future, timeout.duration)
          complete(result)
        }
    } ~
      put {
        path("") {
//          complete("Use /n, to control the n-th subject")
          entity(as[ChangeTestPair]) { msg =>
            processInstanceActor ! msg
            complete("executed")
          }
        } ~
          path("restart") {
            processInstanceActor ! RestartExecution
            complete("restarted") // TODO vorher irgentwie checken?
          } ~
          pathPrefix(IntNumber) { id =>
            entity(as[ActionHeader]) { json =>
              println("request: " + json)
              processInstanceActor ! ExecuteAction(id, json.action)
              // TODO
              complete("executed")
            }
          }
      }
  })
}
