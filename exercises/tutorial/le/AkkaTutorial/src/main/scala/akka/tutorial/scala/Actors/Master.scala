package akka.tutorial.scala.Actors

import akka.actor.{ Actor, ActorRef, Props }
import akka.routing.RoundRobinRouter

import akka.tutorial.scala.message.Calculate
import akka.tutorial.scala.message.Work
import akka.tutorial.scala.message.eApproximation

/**
 * nrOfWorkers – Wie viele Worker sollen verwendet werden
 * nrOfMessages – Wie viele Chunks sollen an die Worker verteilt werden
 * nrOfElements – welchen Umfang sollen die Chunks haben
 */
class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Actor {
  var result = 0.0;
  var count = 0;

  def receive = {
    case Calculate =>
      {
        println("begin calculation")
        val routerProps = Props(new Worker).withRouter(RoundRobinRouter(nrOfWorkers))
        val router = context.actorOf(routerProps)
        for (i <- 0 to nrOfMessages - 1) {
          router ! new Work(i * nrOfElements, (i + 1) * nrOfElements)
        }
      }

    case res: Double =>
      {
        result += res
        count = count + 1;
        if (count == nrOfMessages) {          
          val printer = context.actorOf(Props(new PrinterActor()))
          printer ! new eApproximation(result)
        }
      }

    case _ => println("received unknown object type")
  }
}