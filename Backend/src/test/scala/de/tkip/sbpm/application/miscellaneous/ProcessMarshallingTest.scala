package de.tkip.sbpm.application.miscellaneous

import scala.io.Source
import de.tkip.sbpm.rest.GraphJsonProtocol._
import spray.json._
import de.tkip.sbpm.model.{StateType, Graph}
import org.scalatest.FunSuite
import ProcessAttributes._

class ProcessMarshallingTest extends FunSuite {

  val simpleGraphSource = Source.fromURL(getClass.getResource("simple-graph.json")).mkString
  val domainGraph = simpleGraphSource.asJson.convertTo[Graph](graphJsonFormat)

  test("parsing state options") {
    val graph = parseGraph(domainGraph)
    val subject = graph.subjects("Subj1")

    assert(subject.states.length === 2)

    val openIPState = subject.states.find(_.id == 1).get
    val options = openIPState.options

    assert(openIPState.stateType === StateType.OpenIPStateType)

    assert(options.messageType === Some(AllMessages))
    assert(options.subjectId === Some("Subj1"))
    assert(options.correlationId === Some("##cid##"))
    assert(options.conversation === Some("c1"))
    assert(options.stateId === None)
  }

  test("parsing empty state options") {
    val graph = parseGraph(domainGraph)
    val subject = graph.subjects("Subj1")

    val actState = subject.states.find(_.id == 0).get
    val options = actState.options

    assert(actState.stateType === StateType.ActStateType)

    assert(options.messageType === None)
    assert(options.subjectId === None)
    assert(options.correlationId === Some(""))
    assert(options.conversation === Some(""))
    assert(options.stateId === None)
  }

}
