package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import scala.slick.lifted.ForeignKeyAction._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity
import akka.event.Logging
import de.tkip.sbpm.persistence.schema._

private[persistence] class SchemaActor extends Actor
  with DatabaseAccess
  with GraphEdgesSchema
  with GraphNodesSchema
  with GraphRoutingsSchema
  with GroupsRolesSchema
  with GroupsUsersSchema
  with MessagesSchema
  with ProcessActiveGraphsSchema
  with ProcessInstancesSchema
  with ConfigurationsSchema
  with UserIdentitiesSchema {
  import driver.simple._

  val tables = List(
    GraphChannels,
    GraphEdges,
    GraphMacros,
    GraphMessages,
    GraphNodes,
    GraphRoutings,
    Graphs,
    GraphSubjects,
    GraphVariables,
    Groups,
    GroupsRoles,
    GroupsUsers,
    Messages,
    Processes,
    ProcessActiveGraphs,
    ProcessInstances,
    Roles,
    UserIdentities,
    Users,
    Configurations)

  val ddl = tables.map(_.ddl).reduceLeft(_ ++ _)

  def receive = {
    case query.Schema.Create => answer { implicit session =>
      create(session)
    }
    case query.Schema.Drop => answer { implicit session =>
      drop(session)
    }
    case query.Schema.Recreate => answer { implicit session =>
      drop(session)
      create(session)
    }
  }

  def create(session: Session) = {
//    log.debug("\n" + ddl.createStatements.mkString(";\n"))
    ddl.create(session)
  }

  def drop(session: Session) = {
//    log.debug("\n" + ddl.dropStatements.mkString(";\n"))
    ddl.drop(session)
  }

}