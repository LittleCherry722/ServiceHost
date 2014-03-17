package de.tkip.sbpm.instrumentation

import akka.actor.Actor

/**
 * Actor Stack for stackable traits for actors.
 * Allows to add before / after receive actions with traits
 * See https://gist.github.com/ericacm/7234947 for more information
 * and http://www.slideshare.net/EvanChan2/akka-inproductionpnw-scala2013 for rationale
*/
trait ActorStack extends Actor {
  /** Actor classes should implement this partialFunction for standard
    * actor message handling
    */
  def wrappedReceive: Receive

  /** Stackable traits should override and call super.receive(x) for
    * stacking functionality
    */
  def receive: Receive = {
    case x => if (wrappedReceive.isDefinedAt(x)) wrappedReceive(x) else unhandled(x)
  }
}
