package de.tkip.akkatutorial

import akka.actor._
import akka.routing._
import scala.collection.mutable.ArrayBuffer

case class Calculate()
case class Work(start: Int, end: Int)
case class Result(result: Float)
case class eApproximation(result: Float)

class Worker extends Actor {
	def faculty(n: Int): Float = {
	  if(n == 0 || n == 1){
        1
      }
      else {
        n * faculty(n - 1)
      }
	}
  
    def receive = {
      case Work(start, end) => {
        var e : Float = 0;
        for (i <- start to end){
          e += (1.0f / faculty(i))
        }
        println(e)
      }
    }
}

class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Actor{
  val system = ActorSystem("AkkaProjectInScala")
  var eAproximation = 0.0f
  
  //gerate list of workers
  val workers = for (i <- 0 until nrOfWorkers) yield system.actorOf(Props(new Worker()))
  
  val router = system.actorOf(Props.empty.withRouter(RoundRobinRouter(routees = workers)))
  
  val printer = system.actorOf(Props(new PrinterActor()))
  
  
  def receive = {
    case Calculate() => {
      for (i <- 0 to nrOfMessages - 1) {
        println(i * nrOfElements)
        println((i + 1) * nrOfElements)
        router ! new Work(i * nrOfElements, (i + 1) * nrOfElements)
      }
    }
    case Result(res) => {
      eAproximation += res
      printer ! eApproximation(eAproximation)
    }
  }
  
  
}

class PrinterActor extends Actor {
  
  def receive = {
    case eApproximation(eAproximation) => {
      println(eAproximation)
      system.shutdown()
    }
  }
}

object Main extends App {
 
  println("start")
  val system = ActorSystem("AkkaProjectInScala")
  val master = system.actorOf(Props(new Master(2, 10, 2)))
  master ! new Calculate()
  
}
