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

package de.tkip.sbpm.persistence.schema

import scala.slick.lifted.ForeignKeyAction._
import de.tkip.sbpm.persistence.mapping._

/**
 * Defines the database schema of GraphRoutings.
 * If you want to query GraphRoutings database table mix this trait
 * into the actor performing the queries.
 */
trait GraphRoutingsSchema extends GroupsSchema with UsersSchema with GraphSubjectsSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "graph_routings" table in the database
  // using slick's lifted embedding API
  object GraphRoutings extends SchemaTable[GraphRouting]("graph_routings") {
    def id = stringUuidCol
    def graphId = column[Int]("graph_id")
    def conditionSubjectId = column[String]("condition_subject")
    def conditionOperator = column[Boolean]("condition_operator")
    def conditionGroupId = column[Option[Int]]("condition_group_id")
    def conditionUserId = column[Option[Int]]("condition_user_id")
    def implicationSubjectId = column[String]("implication_subject")
    def implicationOperator = column[Boolean]("implication_operator")
    def implicationGroupId = column[Option[Int]]("implication_group_id")
    def implicationUserId = column[Option[Int]]("implication_user_id")

    def * = id ~ graphId ~ conditionSubjectId ~ conditionOperator ~ conditionGroupId ~
      conditionUserId ~ implicationSubjectId ~ implicationOperator ~ implicationGroupId ~
      implicationUserId <> (GraphRouting, GraphRouting.unapply _)

    def pk = primaryKey(pkName, (id, graphId))

    def graph =
      foreignKey(fkName("graphs"), graphId, Graphs)(_.id, NoAction, Cascade)
    def conditionGroup =
      foreignKey(fkName("groups_condition"), conditionGroupId, Groups)(_.id, NoAction, Cascade)
    def conditionUser =
      foreignKey(fkName("users_condition"), conditionUserId, Users)(_.id, NoAction, Cascade)
    def implicationGroup =
      foreignKey(fkName("groups_implication"), implicationGroupId, Groups)(_.id, NoAction, Cascade)
    def implicationUser =
      foreignKey(fkName("users_implication"), implicationUserId, Users)(_.id, NoAction, Cascade)
    def conditionSubject =
      foreignKey(fkName("graph_subjects_condition"), (conditionSubjectId, graphId), GraphSubjects)(s => (s.id, s.graphId), NoAction, Cascade)
    def implicationSubject =
      foreignKey(fkName("graph_subjects_implication"), (implicationSubjectId, graphId), GraphSubjects)(s => (s.id, s.graphId), NoAction, Cascade)

  }

}