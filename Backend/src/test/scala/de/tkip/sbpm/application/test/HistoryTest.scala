package de.tkip.sbpm.application.test

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import akka.pattern._
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous.Debug
import de.tkip.sbpm.application.miscellaneous.GetHistory
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.HistoryAnswer
import akka.testkit.TestKit

class HistoryTest extends TestKit(ActorSystem("TestSystem")) with FunSuite with BeforeAndAfterAll {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
  val actor = system.actorOf(Props(new ProcessInstanceActor(CreateProcessInstance(1, 1))))

  override def afterAll() {
    system.shutdown()
  }

  test("test history debug data structure") {
    val future = actor ? new GetHistory(userID = 1, processInstanceID = 1) with Debug
    val result = Await.result(future.mapTo[HistoryAnswer], timeout.duration)
    val history = result.history
    println(history)
    assert(history.entries.length === 11)
    val msg = history.entries(6).message
    val payloadResult = msg.get.data
    println(payloadResult)
    assert(payloadResult === "152876(1),4547984(3),541754(1)")
  }
}