package de.tkip.servicehost

import akka.actor.ActorRef
import akka.event._
import akka.util.Subclassification
import akka.actor.actorRef2Scala

case class SbpmEventBusEvent(channel: String, message: Any)
case class SbpmEventBusTextMessage(text: String)

object SbpmEventBus extends EventBus with SubchannelClassification {
  type Event = SbpmEventBusEvent
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