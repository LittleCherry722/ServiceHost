package de.tkip.sbpm.external.auth

import akka.actor.Actor
import akka.actor.ActorLogging
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.drive.DriveScopes
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl
import com.google.api.client.auth.oauth2.CredentialStoreRefreshListener
import java.util.ArrayList
import java.util.Collections
import scala.io.Source
import java.io.FileInputStream
import de.tkip.sbpm.application.miscellaneous.GoogleMessage


// message types for google specific communication
trait GoogleAuthAction extends GoogleMessage


//case clases for authentication purpose
case class deleteCredential(id: String) extends GoogleAuthAction
case class getCredential(id: String) extends GoogleAuthAction
case class googleResponse(response: String) extends GoogleAuthAction
case class getAuthUrl(id: String) extends GoogleAuthAction


class GoogleAuthActor extends Actor with ActorLogging {

  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  // access scope is the whole google drive
  val SCOPE = Collections.singletonList[String](DriveScopes.DRIVE)
  val HTTP_TRANSPORT = new NetHttpTransport()
  val JSON_FACTORY = new JacksonFactory()
  
  
  // load application settings from config file stored in resources folder
  val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileInputStream("resources/client_secrets.json"))
  
  // currently no persistence
  val credentialStore = new FileCredentialStore(new java.io.File(System.getProperty("user.home"), ".credentials/drive.json"), JSON_FACTORY)
  
  // instanciate new code flow
  val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPE)
    .setCredentialStore(credentialStore).build()
    
  

  
  def receive = {
    case deleteCredential(id) => sender ! deleteCredential(id)
    
    case getCredential(id) => sender ! getUserCredential(id)
    
    case googleResponse(response) => handelResponse(response, "User_1")
    
    case getAuthUrl(id) => sender ! formAuthUrl

    case _ => sender ! "not implemented yet"
  }
  
  /**
   * 1. AuthorizationCodeFlow.loadCredential(String) available?
   * 2. If not, call newAuthorizationUrl() and direct the end-user's browser to an authorization page. 
   * 3. Redirect to the redirect URL with a "code" query parameter which can then be used to request an 
   * 	access token using newTokenRequest(String)
   * 4. AuthorizationCodeFlow.createAndStoreCredential(TokenResponse, String) to store
   */
  
  
  /** Loads user credentials from database, in case user is
   * unknown it returns null
   */
  def getUserCredential(id : String): Credential = {
      flow.loadCredential(id)
  }
  
  /** Generate autorization URL */ 
  def formAuthUrl(): String = {
    flow.newAuthorizationUrl().setRedirectUri("http://localhost:8080/oauth2callback").build()
  }
  
  //TODO - Catch exceptions 
  /** Receives google post and exchanges it to an access token */
  def handelResponse(response : String, id : String) = {
    log.debug(getClass().getName() + " Response: " + response)
    val tokenRequest = flow.newTokenRequest(response).execute()
    flow.createAndStoreCredential(tokenRequest, id.toString)
  }
  
  /** Delete access token if user wants to retrieve access for application*/
  def deleteCredential(id : String): Boolean = {
    flow.getCredentialStore().delete(id, getUserCredential(id))
    //check if access token for specific user is empty
    flow.loadCredential(id.toString).getAccessToken().isEmpty()
    
  }
  
  
}