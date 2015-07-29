package de.tkip.sbpm.application.verification


import de.tkip.sbpm.anonymization.Anonymizer
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.verification.ModelConverter
import org.scalatest.FunSuite
import spray.json._

import scala.io.Source

/**
 * Created by arne on 26.10.14.
 */
class ModelConverterTest extends FunSuite {
  def getGraph(jsonName: String): Graph = {
    val res = getClass.getResource(s"/de/tkip/sbpm/anonymization/$jsonName")
    val simpleGraphSource = Source.fromURL(res).mkString
    val domainGraph = simpleGraphSource.parseJson.convertTo[Graph](graphJsonFormat)
    domainGraph
  }

  val ratioDrinkCustomer = "Subj2:468ad62b-388b-4183-ac1b-95f88e071f7c"
  val simpleViewSubject = "Subj2:ca2dc5f8-7fb8-4232-a834-83f19785ea1e"

//  test("create LTS") {
//    val graph = getGraph("ratio-drink.json")
//    val verificationErrors = ModelConverter.verifyGraph(graph)
//    println(s"valid?: ${verificationErrors.isEmpty}")
//    verificationErrors.foreach { e =>
//      println("invalid nodes:")
//      println(e)
//    }
//    Anonymizer.createView(ratioDrinkCustomer, graph) match {
//      case Right(viewGraph) => {
//        println(viewGraph.toJson.compactPrint)
//        val viewVerificationErrors = ModelConverter.verifyGraph(viewGraph)
//        println(s"Generated view valid?: ${viewVerificationErrors.isEmpty}")
//        viewVerificationErrors.foreach { e =>
//          println("invalid nodes for viewGraph:")
//          println(e)
//
//        }
//      }
//      case Left(e) => println("Could not create view. Error: " + e)
//    }
//  }
}
