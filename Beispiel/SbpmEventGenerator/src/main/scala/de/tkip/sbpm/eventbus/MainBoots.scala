package de.tkip.sbpm.eventbus

import akka.actor._
import akka.actor.ActorSelection.toScala
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import ExecutionContext.Implicits.global
import scala.util.Random
import scala.actors.ActorRef
import scala.swing._
import event._

object MainBoots extends App {
  val ui = new UI
  ui.visible = true
  val system = ActorSystem("default")
  val remotePubActor = system.actorSelection("akka.tcp://sbpm@127.0.0.1:2552/user/eventbus-remote-publish")
  val duration = Duration(1000, MILLISECONDS)
  val f = remotePubActor.resolveOne(duration)
  var s = true

  remotePubActor ! "Hello from remote app"
  remotePubActor ! SbpmEventBusEvent("/traffic/darmstadt/flow", SbpmEventBusTextMessage("Event 10 updated..."))

  import system.dispatcher

  private def startSbpmEventBusTrafficFlowMessage(): Unit = {
    s = true
    f onComplete {
    case Success(actor) =>
      //        val cancellable = system.scheduler.schedule(0 milliseconds, 1000 milliseconds){
      //        actor ! new SbpmEventBusTrafficFlowMessage(Random.nextInt(Integer.MAX_VALUE),Random.nextInt(Integer.MAX_VALUE))
      //      }
      while (s) {
        actor ! new SbpmEventBusTrafficFlowMessage(Random.nextInt(Integer.MAX_VALUE), Random.nextInt(Integer.MAX_VALUE))
        Thread.sleep(1000)
      }
    case Failure(e) => e.printStackTrace()
  }
  }

  private def stopSbpmEventBusTrafficFlowMessage(): Unit = {
    s = false
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