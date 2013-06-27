/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation epGroup @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.external.auth

import scala.io.Source
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import java.util.{Arrays, ArrayList, Collections}
import java.io.{IOException, FileInputStream}

import akka.actor.{ActorSystem, Props, Actor}
import akka.actor.ActorSystem._
import akka.actor.ActorLogging
import akka.pattern._
import akka.util.Timeout

import com.google.api.client.auth.oauth2.{
  Credential, CredentialRefreshListener, TokenResponse, TokenErrorResponse,
  CredentialStoreRefreshListener, TokenResponseException}
import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow, GoogleClientSecrets,
  GoogleAuthorizationCodeRequestUrl, GoogleCredential}
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.drive.DriveScopes
import com.google.api.services.oauth2.Oauth2Scopes

import de.tkip.sbpm

// message types for Google specific communication
sealed trait GoogleAuthAction extends sbpm.application.miscellaneous.GoogleMessage
case class DeleteCredential(id: String) extends GoogleAuthAction
case class GetCredential(id: String) extends GoogleAuthAction
case class GetAuthUrl(id: String) extends GoogleAuthAction
case class InitUser(id: String) extends GoogleAuthAction
case class GetAuthenticationState(id: String) extends GoogleAuthAction
case class GoogleResponse(id: String, response: String) extends GoogleAuthAction

object GoogleAuthActor {

  val HTTP_TRANSPORT = new NetHttpTransport()
  val JSON_FACTORY = new JacksonFactory()

  val SCOPE = Arrays.asList[String](
      DriveScopes.DRIVE,
      Oauth2Scopes.USERINFO_PROFILE, 
      Oauth2Scopes.USERINFO_EMAIL)

  val credentialStore = new FileCredentialStore(
    new java.io.File(System.getProperty("user.home"), ".credentials/drive.json"),
    JSON_FACTORY)
}

class GoogleAuthActor extends Actor with ActorLogging {
  import GoogleAuthActor._

  // load application settings from config file stored in resources folder
  val CLIENT_SECRETS = GoogleClientSecrets.load(
    JSON_FACTORY,
    getClass().getResourceAsStream("/client_secrets.json")
  )

  // get the first defined redirect uri from client_secrets.json
  val CALLBACK_URL = "http://localhost:8080/oauth2callback" //CLIENT_SECRETS.getWeb().getRedirectUris().get(0)
  
  private lazy val persistenceActor = sbpm.ActorLocator.persistenceActor
  private lazy val googleInformationActor = sbpm.ActorLocator.googleUserInformationActor
  implicit val timeout = Timeout(5 seconds)
  
  // instanciate new code flow
  val flow = new GoogleAuthorizationCodeFlow.Builder(
    HTTP_TRANSPORT, JSON_FACTORY, CLIENT_SECRETS, SCOPE)
    .setCredentialStore(credentialStore).build()

  override def preStart() {
    log.debug(getClass.getName + " starts with " + CALLBACK_URL)
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }

  def actorRefFactory = context

  def receive = {
    case InitUser(id) => sender ! initUser(id) // start authentication flow
    case DeleteCredential(id) => sender ! deleteCredential(id)
    case GetCredential(id) => sender ! getUserCredential(id)
    case GoogleResponse(id, response) => handelResponse(id, response)
    case GetAuthUrl(id) => sender ! formAuthUrl(id)
    case _ => sender ! "not implemented yet"
  }

  /**
   * Load user credentials from database, in case user is
   * unknown it returns null
   */
  def getUserCredential(id : String): Credential = {
    val cred = flow.loadCredential(id)
    if (cred != null) {
      try {
        cred.refreshToken()
      } catch {
        case e: TokenResponseException =>
          e.printStackTrace()
          log.debug(getClass().getName() + 
            "Exception occurred while refreshing a token, the permission may have been revoked: " + 
            e.getDetails() + "\n" + e.getMessage())
          return null
      }
      flow.loadCredential(id)
    } else {
      log.debug(getClass().getName() + " User with id: " + id +
        " tried to load a credential from store, but credential_store returned null")
      null
    }
  }
  
  /** 
    * Generate authorization URL
    */ 
  def formAuthUrl(id: String): String = {
    flow.newAuthorizationUrl()
    .setRedirectUri(CALLBACK_URL)
    .setAccessType("offline")
    .setState(id).build()
  }
  
  /** 
   * Initialize authentication flow by checking if user already has a valid token - if not,
   * send back the authentication URL
   */
  def initUser(id: String): String =
    if (getUserCredential(id) != null)
      "AUTHENTICATED"
    else
      formAuthUrl(id)
  
  /**
   * Receives google post and exchanges it to an access token
   */
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
        })
          .addRefreshListener(new CredentialStoreRefreshListener(id, credentialStore))
          .build()
      credential.setFromTokenResponse(tokenResponse)
      credentialStore.store(id, credential)
      log.debug(getClass().getName() + " New credential for user: " + id + " have been saved")
      
      // TODO add new google provider 
      addGoogleProvider(id)
    } catch {
      case e: TokenResponseException => log.debug(getClass().getName() +
        " Exception occurred: " + e.getDetails() + "\n" + e.getMessage())
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
    val email_future = googleInformationActor ? sbpm.external.api.GetGoogleEMail(id)
    val email = Await.result(email_future.mapTo[String], timeout.duration)
    
    // add google as a new user provider
    // TODO check - if there is already a google provider so that there is only one
    // google identity 
    val user_future = persistenceActor ? sbpm.persistence.query.Users.Save.Identity(id.toInt, "GOOGLE", email, None)
    val user = Await.result(user_future.mapTo[Credential], timeout.duration)
  }

}