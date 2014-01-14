import akka.actor._
import akka.routing._


case class Calculate
case class Work(start: Int, end: Int)
case class Result(result: Double)
case class eApproximation(result: Double)


class Worker(name: String) extends Actor {
  println("worker '" + name + "' created");
  def receive: Actor.Receive = {
    case Work(start, end) => {
      val facs = for (i <- start to end) yield 1.0/fac(i)
      sender ! Result(facs.sum)
    }
  }


  def fac(n: Int): Long = {
    if (n == 0) 1 else n * fac(n-1)
  }

}


class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int) extends Worker("master") {
  val system = ActorSystem()

  val printer = system.actorOf(Props(new PrinterActor))
  val workers = for (i <- 0 until nrOfWorkers) yield system.actorOf(Props(new Worker("actor"+i)))

  val routerProps = Props.empty.withRouter(RoundRobinRouter(routees = workers))
  val router = system.actorOf(routerProps)

  var aprox = 0.0

  override def receive: Actor.Receive = {
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

class PrinterActor extends Worker("printer") {
  override def receive: Actor.Receive = {
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
