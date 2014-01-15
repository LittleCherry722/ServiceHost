package akka.tutorial.scala

import akka.actor.Actor
import akka.routing.RoundRobinRouter
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorSystem

object Solution extends App {
  
  calculate(5, 5, 5)
  
  
  case class Calculate
  case class Work(from: Int, nrOfElements: Int)
  case class Result(result: Double)
  case class eApproximation(finalResult: Double)

  class Worker extends Actor {
    def receive = {
      case Work(from, nrOfElements) => {
        val result = calculate(from, nrOfElements)
        sender ! Result(result)

      }
    }
    def calculate(from: Int, nrOfElements: Int): Double = {
      var result: Double = 0
      for (i <- from until (nrOfElements + from)) {
        result = result + 1 / fact(i)
      }
      result
    }

    def fact(n: Int): Double = {
      if (n == 0 || n == 1)
        1
      else {
        var res = 1
        for (i <- 1 until (n + 1)) {
          res = res * i
        }
        res
      }
    }
  }

  class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, printer: ActorRef) extends Actor {
    var finalResult: Double = 0
    var doneResults: Int = 0
    def receive = {
      case Calculate => {
        for (i <- 0 until nrOfMessages) {
          router ! Work(i * nrOfElements, nrOfElements)
        }
      }
      case Result(part) => {
        finalResult += part
        doneResults += 1
        if (doneResults == nrOfMessages) {
          printer ! eApproximation(finalResult)
          context.stop(self)
        }
      }
    }
    val router = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "router")
  }

  class PrinterActor extends Actor {
    def receive = {
      case eApproximation(result) => {
        println("Result: " + result)
        context.system.shutdown()
      }
    }
  }

  def calculate(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) {
    val system = ActorSystem()
    val printer = system.actorOf(Props[PrinterActor], name = "printer")
    val master = system.actorOf(Props(new Master(
        nrOfWorkers, nrOfMessages, nrOfElements, printer)), 
        name = "master")
    master ! Calculate
  }
}

