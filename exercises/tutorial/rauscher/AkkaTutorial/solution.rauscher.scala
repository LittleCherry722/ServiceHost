package akka.tutorial.scala

import akka.actor._
import akka.routing._

class Message()

case class Calculate()
case class Work(start: Int, end: Int)
case class Result(result: Double)
case class eApproximation(result: Double)

// Srsly, scala? Y U NO HAS DIS!?!
object MyMath {
    def fac(n: Int): Long = {
        if (n == 0) 1 else n * fac(n-1)
    }
}

class Worker extends Actor {
    def receive: Actor.Receive = {
        case Work(start, end) => {
            sender ! Result((for (i <- start to end) yield 1.0 / MyMath.fac(i)).sum)
        }
    }
}

class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Actor {
    val system = ActorSystem()
    
    val printer = system.actorOf(Props(new PrinterActor))
    val workers = for (i <- 0 until nrOfWorkers) yield system.actorOf(Props(new Worker()))
    
    val routerProps = Props.empty.withRouter(RoundRobinRouter(routees = workers))
    val router = system.actorOf(routerProps)
    
    var aprox = 0.0
    
    def receive: Actor.Receive = {
        case Calculate => {
            for (i <- 0.to(nrOfMessages, nrOfElements)) {
                val work = new Work(i, i+nrOfElements-1)
                router ! work
            }
        }
        case Result(res) => {
            aprox += res
            printer ! eApproximation(aprox)
        }
    }
}

class PrinterActor extends Actor {
    def receive: Actor.Receive = {
        case eApproximation(aprox) => {
            println(aprox)
        }
    }
}

object Main extends App {
    val system = ActorSystem()
    
    val master = system.actorOf(Props(new Master(2, 20, 5)))
    master ! Calculate
}
