package de.tkip.sbpm.external.api

import akka.actor.ActorLogging
import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorSystem._
import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern._
import de.tkip.sbpm.application.miscellaneous.GoogleMessage
import com.google.api.services.oauth2.model.Userinfo
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.Oauth2.Builder
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.auth.oauth2.Credential
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GetCredential




// case classes to interact with google user information actor
sealed trait GoogleUserInformationAction extends GoogleMessage

case class GetGoogleEMail(id: String) extends GoogleUserInformationAction

case class GetGoogleUserInfo(id: String) extends GoogleUserInformationAction



class GoogleUserInformationActor extends Actor with ActorLogging {

  private lazy val googleAuthActor = ActorLocator.googleAuthActor
  implicit val timeout = Timeout(10 seconds)
  
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
  
  
  def receive = {
    case GetGoogleUserInfo(id) => sender ! getUserInfo(id)
    
    case GetGoogleEMail(id) => sender ! getUserInfo(id).getEmail()
    
    case _ => sender ! "not implemented yet"
  }
  
  // ask google auth actor for a valid user token
  def getUserToken(id: String): Credential = {
    val future = googleAuthActor ? GetCredential(id)
    val result = Await.result(future.mapTo[Credential], timeout.duration)
    result
  }
  
  // get the available user information from google
  def getUserInfo(id: String):Userinfo = {
    val userInfoService = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), getUserToken(id)).setApplicationName("SBPM-oAuth").build()
    userInfoService.userinfo().get.execute()
  }
  
}