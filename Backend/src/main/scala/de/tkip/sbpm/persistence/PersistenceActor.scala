package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.AskSupport
import akka.actor.ActorLogging
import akka.pattern._
import de.tkip.sbpm.application.miscellaneous.PersistenceMessage
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.actor.ActorRef
import scala.concurrent.Await
import de.tkip.sbpm.persistence.query._
import scala.reflect.ClassTag
import akka.actor.PoisonPill

/**
 * Handles all DB operations using slick (http://slick.typesafe.com/).
 * Redirects table specific actions to sub actors.
 */
class PersistenceActor extends Actor with ActorLogging {

  def receive = {
    // redirect all request to responsible sub actors
    case q: Users.Query => forwardTo[UserPersistenceActor](q)
    case q: Groups.Query => forwardTo[GroupPersistenceActor](q)
    case q: Roles.Query => forwardTo[RolePersistenceActor](q)
    case q: GroupsRoles.Query => forwardTo[GroupRolePersistenceActor](q)
    case q: GroupsUsers.Query => forwardTo[GroupUserPersistenceActor](q)
    case q: Messages.Query => forwardTo[MessagePersistenceActor](q)
    case q: ProcessInstances.Query => forwardTo[ProcessInstancePersistenceActor](q)
    case q: Processes.Query => forwardTo[ProcessPersistenceActor](q)
    case q: Graphs.Query => forwardTo[GraphPersistenceActor](q)
    case q: Schema.Query => forwardTo[SchemaActor](q)
  }

  private def forwardTo[A <: Actor: ClassTag](query: BaseQuery) = {
    val actor = context.actorOf(Props[A])
    actor.forward(query)
    actor ! PoisonPill
  }

}
