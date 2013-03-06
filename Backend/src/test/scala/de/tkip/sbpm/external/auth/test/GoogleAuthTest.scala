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
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GoogleAuthActor
import de.tkip.sbpm.external.auth.GetAuthUrl
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.auth.oauth2.Credential


class GoogleAuthTest extends FunSuite {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
 
  val sys = ActorSystem()
  val actor = sys.actorOf(Props[GoogleAuthActor])
  
  test("Test if GoogleAuthActor builds a valid authentication url") {
    val future = actor ? GetAuthUrl("User_1")
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
    assert(result === "https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=925942219892.apps.googleusercontent.com&redirect_uri=http://localhost:8080/oauth2callback&response_type=code&scope=https://www.googleapis.com/auth/drive&state=User_1")
  }
  
  test("Load a credential for a new user") {
    val future = actor ? GetCredential("User_1")
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    println("Token: " + result.getAccessToken())
    println("Expires in: " + (result.getExpiresInSeconds() / 60) + " minutes")
    println("Refresh Token: " + result.getRefreshToken())
  }
  
  test("Test if credentials can be refreshed") {
    val future = actor ? GetCredential("User_1")
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    println("Expires in: " + (result.getExpiresInSeconds() / 60) + " minutes")
    println("Refresh token: " + result.refreshToken())
    println("Expires in: " + (result.getExpiresInSeconds() / 60) + " minutes")
  }
}


  //TODO integration des tests in das laufende aktoren systen, nicht in ein temporäres system
  /**
  test("Check if the token can be refreshed")
  	val future = actor ? new GetCredential("User_1")
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    println("Token: " + result.getAccessToken())
    println("Expires in: " + (result.getExpiresInSeconds() / 60) + " minutes")
    println("Refresh Token: " + result.getRefreshToken())
}
  */