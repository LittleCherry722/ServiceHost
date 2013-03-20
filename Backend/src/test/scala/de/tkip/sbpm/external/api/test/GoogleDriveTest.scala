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


class GoogleDriveTest extends FunSuite {
  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global
 
  val sys = ActorSystem()
  val driveActor = sys.actorOf(Props[GoogleDriveActor])
  val authActor = sys.actorOf(Props[GoogleAuthActor],"google-auth")
  
  
  
  test("Test if GoogelDriveActor is able to get a valid credential from GoogleAuthActor") {
    val future = driveActor ? HasAccessToValidGDriveToken("User_1")
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    assert( (result.getExpiresInSeconds() / 60) > 55 ) 
    assert(!result.getRefreshToken().isEmpty())
  }
  
  
  test("Test if a new drive object can be added to the drive set") {
    val future = driveActor ? InitUserGDrive("User_1")
    val result = Await.result(future.mapTo[Boolean], timeout.duration)
    assert(result == true)
  }
  
  
  test("Test if GoogleDriveActor is able to establish connection to google drive") {
    val future = driveActor ? ListGDriveFiles("dp.dornseifer@googlemail.com")
    val result = Await.result(future.mapTo[java.util.List[File]], timeout.duration)
    println(result.toString())
  }  
  
  
  test("Test if the google drive object can be deleted") {
    val future = driveActor ? DeleteUserGDrive("User_1")
    val result = Await.result(future.mapTo[Boolean], timeout.duration)
    assert(result == true)
  }
  
  
  
}
  