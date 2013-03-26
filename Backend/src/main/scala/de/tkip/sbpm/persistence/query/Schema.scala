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

package de.tkip.sbpm.persistence.query

/**
 * PersistenceActor queries for all schema related operations.
 */
object Schema {
  // used to identify all Schema queries
  trait Query extends BaseQuery
  
  /**
   * create database schema
   * executes DDL to create tables
   */
  case object Create extends Query
  /**
   * drop database schema
   * executes DDL to drop tables
   */
  case object Drop extends Query
  /**
   * recreate database schema
   * executes DDL to drop tables and
   * then to create tables
   */
  case object Recreate extends Query
}