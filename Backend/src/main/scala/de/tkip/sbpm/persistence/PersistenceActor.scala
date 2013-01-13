package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.AskSupport
import akka.actor.ActorLogging

// common message super class for all persistence related actions
abstract class PersistenceAction

// message to create database tables
// this message is redirected to all sub actors
// to execute the DDL commands to create their tables
case object InitDatabase

/**
 * Handles all DB operations using slick (http://slick.typesafe.com/).
 * Redirects table specific actions to sub actors.
 */
class PersistenceActor extends Actor with ActorLogging {
  // define sub actors for handling actions for different tables
  private val processActor = context.actorOf(Props[ProcessPersistenceActor], "process")
  private val graphActor = context.actorOf(Props[GraphPersistenceActor], "graph")
  private val userActor = context.actorOf(Props[UserPersistenceActor], "user")
  private val roleActor = context.actorOf(Props[RolePersistenceActor], "role")
  private val groupActor = context.actorOf(Props[GroupPersistenceActor], "group")
  private val groupRoleActor = context.actorOf(Props[GroupRolePersistenceActor], "group-role")
  private val groupUserActor = context.actorOf(Props[GroupUserPersistenceActor], "group-user")
  private val messageActor = context.actorOf(Props[MessagePersistenceActor], "message")
  private val processInstanceActor = context.actorOf(Props[ProcessInstancePersistenceActor], "process_instance")
  private val relationActor = context.actorOf(Props[RelationPersistenceActor], "relation")
  private val configurationActor = context.actorOf(Props[ConfigurationPersistenceActor], "configuration")

  def receive = {
    // redirect all request to responsible sub actors
    case m: ProcessAction => processActor.forward(m)
    case m: GraphAction => graphActor.forward(m)
    case m: UserAction => userActor.forward(m)
    case m: GroupAction => groupActor.forward(m)
    case m: RoleAction => roleActor.forward(m)
    case m: GroupRoleAction => groupRoleActor.forward(m)
    case m: GroupUserAction => groupUserActor.forward(m)
    case m: MessageAction => messageActor.forward(m)
    case m: ProcessInstanceAction => processInstanceActor.forward(m)
    case m: RelationAction => relationActor.forward(m)
    case m: ConfigurationAction => configurationActor.forward(m)
    // msg to initialize database
    case InitDatabase => init()
  }
  
  private def init() {
    // send init message to all sub actors
    // each sub actor creates its tables on its own
    val msg = InitDatabase
    processActor ! msg
    graphActor ! msg
    userActor ! msg
    roleActor ! msg
    groupActor ! msg
    groupRoleActor ! msg
    groupUserActor ! msg
    messageActor ! msg
    processInstanceActor ! msg
    relationActor ! msg
    configurationActor ! msg
  }

}
