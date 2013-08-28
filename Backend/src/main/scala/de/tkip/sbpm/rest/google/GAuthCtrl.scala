package de.tkip.sbpm.rest.google

import scala.io.Source

import java.io.{BufferedReader, InputStreamReader, IOException}
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

import com.google.api.services.drive.{DriveScopes}
import com.google.api.services.calendar.{CalendarScopes}


object GAuthCtrl {
  case class NoCredentialsException(authorizationUrl: String) extends Exception

  val clientSecretsSource = new java.io.InputStreamReader(
    getClass().getResourceAsStream("/client_secrets.json")
  )
  val credentialsSource = new java.io.File(
    System.getProperty("user.home"), ".credentials/drive.json"
  )

  private val REDIRECT_URI = "http://localhost:8080/oauth2callback"
  // private val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
  private val SCOPES = Arrays.asList(
      DriveScopes.DRIVE,
      CalendarScopes.CALENDAR,
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

  def askForAuthorizationCode(userId: String) {
    println("Please open the following URL in your browser then type the authorization code:")
    println("  " + authorizationUrl(userId))
    val br = new BufferedReader(new InputStreamReader(System.in))
    val code = br.readLine()
    initCredentials(userId, code)
  }

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

}