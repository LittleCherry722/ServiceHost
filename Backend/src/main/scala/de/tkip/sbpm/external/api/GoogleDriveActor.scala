package de.tkip.sbpm.external.api

import akka.actor.Actor
import akka.actor.ActorLogging
import de.tkip.sbpm.application.miscellaneous.GoogleMessage
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
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
import de.tkip.sbpm.external.auth.GetAuthUrl
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.auth.oauth2.Credential

// message types for google specific communication
trait GoogleDriveAction extends GoogleMessage

// case classes to communicate with google drive
case  class GetGDriveDirectory() extends GoogleDriveAction
case  class GetFileFromGDrive() extends GoogleDriveAction
case  class CreateNewFile() extends GoogleDriveAction
case  class CreateNewDirectory() extends GoogleDriveAction
case  class HasValidGDriveToken(id: String) extends GoogleDriveAction



class GoogleDriveActor extends Actor with ActorLogging {
  implicit val timeout = Timeout(10 seconds)

  private lazy val googleAuthActor = ActorLocator.googleAuthActor
  
 def actorRefFactory = context

  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  //google drive classes
  val HTTP_TRANSPORT = new NetHttpTransport()
  val JSON_FACTORY = new JacksonFactory()
  val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).build
  
  def receive = {
    
    case GetFileFromGDrive => sender ! "not yet implemented"
    
    case GetGDriveDirectory => sender ! "not yet implemented"
    
    case CreateNewFile => sender ! "not yet implemented"
    
    case CreateNewDirectory => sender ! "not yet implemented"
    
    case HasValidGDriveToken => sender ! "not yet implemented"
    
    case _ => sender ! "not yet implemented"
  }
  
  
  def getUserToken(id: String): Credential = {
    val future = googleAuthActor ? GetCredential(id)
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    result
  }

}