package de.tkip.sbpm.external.api

import akka.actor.Actor
import akka.actor.ActorLogging
import de.tkip.sbpm.application.miscellaneous.GoogleMessage
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive

// message types for google specific communication
trait GoogleDriveAction extends GoogleMessage

// case classes to communicate with google drive
case  class GetGDriveDirectory() extends GoogleDriveAction
case  class GetFileFromGDrive() extends GoogleDriveAction
case  class CreateNewFile() extends GoogleDriveAction
case  class CreateNewDirectory() extends GoogleDriveAction
case  class HasValidGDriveToken() extends GoogleDriveAction



class GoogleDriveActor extends Actor with ActorLogging {

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
  
  /**
  def getUserToken(id: String): Option[String] = {
    val future = googleAuthActor ? GetCredential(id)  
  }
*/
}