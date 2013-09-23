package de.tkip.sbpm.rest.google

import scala.io.Source

import java.io.IOException
import java.util.{Arrays, List}

import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow,
  GoogleTokenResponse,
  GoogleAuthorizationCodeRequestUrl,
  GoogleClientSecrets,
  GoogleCredential
}
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.{
  FileContent,
  HttpTransport,
  HttpResponse,
  HttpResponseException
}

import com.google.api.services.oauth2.{Oauth2, Oauth2Scopes}
import com.google.api.services.oauth2.model.Userinfo
import com.google.api.services.drive.{Drive, DriveScopes}
import com.google.api.services.drive.model.{File, Permission}

import de.tkip.sbpm.rest.google.GAuthCtrl


object GDriveControl {
  case class GDriveFileInfo(title: String, url: String, iconLink: String)

  val default_query = "'me' in owners and mimeType='application/pdf'"
  val default_fields = "items(id,title,description,downloadUrl,iconLink,mimeType,ownerNames)"

  private val jsonFactory = new JacksonFactory()
  private val httpTransport = new NetHttpTransport()

  // Service Functions

  def buildService(credentials: Credential) =
    new Drive.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName("gDriveCtrl")
      .build()

  def getUserInfo(credentials: Credential): Userinfo =
    new Oauth2.Builder(httpTransport, jsonFactory, credentials)
      .build()
      .userinfo()
      .get()
      .execute()

  def queryFiles(service: Drive, q: String, f: String) =
    service.files().list()
      .setMaxResults(100)
      .setPrettyPrint(true)
      .setQ(q)
      .setFields(f)
      .execute()

  def getFile(drive: Drive, fileId: String) =
    drive.files()
      .get(fileId)
      .execute()

  def insertFile(service: Drive, title: String, description: String,
                 mimeType: String, filename: String) = {
    val gFile = new File()
    gFile.setTitle(title)
    gFile.setDescription(description)
    gFile.setMimeType(mimeType)

    val file = new java.io.File(filename)
    val gFileContent = new FileContent(mimeType, file)

    service.files().insert(gFile, gFileContent).execute()
  }

  // Permissions

  def updatePermission(service: Drive, fileId: String,
      permissionId: String, newRole: String): Permission = {
    val p = service.permissions()
      .get(fileId, permissionId)
      .execute()

    p.setRole(newRole)

    service.permissions()
      .update(fileId, permissionId, p)
      .execute()
  }

  def insertPermission(service: Drive, fileId: String,
      p_type: String, value: String, role: String): Permission = {
    val p = new Permission()

    p.setValue(value)
    p.setType(p_type)
    p.setRole(role)

    service.permissions()
      .insert(fileId, p)
      .execute()
  }

  def removePermission(service: Drive, fileId: String,
      permissionId: String) {
    service.permissions()
      .delete(fileId, permissionId)
      .execute()
  }

  def printFile(service: Drive, fileId: String) {
    val file: File = service.files().get(fileId).execute()
    println("Title: " + file.getTitle())
    println("Description: " + file.getDescription())
    println("MIME type: " + file.getMimeType())
  }

}

class GDriveControl {
  import GDriveControl._
  import scala.collection.mutable

  private val driveMap = mutable.Map[String, Drive]()

  /*
   * Return a drive instance for the user. If none is found,
   * instantiate one and hold it in the map for later use.
   */
  def driveOf(userId: String): Drive = {
    if (! driveMap.contains(userId)) {
      println(s"no ID in map for $userId: $driveMap")
      val d = buildService(GAuthCtrl.getCredentials(userId))
      driveMap(userId) = d
    }
    return driveMap(userId)
  }

  def myFiles(userId: String) =
    queryFiles(driveOf(userId), default_query, default_fields)

  def findFiles(userId: String, q: String, f: String = default_fields) =
    queryFiles(driveOf(userId), q, f)
      .toPrettyString

  def fileUrl(userId: String, fileId: String) =
    getFile(driveOf(userId), fileId).getAlternateLink

  def fileInfo(userId: String, fileId: String) = {
    val f = getFile(driveOf(userId), fileId)
    GDriveFileInfo(f.getTitle, f.getAlternateLink, f.getIconLink)
  }

  def insertFile(userId: String, title: String, description: String,
                 mimeType: String, filename: String) =
    GDriveControl.insertFile(driveOf(userId), title, description, mimeType, filename)

  def userInfo(userId: String): String =
    getUserInfo(GAuthCtrl.getCredentials(userId))
      .toPrettyString

  def userEmail(userId: String): String =
    getUserInfo(GAuthCtrl.getCredentials(userId))
      .getEmail()

  def publishFile(userId: String, fileId: String) =
    insertPermission(driveOf(userId), fileId,
      "anyone", "", "reader")

  def unpublishFile(userId: String, fileId: String) =
    removePermission(driveOf(userId), fileId,
      "anyone")

  def shareFile(userId: String, fileId: String, targetId: String) =
    insertPermission(driveOf(userId), fileId,
      "user", userEmail(targetId), "reader")
    
}