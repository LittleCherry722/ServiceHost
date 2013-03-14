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

// common message super class for all persistence related actions
trait PersistenceAction extends PersistenceMessage

// message to create database tables
// this message is redirected to all sub actors
// to execute the DDL commands to create their tables
case object InitDatabase extends PersistenceAction

// message to drop database tables
// this message is redirected to all sub actors
// to execute the DDL commands to drop their tables
case object DropDatabase extends PersistenceAction

/**
 * Handles all DB operations using slick (http://slick.typesafe.com/).
 * Redirects table specific actions to sub actors.
 */
class PersistenceActor extends Actor with ActorLogging {
  // define sub actors for handling actions for different tables
  private lazy val processActor = context.actorOf(Props[ProcessPersistenceActor], "process")
  private val graphActor = context.actorOf(Props[GraphPersistenceActor], "graph")
  private lazy val userActor = context.actorOf(Props[UserPersistenceActor], "user")
  private lazy val roleActor = context.actorOf(Props[RolePersistenceActor], "role")
  private lazy val groupActor = context.actorOf(Props[GroupPersistenceActor], "group")
  private lazy val groupRoleActor = context.actorOf(Props[GroupRolePersistenceActor], "group-role")
  private lazy val groupUserActor = context.actorOf(Props[GroupUserPersistenceActor], "group-user")
  private lazy val messageActor = context.actorOf(Props[MessagePersistenceActor], "message")
  private lazy val processInstanceActor = context.actorOf(Props[ProcessInstancePersistenceActor], "process-instance")
  private lazy val relationActor = context.actorOf(Props[RelationPersistenceActor], "relation")
  private lazy val configurationActor = context.actorOf(Props[ConfigurationPersistenceActor], "configuration")

  def receive = {
    // redirect all request to responsible sub actors
    case m: ProcessAction => forwardTo(m, processActor)
    case m: GraphAction => forwardTo(m, graphActor)
    case m: UserAction => forwardTo(m, userActor)
    case m: GroupAction => forwardTo(m, groupActor)
    case m: RoleAction => forwardTo(m, roleActor)
    case m: GroupRoleAction => forwardTo(m, groupRoleActor)
    case m: GroupUserAction => forwardTo(m, groupUserActor)
    case m: MessageAction => forwardTo(m, messageActor)
    case m: ProcessInstanceAction => forwardTo(m, processInstanceActor)
    case m: RelationAction => forwardTo(m, relationActor)
    case m: ConfigurationAction => forwardTo(m, configurationActor)
    // msg to initialize database
    case InitDatabase => init()
    // msg to destroy database
    case DropDatabase => destroy()
  }

  implicit val timeout = Timeout(30 seconds)
  implicit val execCtx = context.system.dispatcher

  private def forwardTo(m: PersistenceAction, actor: ActorRef) = {
    actor.forward(m)
  }

  private def init() {
    // send init message to all sub actors
    // each sub actor creates its tables on its own
    execOnAllActors(InitDatabase)
  }

  private def destroy() {
    // send init message to all sub actors
    // each sub actor creates its tables on its own
    execOnAllActors(DropDatabase)
  }

  private def execOnAllActors(msg: PersistenceAction) {
    // send message to all sub actors
    val actors = List(
      processActor,
      graphActor,
      userActor,
      roleActor,
      groupActor,
      groupRoleActor,
      groupUserActor,
      messageActor,
      processInstanceActor,
      relationActor,
      configurationActor)

    for (actor <- actors) {
      Await.result(actor ? msg, timeout.duration) match {
        case f: akka.actor.Status.Failure => {
          sender ! f
          return
        }
        case _ =>
      }
    }

    sender ! true
  }
}
