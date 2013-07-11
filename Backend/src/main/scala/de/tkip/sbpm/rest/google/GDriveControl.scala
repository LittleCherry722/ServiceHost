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

import com.google.api.client.http.{HttpTransport, HttpResponse, HttpResponseException}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore

import com.google.api.services.oauth2.{Oauth2, Oauth2Scopes}
import com.google.api.services.oauth2.model.Userinfo

import com.google.api.services.drive.{Drive, DriveScopes}
import com.google.api.services.drive.model.File


object GDriveControl {
  case class NoCredentialsException(authorizationUrl: String) extends Exception

  val clientSecretsSource = getClass().getResourceAsStream("/client_secrets.json")
  val credentialsSource = new java.io.File(System.getProperty("user.home"), ".credentials/drive.json")

  val default_query = "'me' in owners and mimeType='application/pdf'"
  val default_fields = "items(id,title,description,downloadUrl,mimeType,ownerNames)"

  private val REDIRECT_URI = "http://localhost:8080/oauth2callback"//"urn:ietf:wg:oauth:2.0:oob"
  private val SCOPES = Arrays.asList(
      DriveScopes.DRIVE,
      Oauth2Scopes.USERINFO_PROFILE, 
      Oauth2Scopes.USERINFO_EMAIL)

  private val jsonFactory = new JacksonFactory()
  private val httpTransport = new NetHttpTransport()
  private val credentialStore = new FileCredentialStore(credentialsSource, jsonFactory)
  private val clientSecrets = GoogleClientSecrets.load(jsonFactory, clientSecretsSource)
  private val authCodeFlow: GoogleAuthorizationCodeFlow = null

  // def askForAuthorizationCode(): String = {
  //   println("Please open the following URL in your browser then type the authorization code:")
  //   println("  " + authorizationUrl)
  //   val br = new BufferedReader(new InputStreamReader(System.in))
  //   val code = br.readLine()
  //   return code
  // }

  def authorizationUrl(userId: String): String =
    flow.newAuthorizationUrl()
      .setRedirectUri(REDIRECT_URI)
      .setState(userId)
      .build()

  def flow: GoogleAuthorizationCodeFlow =
    if (authCodeFlow == null)
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
    else
      authCodeFlow

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

  def initCredentials(userId: String, code: String) = {
    val tokenResponse = tokenResponseForAuthCode(userId, code)
    flow.createAndStoreCredential(tokenResponse, userId)
  }

  def getCredentials(userId: String): Credential = {
    val c = flow.loadCredential(userId)
    if (c == null)
      throw new NoCredentialsException(authorizationUrl(userId))
    return c
  }

  def drive(userId: String): Drive = {
    if (! driveMap.contains(userId)) {
      println(s"no ID in map for $userId: $driveMap")
      val d = buildService(getCredentials(userId))
      driveMap(userId) = d
    }
    return driveMap(userId)
  }

  def myFiles(userId: String) =
    queryFiles(drive(userId), default_query, default_fields)

  def findFiles(userId: String, q: String, f: String = default_fields) =
    queryFiles(drive(userId), q, f).toPrettyString

  def userInfo(userId: String): String =
    getUserInfo(getCredentials(userId)).toPrettyString
    
}