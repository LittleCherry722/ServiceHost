package de.tkip.sbpm.rest

import akka.actor.Actor
import spray.routing.HttpService
import akka.actor.ActorLogging
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.external.auth.GoogleResponse
import scala.util.parsing.json.JSONObject
import scala.util.parsing.json.JSONObject
import spray.json.JsonFormat
import de.tkip.sbpm.external.auth.GoogleResponse
import de.tkip.sbpm.external.auth.GetAuthenticationState
import de.tkip.sbpm.external.auth.InitUser

class GoogleResponseActor extends Actor with HttpService with ActorLogging {
  
    private lazy val googleAuthActor = ActorLocator.googleAuthActor
    
  def actorRefFactory = context
  
  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }
  
  
  // just forward the query parameters from google to googleAuthActor
  def receive = runRoute({
    get {
      
      path("authstate") {
        parameters("id") {(id) => {
          log.debug(getClass.getName + " received question for auth state from user: " + id)
          googleAuthActor ! GetAuthenticationState(id)
          // TODO add http status code or marshal response to json
          complete("")
        }
        
      } 
      }~
      path("") {
        parameters("code", "state") {(code, state) => {
          log.debug(getClass.getName + " received from google response: " + "name: " + state + ", code: " + code)
          googleAuthActor ! GoogleResponse(state, code)
          complete("")
        } 
        }   
      }
    }~
    post {
      path("initAuth") {
        parameters("id") {(id) => {
          log.debug(getClass.getName + " received authentication init post from user: " + id)
          googleAuthActor ! InitUser(id)
          // TODO add http response -> authentication url or in case the user is alread authenticated send back a error code
          complete("")
        }
        }
      }
     }
  })
}