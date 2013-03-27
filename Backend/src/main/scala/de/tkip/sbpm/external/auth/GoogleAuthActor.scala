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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.auth.oauth2.CredentialRefreshListener
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.auth.oauth2.TokenErrorResponse
import java.util.Arrays
import com.google.api.services.oauth2.Oauth2Scopes
import de.tkip.sbpm.ActorLocator
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorSystem._
import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern._
import de.tkip.sbpm.external.api.GetGoogleEMail
import de.tkip.sbpm.model.UserIdentity
import de.tkip.sbpm.persistence.query.Users

// message types for google specific communication
sealed trait GoogleAuthAction extends GoogleMessage


//case classes for authentication purpose
case class DeleteCredential(id: String) extends GoogleAuthAction

case class GetCredential(id: String) extends GoogleAuthAction

case class GetAuthUrl(id: String) extends GoogleAuthAction

case class InitUser(id: String) extends GoogleAuthAction

// get current status for user with given id
case class GetAuthenticationState(id: String) extends GoogleAuthAction

// message that keeps response of google authentication service 
case class GoogleResponse(id: String, response: String) extends GoogleAuthAction




class GoogleAuthActor extends Actor with ActorLogging {

  def actorRefFactory = context
  
  private lazy val persistenceActor = ActorLocator.persistenceActor
  private lazy val googleInformationActor = ActorLocator.googleUserInformationActor
  implicit val timeout = Timeout(5 seconds)
  
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  // access scope is the whole google drive
  val SCOPE = Arrays.asList[String](
      DriveScopes.DRIVE, 
      Oauth2Scopes.USERINFO_PROFILE, 
      Oauth2Scopes.USERINFO_EMAIL)
      
  val HTTP_TRANSPORT = new NetHttpTransport()
  val JSON_FACTORY = new JacksonFactory()
  
  
  // load application settings from config file stored in resources folder
  val CLIENT_SECRETS = GoogleClientSecrets.load(JSON_FACTORY, getClass().getResourceAsStream("/client_secrets.json"))
  val CALLBACK_URL = "http://sbpm-gw.tk.informatik.tu-darmstadt.de/oauth2callback"
  // currently no persistence
  val credentialStore = new FileCredentialStore(new java.io.File(System.getProperty("user.home"), ".credentials/drive.json"), JSON_FACTORY)
  
  // instanciate new code flow
  val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, CLIENT_SECRETS, SCOPE)
    .setCredentialStore(credentialStore).build()

  
  def receive = {
        
	// starts authentication flow
  	case InitUser(id) => sender ! initUser(id)
  
    case DeleteCredential(id) => sender ! deleteCredential(id)
    
    case GetCredential(id) => sender ! getUserCredential(id)
    
    case GoogleResponse(id, response) => handelResponse(id, response)
    
    case GetAuthUrl(id) => sender ! formAuthUrl(id)

    case _ => sender ! "not implemented yet"
  }

  
  /** Loads user credentials from database, in case user is
   * unknown it returns null
   */
  def getUserCredential(id : String): Credential = {
    if (flow.loadCredential(id) != null) {
      try {
      flow.loadCredential(id).refreshToken()
      } catch {
        case e : TokenResponseException => {
          log.debug(getClass().getName() + 
            " Exception occurred while refreshing a token, the permission maybe has been revoked: " + 
            e.getDetails() + "\n" + e.getMessage())
          
            return null
      }}
      flow.loadCredential(id)
    } else {
      log.debug(getClass().getName() + " User with id: " + id + " tried to load a credential from store, but credential_store returned null")
      return null
    }
  }
  
  /** Generate authorization URL */ 
  def formAuthUrl(id: String): String = {
    flow.newAuthorizationUrl()
    .setRedirectUri(CALLBACK_URL)
    .setAccessType("offline")
    .setState(id).build()
  }
  
  /** Initialize authentication flow by checking if user already has a valid token - if not,
   *  send back the authentication URL
   */
  def initUser(id: String): String = {
    var returnValue = ""
    if (getUserCredential(id) != null) {
      returnValue = "AUTHENTICATED"
    } else {
     returnValue = formAuthUrl(id)
    }
    returnValue
  }
  
  /** Receives google post and exchanges it to an access token */
  def handelResponse(id : String, response : String) = {
    //log.debug(getClass().getName() + " Response: " + response)
    try {  
    val tokenRequest = flow.newTokenRequest(response).setRedirectUri(CALLBACK_URL)
    val tokenResponse = tokenRequest.execute()
    val credential = new GoogleCredential.Builder()
    .setTransport(new NetHttpTransport)
    .setJsonFactory(new JacksonFactory)
    .setClientSecrets(CLIENT_SECRETS)
    .addRefreshListener(new CredentialRefreshListener() {
      
      def onTokenResponse(credential : Credential , tokenResponse : TokenResponse) {
        log.debug(getClass().getName() + " Refresh credential for User " + id + ": OK")
      }
      
      def onTokenErrorResponse(credential : Credential, tokenErrorResponse : TokenErrorResponse) {
        log.debug(getClass().getName() + " Refresh credential for User: " + id + ": FAIL")
      }
    }).addRefreshListener(new CredentialStoreRefreshListener(id, credentialStore))
    .build()
    credential.setFromTokenResponse(tokenResponse)
    credentialStore.store(id, credential)
    log.debug(getClass().getName() + " New credential for user: " + id + " have been saved")
    
    // TODO add new google provider 
    addGoogleProvider(id)
    } catch {
    case e : TokenResponseException => log.debug(getClass().getName() + " Exception occurred: " + e.getDetails() + "\n" + e.getMessage())
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
  
  /** Add additional "GOOGLE" provider to the user */
  def addGoogleProvider(id: String) = {
    // ask google information actor for the email address of the user 
    val email_future = googleInformationActor ? GetGoogleEMail(id)
    val email = Await.result(email_future.mapTo[String], timeout.duration)
    
    // add google as a new user provider
    // TODO check - if there is already a google provider so that there is only one
    // google identity 
    val user_future = persistenceActor ? Users.Save.Identity(id.toInt, "GOOGLE", email, None)
    val user = Await.result(user_future.mapTo[Credential], timeout.duration)
  }
}