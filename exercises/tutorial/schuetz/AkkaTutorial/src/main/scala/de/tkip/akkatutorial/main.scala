package de.tkip.akkatutorial

import akka.actor._
import akka.routing.RoundRobinRouter

object Main extends App {
  val system = ActorSystem()
  val master = system.actorOf(Props(new Master(2,20,5, system)))
  master ! new Calculate()
}

  case class Calculate()
  case class Work(from: Int, to: Int)
  case class Result(interResult: Double)
  case class eApproximation(e: Double)
  
  class Worker() extends Actor {
    def fac(i: Int): Int = {
      if (i < 2) {
        1
      } 
      else {
    	  i * fac(i - 1)
      }
    }
    
    override def receive: Actor.Receive = {
      case Work(from, to) =>   {
//        println("received work from: " + from + " to: " + to)
        sender ! new Result((for (i <- from to to) yield 1.0/fac(i)).sum)
      }
    }
    
  }
  
  class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, system: ActorSystem) extends Actor {
    val router = system.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfInstances = nrOfWorkers)))
    val printer = system.actorOf(Props(new PrinterActor(system: ActorSystem)), name = "printer")
    var approxE = 0.0
    var numOfResults = 0
    override def receive: Actor.Receive = {
      case Calculate() => {
        for (i <- 0 to (nrOfMessages, nrOfElements))  {
          router ! new Work(i, i + nrOfElements - 1)
          numOfResults += 1
        }
      }
      case Result(interResult) =>  {
//        println("received intermediate result: " + interResult)
        approxE += interResult
        numOfResults -= 1
        if (numOfResults  == 0) {
          printer ! new eApproximation(approxE)
        }
      }
    }
  }
  
  class PrinterActor(system: ActorSystem) extends Actor {
    override def receive: Actor.Receive = {
      case eApproximation(e) =>  {
        println("Das approximierte Ergebnis ist: " + e)
        system.shutdown
      }
    }
  }
  
