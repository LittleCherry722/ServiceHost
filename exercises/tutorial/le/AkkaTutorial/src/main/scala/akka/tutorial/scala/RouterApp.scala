

package akka.tutorial.scala

import akka.tutorial.scala.Actors.Master
import akka.tutorial.scala.Actors.PrinterActor
import akka.tutorial.scala.message.Calculate

import akka.actor.{ ActorSystem, Props }
import akka.util.Timeout
import scala.concurrent.Await

object RouterApp extends App {
  val system = ActorSystem()
  val master = system.actorOf(Props(new Master(5, 10, 3)), "master")
  val printer = system.actorOf(Props(new PrinterActor()))
  println("app started...")
  master ! Calculate
}