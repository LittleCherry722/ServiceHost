package de.tkip.sbpm.anonymization

import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.rest.GraphJsonProtocol._
import org.scalatest.FunSuite
import spray.json._

import scala.io.Source

/**
 * Created by arne on 22.05.15.
 */
class AnonymizationTest extends FunSuite {
  def getGraph(jsonName: String): Graph = {
    val res = getClass.getResource(jsonName)
    val simpleGraphSource = Source.fromURL(res).mkString
    val domainGraph = simpleGraphSource.parseJson.convertTo[Graph](graphJsonFormat)
    domainGraph
  }
//
//  test("views do not break verifiability") {
//    val graph = getGraph("ratio-drink.json")
//    assert(ModelConverter.verifyGraph(graph).isRight)
//
//    for (viewSubject <- graph.subjects.values.filterNot(_.isStartSubject.getOrElse(false)).filterNot(_.name == "RatioDrink")) {
//      val viewSubjectId = viewSubject.id
//      // val view = Anonymizer.createView(viewSubjectId, graph).right.get
//      // println(view.toJson.compactPrint)
//      val newSubjects = graph.subjects.mapValues { s =>
//        val newMacros = s.macros.mapValues { m =>
//          val newNodes = m.nodes.mapValues { n =>
//            if (n.chooseAgentSubject.contains(viewSubjectId)) {
//              GraphNode(
//                id = n.id
//              , nodeType = StateType.ActStateString
//              , text = "Choose Agent Dummy"
//              )
//            } else {
//              n
//            }
//          }
//          m.copy(nodes = newNodes)
//        }
//        s.copy(macros = newMacros)
//      }
//      val newGraph = graph.copy(subjects = newSubjects)
//      val veri = new Verificator(ModelConverter.convertForInterface(newGraph, viewSubjectId))
//      veri.optimize = true
//      veri.verificate()
//      veri.pruneLts()
//      val lts = veri.lts
//      VerificationGraphWriter.writeLts(lts, filename = s"view-$viewSubjectId")
//      println(s"view lts size: States: ${lts.states.size}, transitions: ${lts.transitions.size}")
////      assert(valid)
//     }
//  }

//  test("views do not break bisimulation") {
//    val graph = getGraph("ratio-drink.json")
//    assert(ModelConverter.verifyGraph(graph).isEmpty)
//
//    val lts = Verification.buildLts(ModelConverter.convertForVerification(graph), optimize = true)
//    println(s"regular lts size: States: ${lts.states.size}, transitions: ${lts.transitions.size}")
//
//    for (viewSubject <- graph.subjects.values.filter(_.isStartSubject.getOrElse(false))) {
//      val viewSubjectId = viewSubject.id
//      val view = Anonymizer.createView(viewSubjectId, graph).right.get
//      val lts = Verification.buildLts(ModelConverter.convertForVerification(view), optimize = true)
//      println(s"view lts size: States: ${lts.states.size}, transitions: ${lts.transitions.size}")
//      VerificationGraphWriter.writeLts(lts, filename = s"view-${viewSubjectId}")
//      val valid = ModelBisimulation.checkGraphs(view, view)
//      assert(valid)
//      println("another view valid")
//    }
//  }

}
