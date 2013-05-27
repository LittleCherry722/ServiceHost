package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing._

/**
 * This Actor preprocesses the request, by reading the cookie informationen,
 * so the child actors have better access to it
 */
abstract class AbstractInterfaceActor extends Actor with HttpService {

  // TODO userId nicht ueberrschreibbar!
  protected var userId: Int = 1

  final def receive = extractCookie andThen routing

  private def extractCookie: PartialFunction[Any, RequestContext] = {
    case ctx: RequestContext => {
      // TODO ...
      // TODO namen vom generellen punkt
      val userIdCookie = ctx.request.cookies.find(_.name == "sbpm-userId")
      userId =
        if (userIdCookie.isDefined) userIdCookie.get.content.toInt
        // TODO else error
        else 1
      System.err.println("USERID: " + userId); // XXX weg

      // return ctx
      ctx
    }
  }

  /**
   * Implement routing over HttpService.runRoute
   */
  protected def routing: PartialFunction[RequestContext, Unit]
}