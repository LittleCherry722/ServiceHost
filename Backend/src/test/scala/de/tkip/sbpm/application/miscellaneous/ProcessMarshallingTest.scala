package de.tkip.sbpm.application.miscellaneous

import akka.event.{ LoggingAdapter, NoLogging }
import scala.io.Source
import de.tkip.sbpm.rest.GraphJsonProtocol._
import spray.json._
import de.tkip.sbpm.model.{Subject, StateType, Graph}
import org.scalatest.FunSuite
import ProcessAttributes._

class ProcessMarshallingTest extends FunSuite {

  // TODO: NoLogging doesn't sound good.. But setting up a ActorSystem just for the logging is also not a good way
  implicit val log: LoggingAdapter = NoLogging

  val simpleGraphSource = Source.fromURL(getClass.getResource("simple-graph.json")).mkString
  val domainGraph = simpleGraphSource.asJson.convertTo[Graph](graphJsonFormat)

  test("parsing state options") {
    val graph = parseGraph(domainGraph)
    val subject = graph.subjects("Subj1").asInstanceOf[Subject]

    assert(subject.states.length === 3)

    val closeIPState = subject.states.find(_.id == 2).get
    val options = closeIPState.options

    assert(closeIPState.stateType === StateType.CloseIPStateType)

    assert(options.messageType === Some("m0"))
    assert(options.subjectId === Some("Subj1"))
    assert(options.correlationId === Some(""))
    assert(options.conversation === Some(""))
    assert(options.stateId === None)
  }

  test("parsing state options with all type") {
    val graph = parseGraph(domainGraph)
    val subject = graph.subjects("Subj1").asInstanceOf[Subject]

    val openIPState = subject.states.find(_.id == 1).get
    val options = openIPState.options

    assert(openIPState.stateType === StateType.OpenIPStateType)

    assert(options.messageType === Some(AllMessages))
    assert(options.subjectId === Some(AllSubjects))
    assert(options.correlationId === Some("##cid##"))
    assert(options.conversation === Some("c1"))
    assert(options.stateId === None)
  }

  test("parsing empty state options") {
    val graph = parseGraph(domainGraph)
    val subject = graph.subjects("Subj1").asInstanceOf[Subject]

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
