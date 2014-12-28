package de.tkip.akkatutorial

import akka.actor._
import akka.routing.RoundRobinRouter
import annotation.tailrec

object Main extends App {

  calculate(nrOfWorkers = 4, nrOfElements = 2500, nrOfMessages = 50)

  sealed trait eMessage
  case object Calculate extends eMessage
  case class Work(start: Long, nrOfElements: Int) extends eMessage
  case class Result(value: Double) extends eMessage
  case class eApproximation(euler: Double)

  class Worker extends Actor {

    def calcEuler(start: Long, nrOfElements: Int): Double = {
      var acc = 0.0
      var fac = factorial(start, 1)
      for (i <- start until (start + nrOfElements)) {
        var mult = 1.0
        (1L to i).foreach(x => mult *= 1.0 / x)
        acc += mult
      }
      acc
    }

    def receive = {
      case Work(start, nrOfElements) =>
        sender ! Result(calcEuler(start, nrOfElements))
    }

    @tailrec final def factorial(n: Long, acc: Long): Long = {
      n match {
        case 0 => acc
        case i => factorial(i - 1, acc * i)
      }
    }
  }

  class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, printer: ActorRef) extends Actor {

    var euler: Double = 0.0
    var nrOfResults: Int = 0

    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

    def receive = {
      case Calculate =>
        for (i <- 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
          case Result(value) =>
            euler += value
            nrOfResults += 1
            if (nrOfResults == nrOfMessages) {
              printer ! eApproximation(euler)
              context.stop(self)
            }
    }
  }

  class Printer extends Actor {
    def receive = {
      case eApproximation(euler) =>
        println("approximation: %s\n".format(euler))
        context.system.shutdown()
    }
  }

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    val system = ActorSystem("eSystem")

    val printer = system.actorOf(Props[Printer], name = "printer")

    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements, printer)),
      name = "master")

    master ! Calculate
  }
}
