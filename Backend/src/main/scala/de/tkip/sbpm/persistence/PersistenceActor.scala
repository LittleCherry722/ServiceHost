package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props

/**
 * do db queries based on slick (http://slick.typesafe.com/)
 */
class PersistenceActor extends Actor {

  def receive = { 
    // redirect all request for "Process" to ProcessPersistenceActor actor
    case m: ProcessAction => context.actorOf(Props[ProcessPersistenceActor]).forward(m)
    case _ => "not yet implemented"
  }
  
}