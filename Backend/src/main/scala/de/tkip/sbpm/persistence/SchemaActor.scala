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

package de.tkip.sbpm.persistence

import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import scala.slick.lifted.ForeignKeyAction._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity
import akka.event.Logging
import de.tkip.sbpm.persistence.schema._
import scala.slick.lifted.DDL

/**
 * Handle create and drop operations for all
 * tables in the database schema.
 */
private[persistence] class SchemaActor extends Actor
  with DatabaseAccess
  with GraphEdgesSchema
  with GraphNodesSchema
  with GraphVarMansSchema
  with GraphRoutingsSchema
  with GroupsRolesSchema
  with GroupsUsersSchema
  with MessagesSchema
  with ProcessActiveGraphsSchema
  with ProcessInstancesSchema
  with ConfigurationsSchema
  with UserIdentitiesSchema {

  // import current slick driver dynamically
  import driver.simple._

  private val tables = List(
    GraphConversations,
    GraphEdges,
    GraphMacros,
    GraphMessages,
    GraphNodes,
    GraphVarMans,
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

  // concat all table DDLs to insert at once
  private val ddl = tables.map(_.ddl).reduceLeft(_ ++ _)

  def receive = {
    // create new schema
    case query.Schema.Create => answer { implicit session =>
      create(session)
    }
    // drop existing schema (ignore missing tables)
    case query.Schema.Drop => answer { implicit session =>
      drop(session)
    }
    // combination of drop and create
    case query.Schema.Recreate => answer { implicit session =>
      drop(session)
      create(session)
    }
  }

  /**
   * Create db schema using all table DDLs.
   */
  private def create(session: Session) =
    ddl.create(session)

  /**
   * Drop all tables using their DDLs.
   * Ignore errors if tables not exist.
   */
  private def drop(implicit session: Session) =
    executeIgnoreErrors(ddl.dropStatements)

  /**
   * Executes the given statements and skip all statements
   * that produce an exception.
   */
  protected def executeIgnoreErrors(cmds: Iterator[String])(implicit session: Session) =
    for (s <- cmds) {
      try {
        session.withPreparedStatement(s)(_.execute)
      } catch {
        // continue but log warning
        case e: Throwable => log.warning(e.getMessage)
      }
    }

}