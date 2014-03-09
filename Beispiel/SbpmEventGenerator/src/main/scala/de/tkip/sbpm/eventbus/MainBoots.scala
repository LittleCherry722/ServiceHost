package de.tkip.sbpm.eventbus

import akka.actor._
import akka.actor.ActorSelection.toScala
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import ExecutionContext.Implicits.global
import scala.util.Random
import scala.swing._
import event._

object MainBoots extends App {
  val ui = new UI
  val system = ActorSystem("EventBus")
  val remotePubActor = system.actorSelection("akka.tcp://sbpm@127.0.0.1:2552/user/eventbus-remote-publish")
  val resolveTimeout = Duration(40000, MILLISECONDS)
  val f = remotePubActor.resolveOne(resolveTimeout)
  var remoteActor: Option[akka.actor.ActorRef] = None
  var running = false
  f onComplete {
    case Success(actor) =>
      ui.visible = true
      remoteActor = Some(actor)
    case Failure(e) => e.printStackTrace()
  }

  remotePubActor ! "Hello from remote app"

  import system.dispatcher

  private def startSbpmEventBusTrafficFlowMessage(): Unit = {
    running = true
    // run the loop in a new thread
    future {
      Thread.sleep(1000)
      while (running) {
        println("sending SbpmEventBusTrafficFlowMessage")
        remoteActor.get ! SbpmEventBusEvent("/traffic/darmstadt/flow", new SbpmEventBusTrafficFlowMessage(Random.nextInt(10), Random.nextInt(90) + 10))
        Thread.sleep(1000)
      }
    }
  }

  private def stopSbpmEventBusTrafficFlowMessage(): Unit = {
    running = false
  }

  class UI extends MainFrame {
    title = "Event Control"
    preferredSize = new Dimension(160, 120)
    val toggle = new ToggleButton { text = "Click to Start" }
    contents = toggle
    listenTo(toggle)
    reactions += {
      case ButtonClicked(component) if component == toggle =>
        toggle.text = if (toggle.selected) "started" else "stoped"
        if (toggle.selected) {
          startSbpmEventBusTrafficFlowMessage()
        } else {
          stopSbpmEventBusTrafficFlowMessage()
        }
    }
  }
}
