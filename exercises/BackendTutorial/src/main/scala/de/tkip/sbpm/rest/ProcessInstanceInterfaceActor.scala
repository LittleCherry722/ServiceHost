package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService

class ProcessInstanceInterfaceActor extends Actor with HttpService {

  def actorRefFactory = context

  def receive = runRoute({
    get {
      // TODO
      complete("complete get")
    } ~
      put {
        //TODO
        complete("complete put")

      } ~
      post {
        //TODO
        complete("complete post")
      }
  })
}
