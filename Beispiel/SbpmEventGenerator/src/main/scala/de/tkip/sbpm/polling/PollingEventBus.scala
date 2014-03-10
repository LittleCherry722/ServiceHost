package de.tkip.sbpm.polling

import akka.actor.ActorRef
import akka.event._
import akka.util.Subclassification
import akka.actor.actorRef2Scala

case class PollingEventBusEvent(channel: String, message: Any)

object SbpmEventBus extends EventBus with SubchannelClassification {
  type Event = PollingEventBusEvent
  type Classifier = String
  type Subscriber = ActorRef
  
  protected def classify(event: Event): Classifier = event.channel

  val subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier) = x equals y
    def isSubclass(x: Classifier, y: Classifier) = x startsWith y
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event.message
  }
}