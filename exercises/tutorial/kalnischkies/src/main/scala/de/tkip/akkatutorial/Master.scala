package de.tkip.akkatutorial

import akka.actor.Props
import akka.actor.ActorSystem
import akka.routing.RoundRobinPool
import akka.actor.Actor
import scala.concurrent.Future
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration.Duration

class Master(system: ActorSystem, nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Actor {
  val router = system.actorOf(RoundRobinPool(nrOfWorkers).props(Props[Worker]), "router-of-e")
  val printer = system.actorOf(Props(new PrinterActor(system)), "printer-of-e")
  var results: Array[Double] = Array();

  def receive: Actor.Receive = {
    case Calculate() => {
      // If we get two of those messages, the world collapses
      results = Array()
      0.until(nrOfMessages).foreach(x => router ! new Work(x * nrOfElements, (x + 1) * nrOfElements))
    }
    case Result(r : Double) => {
      results = results :+ r
      if (results.length == nrOfMessages)
        printer ! eApproximation(results.filter(x =>  x != Double.NegativeInfinity && x != Double.PositiveInfinity).sum)
    }
  }
  
  
}
