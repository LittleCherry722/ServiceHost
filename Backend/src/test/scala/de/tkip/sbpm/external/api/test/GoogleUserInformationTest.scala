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
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.File
import com.google.api.services.oauth2.model.Userinfo
import de.tkip.sbpm.external.api.GetGoogleEMail


class GoogleUserInformationTest extends FunSuite{

  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
 
  val sys = ActorSystem()
  val infoActor = sys.actorOf(Props[GoogleUserInformationActor])
  val authActor = sys.actorOf(Props[GoogleAuthActor],"google-auth")
  
  
  test("Check if user information can be retrieved") {
    val future = infoActor ? GetGoogleUserInfo("User_1")
    val result = Await.result(future.mapTo[Userinfo], timeout.duration)
    println(result.toPrettyString())
  }
  
  test("Check if user email can be retrieved") {
    val future = infoActor ? GetGoogleEMail("User_1")
    val result = Await.result(future.mapTo[String], timeout.duration)
    println(result)
  }
  
}