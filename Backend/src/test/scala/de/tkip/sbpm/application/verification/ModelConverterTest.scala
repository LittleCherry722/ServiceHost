package de.tkip.sbpm.application.verification


import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.rest.GraphJsonProtocol._
import org.scalatest.FunSuite
import spray.json._

import scala.io.Source

/**
 * Created by arne on 26.10.14.
 */
class ModelConverterTest extends FunSuite {
  val simpleGraphSource = Source.fromURL(getClass.getResource("simple-graph.json")).mkString
  val domainGraph = simpleGraphSource.parseJson.convertTo[Graph](graphJsonFormat)

  def getGraph : Graph = {
    domainGraph
  }
}
