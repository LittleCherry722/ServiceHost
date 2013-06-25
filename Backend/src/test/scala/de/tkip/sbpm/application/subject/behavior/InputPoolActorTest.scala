package de.tkip.sbpm.application.subject.behavior

import org.scalatest.FunSuite
import akka.testkit.{TestKit, TestActorRef}
import akka.actor.ActorSystem
import scala.io.Source
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.application.subject.SubjectData
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.miscellaneous.parseGraph
import spray.json._

class InputPoolActorTest extends TestKit(ActorSystem("TestSystem")) with FunSuite {

  val simpleGraphSource = Source.fromURL(getClass.getResource("/de/tkip/sbpm/application/miscellaneous/simple-graph.json")).mkString
  val domainGraph = simpleGraphSource.asJson.convertTo[Graph](graphJsonFormat)
  val graph = parseGraph(domainGraph)
  val subjectData = SubjectData(1, 1, 1, null, null, graph.subjects("Subj1"))

  test("register single subscriber") {
    val actor = TestActorRef(new InputPoolActor(subjectData))

    actor ! SubscribeIncomingMessages(2, "other", "test")
  }
}
