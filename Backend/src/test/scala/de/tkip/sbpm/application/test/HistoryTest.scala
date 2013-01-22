package de.tkip.sbpm.application.test

import scala.collection.Seq
import org.scalatest.FunSuite
import scala.collection.immutable.Map
import scala.reflect.Manifest
import scala.runtime.BoxedUnit
import java.lang.reflect.Method
import akka.pattern._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorSystem._
import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous.Debug
import de.tkip.sbpm.model.ProcessModel

import de.tkip.sbpm.application.miscellaneous.GetHistory

class HistoryTest extends FunSuite {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
  val sys = ActorSystem()
  val actor = sys.actorOf(Props(new ProcessInstanceActor(1, ProcessModel(1, "process 1", null))))

  test("test history debug data structure") {
    // TODO ich hab hier die user und processInstanzid eingefuegt, weil es vorher einen Fehler gab
    val future = actor ? new GetHistory(userID = 1, processID = 1) with Debug
    val result = Await.result(future.mapTo[History], timeout.duration)
    println(result)
    assert(result.entries.length === 13)
    val msg = result.entries(6).message
    val payloadResult = msg.data
    println(payloadResult)
    assert(payloadResult === "152876(1),4547984(3),541754(1)")
  }
}