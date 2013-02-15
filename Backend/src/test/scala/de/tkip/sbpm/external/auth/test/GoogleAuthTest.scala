package de.tkip.sbpm.external.auth.test

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
import de.tkip.sbpm.persistence.TestPersistenceActor
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.HistoryAnswer
import de.tkip.sbpm.external.auth.GoogleAuthActor
import de.tkip.sbpm.external.auth.getAuthUrl
import de.tkip.sbpm.external.auth.getCredential




class GoogleAuthTest extends FunSuite {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
 
  val sys = ActorSystem()
  val actor = sys.actorOf(Props[GoogleAuthActor])
  
  test("Test if GoogleAuthActor builds a valid authentication url") {
    val future = actor ? new getAuthUrl("User_1")
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
    assert(result === "https://accounts.google.com/o/oauth2/auth?client_id=925942219892.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth2callback&response_type=code&scope=https://www.googleapis.com/auth/drive")
  }
  
  test("Check if google response is routed from frontend to backend correctly") {
    val future = actor ? new getAuthUrl("User_1")
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
    println("Type in url in your browser and authorize the app to get a google response")
    
  }
  
  
  test("Load a credential for a new user") {
    val future = actor ? new getCredential("User_1")
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
  }
}
  