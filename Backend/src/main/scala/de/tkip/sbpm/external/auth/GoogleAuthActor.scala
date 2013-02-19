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
import com.google.api.client.auth.oauth2.TokenResponseException


// message types for google specific communication
trait GoogleAuthAction extends GoogleMessage


//case classes for authentication purpose
case class DeleteCredential(id: String) extends GoogleAuthAction

case class GetCredential(id: String) extends GoogleAuthAction

case class GetNewCredential(id: String) extends GoogleAuthAction

case class GetAuthUrl(id: String) extends GoogleAuthAction

case class GoogleResponse(id: String, response: String) extends GoogleAuthAction




class GoogleAuthActor extends Actor with ActorLogging {

  def actorRefFactory = context
  
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
  val CLIENT_SECRETS = GoogleClientSecrets.load(JSON_FACTORY, new FileInputStream("resources/client_secrets.json"))
  
  // currently no persistence
  val credentialStore = new FileCredentialStore(new java.io.File(System.getProperty("user.home"), ".credentials/drive.json"), JSON_FACTORY)
  
  // instanciate new code flow
  val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_SECRETS, SCOPE)
    .setCredentialStore(credentialStore).build()
    
  

  
  def receive = {
    case DeleteCredential(id) => sender ! deleteCredential(id)
    
    case GetCredential(id) => sender ! getUserCredential(id)
    
    case GoogleResponse(id, response) => handelResponse(id, response)
    
    case GetAuthUrl(id) => sender ! formAuthUrl(id)
    
    
    // implement a callback to have non-blocking struktures
    case GetNewCredential(id) => sender ! "callback"

    case _ => sender ! "not implemented yet"
  }
  
  /**
   * 1. AuthorizationCodeFlow.loadCredential(String) available?
   * 2. If not, call newAuthorizationUrl() and direct the end-user's browser to an authorization page. 
   * 3. Redirect to the redirect URL with a "code" query parameter which can then be used to request an 
   * 	access token using newTokenRequest(String)
   * 4. AuthorizationCodeFlow.createAndStoreCredential(TokenResponse, String) to store
   */
  
  /** Method that handels the whole message flow necessary for
   * new creadentials
   
  def getNewUserCredentials(id: String): Credential = {
    new Credential()
  }
  */
  
  
  /** Loads user credentials from database, in case user is
   * unknown it returns null
   */
  def getUserCredential(id : String): Credential = {
      flow.loadCredential(id)
  }
  
  /** Generate autorization URL */ 
  def formAuthUrl(id: String): String = {
    flow.newAuthorizationUrl().setRedirectUri("http://localhost:8080/oauth2callback").setState(id).build()
  }
   
  /** Receives google post and exchanges it to an access token */
  def handelResponse(id : String, response : String) = {
    log.debug(getClass().getName() + " Response: " + response)
    try {  
    val tokenRequest = flow.newTokenRequest(response).setRedirectUri("http://localhost:8080/oauth2callback")
    
    // for debug purpose
    log.debug(getClass().getName() + " TokenRequest: " + "{Code: " + tokenRequest.getCode() + " RURI: " + tokenRequest.getRedirectUri() + "}")
    
    flow.createAndStoreCredential(tokenRequest.execute(), id)
    
    } catch {
    case m : TokenResponseException => log.debug(getClass().getName() + " Exception occurred: " + m.getDetails() + "\n" + m.getMessage())
}
  }
  
  /** Delete access token if user wants to retrieve access for application*/
  def deleteCredential(id : String): Boolean = {
    if (id != null) {
      val credential = flow.loadCredential(id)
      flow.getCredentialStore().delete(id, credential)
    }
   flow.loadCredential(id).getAccessToken().isEmpty()
  }
  
  
}