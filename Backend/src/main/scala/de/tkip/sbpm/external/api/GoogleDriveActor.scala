/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.external.api

import akka.actor.Actor
import akka.actor.ActorLogging
import de.tkip.sbpm.ActorLocator
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.HttpResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
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
import de.tkip.sbpm.application.miscellaneous.GetHistory
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous.CreateProcessInstance
import de.tkip.sbpm.application.miscellaneous.HistoryAnswer
import de.tkip.sbpm.external.auth.GoogleAuthActor
import de.tkip.sbpm.external.auth.GetAuthUrl
import de.tkip.sbpm.external.auth.GetCredential
import com.google.api.client.auth.oauth2.Credential
import java.util.HashMap
import de.tkip.sbpm.application.miscellaneous.GoogleMessage
import com.google.api.services.drive.model.Permission
import java.io.IOException
import de.tkip.sbpm.external.auth.GetCredential


// case classes to communicate with google drive
sealed trait GoogleDriveAction extends GoogleMessage

// returns index of a specific folder on the google drive, in case string = none it returns 
case  class ListGDriveDirectory(folder: Option[String] = None) extends GoogleDriveAction

case  class ListGDriveFiles(id: String) extends GoogleDriveAction

// opens the file in a google docs compatible editor like a browser
case  class GetAlternateLinkForFileInGDrive(id: String, fileId: String) extends GoogleDriveAction

case  class DownloadFromGDrive(item: String) extends GoogleDriveAction

case  class CreateNewFile(id: String, fileId: String) extends GoogleDriveAction

case  class CreateNewDirectory(id: String, directory: String) extends GoogleDriveAction

case  class HasAccessToValidGDriveToken(id: String) extends GoogleDriveAction

case  class DeleteUserGDrive(id: String) extends GoogleDriveAction

case  class GetFilePermission(id: String, fileId: String) extends GoogleDriveAction

// adds an foreignUser with the given role (viewer ...) to the file specified by fileId in the google drive of user id
case  class SetFilePermission(id: String, foreignUserID: String, role: String, fileId: String) extends GoogleDriveAction

// case class to establish initial drive connection when user logs in 
case  class InitUserGDrive(id: String) extends GoogleDriveAction



class GoogleDriveActor extends Actor with ActorLogging {
  
  implicit val timeout = Timeout(15 seconds)

  private lazy val googleAuthActor = ActorLocator.googleAuthActor
  private lazy val googleInformationActor = ActorLocator.googleUserInformationActor
  
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
  
  
  
  def receive = {
    
    // just for testing purpose
    case HasAccessToValidGDriveToken(id) => sender ! getUserToken(id)
    
    // init google drive instance for user 
    case InitUserGDrive(id) => sender ! initUser(id)
    
    // delete a google drive instance 
    case DeleteUserGDrive(id) => sender ! deleteUserDrive(id)
    
    //case ListGDriveDirectory(folder) => sender ! "listDirectory(folder)" 
    case ListGDriveFiles(id) => sender ! listFiles(id)  
    
    // get export url for a new user as a viewer for a specific file
    case GetAlternateLinkForFileInGDrive(id, fileId) => sender ! getFileAlternateLink(id, fileId)  
    
    // add an new user with a google or a foreign email address as a new viewer for a file specified by fileId
    case SetFilePermission(id, foreignUserId, role, fileId) => sender ! manageGDrivePermissions(id, foreignUserId, role, fileId)
    
    case _ => sender ! "not yet implemented"
  }
  
  
  
  
  // ask google auth actor for a valid user token
  def getUserToken(id: String): Credential = {
    val future = googleAuthActor ? GetCredential(id)
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    result
  }
  
  /** add new google drive connection to DRIVE_SET or check if there is still a valid connection */
  def initUser(id: String): Boolean = {
    if (DRIVE_SET.contains(id)) {
      //still valid? / renew or add new one
      log.debug(getClass.getName + "Drive already existed for user: " + id)
      if (isGDriveValid(id, DRIVE_SET.get(id).get)) {
      log.debug(getClass.getName + "Drive for user: " + id + " is still valid")
      } else {
        log.debug(getClass.getName + "Drive for user: " + id + " is not valid, will create a new one")
        deleteUserDrive(id)
        addUserDrive(id)
      }
    } else {
      //add new one
      addUserDrive(id)
    }
   DRIVE_SET.contains(id) 
  }
  
  /** add a new drive object to the DRIVE_SET with the user_id as hash value */
  def addUserDrive(id: String): Boolean = {
    val drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getUserToken(id)).setApplicationName("SBPM-oAuth").build()
    DRIVE_SET += ((id, drive))
    log.debug(getClass.getName + "Added new drive instance for user: " + id)
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
  def getGDriveObject(id: String): Drive = {
    initUser(id)
    DRIVE_SET.get(id).get
  }
  
  /** lists directory on the google drive, in case the method does not get a parameter it lists the root directory */
  def listFiles(id: String): String = {
    
    // add drive object for user or check if the current one is still valid
    val drive = getGDriveObject(id)
    
    // TODO ask internal user provider for email address because always asking google adds
    // to much latency
    
    // ask google for the email address
    val email_future = googleInformationActor ? GetGoogleEMail(id)
    val email = Await.result(email_future.mapTo[String], timeout.duration)
    
    // define query with trashed = false and user-permission = owner and type = user 
    val query = "trashed = false and mimeType != 'application/vnd.google-apps.folder' and '" + email +"' in owners" 
    
    // select specific fields 
    val fields = "items(description,downloadUrl,iconLink,id,mimeType,ownerNames,title)"
    
    val files = drive.files().list().setPrettyPrint(true).setQ(query).setFields(fields).execute()
    files.toPrettyString() 
  }
  
  /** add read permissions for a specific user to a file in a foreign google drive and return the access url */
  def manageGDrivePermissions(id: String, foreignUserId: String, role: String, fileId: String): Boolean = {
    val drive = getGDriveObject(id)
    val newPermission = new Permission()
    
    // user or group id
    newPermission.setValue(foreignUserId)
    
    // type of permission - user, group, domain, default
    newPermission.setType("user")
    
    // role of new user (owner, writer or reader)
    newPermission.setRole(role)
    
    log.debug(getClass().getName() + " User " + id + " wants to add a new role to one of his files: \n" + foreignUserId + " " + role + " " + fileId)
    
    try {
      drive.permissions().insert(fileId, newPermission).execute()
    } catch {
      case e: IOException => {
        log.debug(getClass().getName() + " Exception occurred while adding permissions: " + e)
        return false
      }    
    } 
    true
  }
  
  /** get a file alternate link for a specific user file stored in a google drive - 
   *  a link for opening the file in using a relevant google editor or viewer */
  def getFileAlternateLink(id: String, fileId: String): String = {
    val drive = getGDriveObject(id)
    val fileUrl = drive.files().get(fileId).execute.getAlternateLink()
    fileUrl
  }
}