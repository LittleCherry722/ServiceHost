package akka.tutorial.scala.Actors
import akka.actor.Actor
import akka.tutorial.scala.message.eApproximation

class PrinterActor extends Actor {
  def receive = {
    case res: eApproximation =>
      {
        println("result is:" + res.result.toString)
        context.system.shutdown()
      }
    case s: String => println("test:" + s)
  }
}