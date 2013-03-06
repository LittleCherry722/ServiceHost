package de.tkip.sbpm.external.api.test

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
import de.tkip.sbpm.external.api.GoogleDriveActor
import de.tkip.sbpm.external.api._


class GoogleDriveTest extends FunSuite {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
 
  val sys = ActorSystem()
  val actor = sys.actorOf(Props[GoogleDriveActor])
  /**
  test("Test if GoogelDriveActor is able to get a valid credential from GoogleAuthActor") {
    val future = actor ? HasAccessToValidGDriveToken
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    println("Token: " + result.getAccessToken())
    println("Expires in: " + (result.getExpiresInSeconds() / 60) + " minutes")
    println("Refresh Token: " + result.getRefreshToken())
  }
  */
  
  test("Test if GoogleDriveActor is able to establish connection to google drive") {
    val future = actor ? ListGDriveDirectory(None)
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
    
  }
  
  
  
}
  