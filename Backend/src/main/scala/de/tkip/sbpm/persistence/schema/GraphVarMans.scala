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

import de.tkip.sbpm.persistence.mapping._
import scala.slick.model.ForeignKeyAction._

/**
 * Defines the database schema of GraphVarMans.
 */
trait GraphVarMansSchema extends GraphNodesSchema {
  import driver.simple._

  class GraphVarMans(tag: Tag) extends SchemaTable[GraphVarMan](tag, "graph_var_mans") {
    def id = column[Short]("id", DbType.smallint)
    def macroId = column[String]("macro_id", DbType.stringIdentifier)
    def subjectId = column[String]("subject_id", DbType.stringIdentifier)
    def graphId = column[Int]("graph_id")
    def varManVar1Id = column[Option[String]]("var_man_var1_id", DbType.stringIdentifier)
    def varManVar2Id = column[Option[String]]("var_man_var2_id", DbType.stringIdentifier)
    def varManOperation = column[Option[String]]("var_man_operation", DbType.stringIdentifier)
    def varManStoreVarId = column[Option[String]]("var_man_store_var_id", DbType.stringIdentifier)

    def * = (id, macroId, subjectId, graphId, varManVar1Id, varManVar2Id, varManOperation
      , varManStoreVarId) <> (GraphVarMan.tupled, GraphVarMan.unapply)

    def pk = primaryKey(pkName, (id, macroId, subjectId, graphId))

//    def graphNode =
//      foreignKey(fkName("graph_node"), (id, subjectId, macroId, graphId), GraphNode)(n => (n.id, n.subjectId, n.macroId, n.graphId))
    def varManVar1 =
      foreignKey(fkName("graph_variables_var_man1"), (varManVar1Id.get, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
    def varManVar2 =
      foreignKey(fkName("graph_variables_var_man2"), (varManVar2Id.get, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
    def varManStore =
      foreignKey(fkName("graph_variables_var_man"), (varManStoreVarId.get, subjectId, graphId), graphVariables)(v => (v.id, v.subjectId, v.graphId))
  }

  val graphVarMans = TableQuery[GraphVarMans]
}