package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService
import spray.routing.RequestContext
import de.tkip.sbpm.logging.DefaultLogging

/**
 * This Actor preprocesses the request, by reading the cookie informationen,
 * so the child actors have better access to it
 */
abstract class AbstractInterfaceActor extends Actor with HttpService with DefaultLogging{

  // TODO userId von subklassen nicht ueberrschreibbar machen!
  private var _userId: Int = 1
  final protected def userId = _userId

  final def receive = {
    case ctx: RequestContext => {
      // first extract the cookie information
      extractCookie(ctx)
      // then run the routing
      routing(ctx)
    }
  }

  private def extractCookie(ctx: RequestContext) {
    // TODO namen vom generellen punkt
    val userIdCookie = ctx.request.cookies.find(_.name == "sbpm-userId")
    _userId =
      if (userIdCookie.isDefined) userIdCookie.get.content.toInt
      // TODO else error
      else 1
  }

  /**
   * Implement routing over HttpService.runRoute
   */
  protected def routing: PartialFunction[RequestContext, Unit]
}
