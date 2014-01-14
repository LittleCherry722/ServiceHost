package akka.tutorial.scala
import akka.actor.{Actor, Props, ActorSystem}
import akka.routing.RoundRobinRouter
import akka.actor.Actor._
import akka.actor.actorRef2Scala

case class Calculate()
case class Work(n : Int, num : Int)
case class Result(value : Double)
case class eApproximation(value : Double)

class MasterActor(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Actor {

  val workers = context.actorOf(Props[WorkerActor].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerActors")
  val printer = context.actorOf(Props[PrinterActor], name = "printerActor")
  var sum = 0.0
  var nrOfResults = 0
  def receive = {
    case Calculate =>
      for (i <- 0 until nrOfMessages) workers ! Work(i * nrOfElements, nrOfElements)
    case Result(value) =>
      nrOfResults += 1
      sum += value
      if (nrOfResults == nrOfMessages)
        printer ! eApproximation(sum)
  }
}

class WorkerActor extends Actor {
  var result = 0.0
  var sum = 1.0
  def receive = {
    case Work(n, num) => 
      result = 0.0
      for(i <- n until (n + num)){
        if(i == 0){
          result += 1.0
        }else{
          sum = 1.0
          for(j <- 1 to i){
            sum *= j
          }
          result += 1/sum 
        }     
      }
//      println("from: " + n + " until: " + (n+num) + " result: " + result)
      sender ! Result(result)
  }
}

class PrinterActor extends Actor{

  def receive = {
    case eApproximation(result) =>
      println("result: " + result)
      context.system.shutdown()
  }
}

object CalculateE extends App {

  val system = ActorSystem("mySystem")
  val master = system.actorOf(Props(new MasterActor(3, 60, 5)), name = "masterActor")
  master ! Calculate

}