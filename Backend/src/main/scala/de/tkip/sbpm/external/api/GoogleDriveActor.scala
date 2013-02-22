package de.tkip.sbpm.external.api

import akka.actor.Actor
import akka.actor.ActorLogging
import de.tkip.sbpm.application.miscellaneous.GoogleMessage
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.HttpResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
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
import java.util.HashMap

// message types for google specific communication
sealed trait GoogleDriveAction extends GoogleMessage

// case classes to communicate with google drive

// returns index of a specific folder on the google drive, in case string = none it returns 
// the index of the root directory
case  class ListGDriveDirectory(folder: Option[String] = None) extends GoogleDriveAction

case  class ListGDriveFiles(id: String) extends GoogleDriveAction

case  class DownloadFromGDrive(item: String) extends GoogleDriveAction

case  class CreateNewFile(file: String) extends GoogleDriveAction

case  class CreateNewDirectory(directory: String) extends GoogleDriveAction

case  class HasAccessToValidGDriveToken(id: String) extends GoogleDriveAction

case  class DeleteUserGDrive(id: String) extends GoogleDriveAction

// case class to establish initial drive connection when user logs in 
case  class InitUserGDrive(id: String) extends GoogleDriveAction



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
  
  //map that keeps track of the different drive connections of all user
  val DRIVE_SET = new scala.collection.mutable.HashMap[String, Drive]()
  
  //just for testing purpose
  //val CREDENTIAL = getUserToken("User_1")
  //log.debug(getClass.getName + "Got Auth Token: " + CREDENTIAL.getAccessToken())
  
  //val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, CREDENTIAL).setApplicationName("SBPM-oAuth").build()
  //var files = drive.files().list().execute()
  //log.debug(getClass.getName + " About: " + files.toString())   //testing purpose
  //files = drive.getBaseUrl
  //log.debug(getClass.getName + " Files: " + files.toString())   //testing purpose
  
  
  
  
  def receive = {
    //case ListGDriveDirectory(folder) => sender ! "listDirectory(folder)" 
    
    case ListGDriveFiles(id) => sender ! listFiles(id) 
    
    // just for testing purpose
    case HasAccessToValidGDriveToken(id) => sender ! getUserToken(id)
    
    case InitUserGDrive(id) => initUser(id) 
    
    case DeleteUserGDrive(id) => deleteUserDrive(id)
    
    case _ => sender ! "not yet implemented"
  }
  
  /** just for testing purpose */
  def getUserToken(id: String): Credential = {
    val future = googleAuthActor ? GetCredential(id)
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    result
  }
  
  /** add new google drive connection to DRIVE_SET or check if there is still a valid connection */
  def initUser(id: String) = {
    if (DRIVE_SET.contains(id)) {
      //still valid? / renew or add new one
      log.debug(getClass.getName + "Drive already existed for user: " + id)
      if (isGDriveValid(id, DRIVE_SET.get(id).get)) {
      log.debug(getClass.getName + "Drive for user: " + id + " is still valid")  
      } else {
        log.info(getClass.getName + "Drive for user: " + id + " is not valid, will create a new one")
        deleteUserDrive(id)
        addUserDrive(id)
      }
    } else {
      //add new one
      addUserDrive(id)
    }    
  }
  
  /** add a new drive object to the DRIVE_SET with the user_id as hash value */
  def addUserDrive(id: String): Boolean = {
    val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getUserToken(id)).setApplicationName("SBPM-oAuth").build()
    DRIVE_SET += ((id, drive))
    log.info(getClass.getName + "Added new drive instance for user: " + id)
    DRIVE_SET.contains(id) && isGDriveValid(id, DRIVE_SET.get(id).get)
  }
  
  
  /** delete drive object, e.g. in case the credentials have been revoked or the access token are not valid any more */
  def deleteUserDrive(id: String): Boolean = {
    DRIVE_SET.remove(id)
    !DRIVE_SET.contains(id)
  }
  
  /** check if drive object can be executed */
  def isGDriveValid(id: String, drive: Drive): Boolean = {
    try {
      //temporary check if drive object is still valid   
      drive.about().get().execute()
      } catch {
    case e : HttpResponseException => {
      if (e.getStatusCode() == 401) {
        log.debug(getClass.getName + " Credentials for user: " + id + " have been revoked")
      }
      log.debug(getClass().getName() + " Exception occurred: " + e)
      return false
    }
     }
    true
  }
      
  /** returns user specific drive object from DRIVE_SET */
  def getDriveObject(id: String): Drive = {
    DRIVE_SET.get(id).get
  }
  
  /** lists directory on the google drive, in case the method does not get a parameter it lists the root directory */
  def listFiles(id: String): FileList = {
    val drive = getDriveObject(id)
    val files = drive.files().list().execute()
    files
  }

  
}