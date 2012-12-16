package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.http.HttpRequest
import akka.event.Logging

/**
 * This Actor is only used to process REST calls regarding "execution"
 */
// TODO when to choose HttpService and when HttpServiceActor
class ExecutionInterfaceActor extends Actor with HttpService {

  val logger = Logging(context.system, this)
  
  override def preStart() {
    logger.debug(context.self + " starts.")
  }
  
  override def postStop() {
    logger.debug(context.self + " stops.")
  }
  
  def actorRefFactory = context
  
  def receive = runRoute({ 
  	complete("not yet implemented")
  })
  
}