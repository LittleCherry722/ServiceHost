package de.tkip.sbpm.application.verification


import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.verification.{ModelBisimulation, ModelConverter}
import org.scalatest.FunSuite
import spray.json._

import scala.io.Source

/**
 * Created by arne on 26.10.14.
 */
class ModelBisimulationTest extends FunSuite {
  def getGraph(jsonName: String): Graph = {
    val res = getClass.getResource(s"/de/tkip/sbpm/anonymization/$jsonName")
    val simpleGraphSource = Source.fromURL(res).mkString
    val domainGraph = simpleGraphSource.parseJson.convertTo[Graph](graphJsonFormat)
    domainGraph
  }

  test("simple identical graphs are behavorial congruent") {
    val simple = getGraph("sim1.json")
    assert(ModelConverter.verifyGraph(simple).isEmpty)
    assert(ModelBisimulation.checkGraphs(simple, simple) === true)
  }

  test("complex identical graphs are behavioral congruent") {
    val ratioDrink = getGraph("ratio-drink.json")
    assert(ModelConverter.verifyGraph(ratioDrink).isEmpty)

    assert(ModelBisimulation.checkGraphs(ratioDrink, ratioDrink))
  }

  test("distinct simple graphs can be behavioral congruent") {
    val simple1 = getGraph("sim1.json")
    val simple2 = getGraph("sim2.json")
    assert(ModelConverter.verifyGraph(simple1).isEmpty)
    assert(ModelConverter.verifyGraph(simple2).isEmpty)

    assert(ModelBisimulation.checkGraphs(simple1, simple2))
  }

  test("distinct complex graphs can be behavioral congruent") {

  }

  test ("distinct simple graphs can have different behaviors") {
    val simple1 = getGraph("sim1.json")
    val simple1Diff = getGraph("sim1-diff.json")

    assert(ModelBisimulation.checkGraphs(simple1, simple1Diff) === false)
  }

  test ("distinct complex graphs can have different behaviors") {

  }

  test("distinct regular graphs can be behavioral congruent") {
    val graph1 = getGraph("medium1.json")
    val graph2 = getGraph("medium2.json")
    assert(ModelConverter.verifyGraph(graph1).isEmpty)
    assert(ModelConverter.verifyGraph(graph2).isEmpty)

    assert(ModelBisimulation.checkGraphs(graph1, graph2) === true)
  }

  test("regular identical graphs are behavioral congruent") {
    val graph = getGraph("medium1.json")
    assert(ModelConverter.verifyGraph(graph).isEmpty)

    assert(ModelBisimulation.checkGraphs(graph, graph) === true)
  }
}
