package de.tkip.akkatutorial

import akka.actor.ActorSystem
import akka.actor.Props
import scala.util.Random

object Main extends App {
  val system = ActorSystem("main")
  val slaves = Random.nextInt(20) + 1
  val messages = Random.nextInt(20) + 1
  val chunk = Random.nextInt(3) + 1
  println("Approximation with " + slaves + " Slaves, " + messages + " Messages and in Chunks of " + chunk)
  val master0 = system.actorOf(Props(new Master(system, slaves, messages,chunk)));
  master0 ! new Calculate
}
