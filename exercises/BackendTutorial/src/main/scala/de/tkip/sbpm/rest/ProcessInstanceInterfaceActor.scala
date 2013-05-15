package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService
import de.tkip.sbpm.application.ReadSubject
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.rest.SprayJsonSupport._
import de.tkip.sbpm.application.ExecuteAction

class ProcessInstanceInterfaceActor extends Actor with HttpService {

  def actorRefFactory = context

  def receive = runRoute({
    get {
      path("") {
        complete("User /n, to get the n-th subject")
      } ~
        pathPrefix(IntNumber) { id =>
          println("request: " + ReadSubject(id))
          // TODO
          complete("TODO")
        }
    } ~
      put {
        entity(as[ExecuteAction]) { json =>
          println("request: " + json)
          // TODO
          complete("complete put")
        }
      } ~
      post {
        //TODO
        complete("complete post")
      }
  })
}
