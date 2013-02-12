package de.tkip.sbpm.external.auth.test

import de.tkip.sbpm.external.auth.GoogleAuthActor
import org.scalatest.FunSuite
import akka.actor.ActorSystem._
import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout




class GoogleAuthTest extends FunSuite {
  implicit val executionContext = scala.concurrent.ExecutionContext.global
  val sys = ActorSystem()
  //val actor = sys.actorOf(Props(new GoogleAuthActor()))
}