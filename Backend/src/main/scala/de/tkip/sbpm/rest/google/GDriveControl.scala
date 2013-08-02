package de.tkip.sbpm.rest.google

import scala.io.Source

import java.io.{IOException, BufferedReader, InputStreamReader}
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
  HttpTransport,
  HttpResponse,
  HttpResponseException
}

import com.google.api.services.oauth2.{Oauth2, Oauth2Scopes}
import com.google.api.services.oauth2.model.Userinfo
import com.google.api.services.drive.{Drive, DriveScopes}
import com.google.api.services.drive.model.{File, Permission}

object GDriveControl {
  case class NoCredentialsException(authorizationUrl: String) extends Exception

  val clientSecretsSource = getClass().getResourceAsStream("/client_secrets.json")
  val credentialsSource = new java.io.File(
    System.getProperty("user.home"),
    ".credentials/drive.json"
  )

  val default_query = "'me' in owners and mimeType='application/pdf'"
  val default_fields = "items(id,title,description,downloadUrl,iconLink,mimeType,ownerNames)"

  private val REDIRECT_URI = "http://localhost:8080/oauth2callback"//"urn:ietf:wg:oauth:2.0:oob"
  private val SCOPES = Arrays.asList(
      DriveScopes.DRIVE,
      Oauth2Scopes.USERINFO_PROFILE, 
      Oauth2Scopes.USERINFO_EMAIL)

  private val jsonFactory = new JacksonFactory()
  private val httpTransport = new NetHttpTransport()
  private val credentialStore = new FileCredentialStore(credentialsSource, jsonFactory)
  private val clientSecrets = GoogleClientSecrets.load(jsonFactory, clientSecretsSource)
  private lazy val authCodeFlow =
    new GoogleAuthorizationCodeFlow.Builder(
      httpTransport,
      jsonFactory,
      clientSecrets,
      SCOPES
    )
      .setAccessType("offline")
      .setApprovalPrompt("force")
      .setCredentialStore(credentialStore)
      .build()

  // Authorization

  def authorizationUrl(userId: String): String =
    flow.newAuthorizationUrl()
      .setRedirectUri(REDIRECT_URI)
      .setState(userId)
      .build()

  // def askForAuthorizationCode(): String = {
  //   println("Please open the following URL in your browser then type the authorization code:")
  //   println("  " + authorizationUrl)
  //   val br = new BufferedReader(new InputStreamReader(System.in))
  //   val code = br.readLine()
  //   return code
  // }

  def flow: GoogleAuthorizationCodeFlow = authCodeFlow

  def tokenResponseForAuthCode(userId: String, authCode: String): GoogleTokenResponse =
    flow.newTokenRequest(authCode)
      .setRedirectUri(REDIRECT_URI)
      .execute()

  def credentialsForAuthCode(userId: String, authCode: String): Credential = {
    val response: GoogleTokenResponse =
          flow.newTokenRequest(authCode)
            .setRedirectUri(REDIRECT_URI)
            .execute()
    return flow.createAndStoreCredential(response, userId)
  }

  // Service Functions

  def buildService(credentials: Credential) =
    new Drive.Builder(httpTransport, jsonFactory, credentials)
      .setApplicationName("gdriveCtrl")
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

  def getUrl(file: File) = file.getAlternateLink()

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
   * Retrieve credentials from API for the given code,
   * and store them using the user ID as the key.
   */
  def initCredentials(userId: String, code: String) = {
    val tokenResponse = tokenResponseForAuthCode(userId, code)
    flow.createAndStoreCredential(tokenResponse, userId)
  }

  /*
   * Return the user's credentials from the credential store,
   * otherwise throw a NoCredentialsException.
   */
  def getCredentials(userId: String): Credential =
    Option(flow.loadCredential(userId)) match {
      case Some(credential) => credential
      case None => throw new NoCredentialsException(
        authorizationUrl(userId)
      )
    }

  /*
   * Return a drive instance for the user. If none is found,
   * instantiate one and hold it in the map for later use.
   */
  def driveOf(userId: String): Drive = {
    if (! driveMap.contains(userId)) {
      println(s"no ID in map for $userId: $driveMap")
      val d = buildService(getCredentials(userId))
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
    getUrl(getFile(driveOf(userId), fileId))

  def userInfo(userId: String): String =
    getUserInfo(getCredentials(userId))
      .toPrettyString

  def userEmail(userId: String): String =
    getUserInfo(getCredentials(userId))
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