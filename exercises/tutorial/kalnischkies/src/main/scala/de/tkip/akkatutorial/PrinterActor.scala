package de.tkip.akkatutorial

import akka.actor.Actor
import akka.actor.ActorSystem

class PrinterActor(system: ActorSystem) extends Actor {
  def receive: Actor.Receive = {
    case eApproximation(r: Double) => {
      println("Approximation is: " + r)
      system.shutdown()
    }
  }
}