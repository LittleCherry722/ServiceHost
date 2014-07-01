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

import scala.slick.driver.{SQLiteDriver, JdbcProfile}
import scala.slick.driver.SQLiteDriver.simple.{Table, Tag}
// import scala.slick.session.Database

/**
 * Base trait for all schema definition traits.
 * Provides common properties and methods for
 * schema definitions and injects slick driver
 * according to configuration.
 */
private[persistence] trait Schema {

  // sub classes should provide akka config
  protected implicit def config: com.typesafe.config.Config

  // sub classes should provide akka config prefix
  protected def configPath: String

  // read config string from akka config
  private def configString(key: String) =
    config.getString(configPath + key)

  protected val driver = SQLiteDriver

  /**
   * Abstract base class for the table definition.
   * Extends slick's table for lifted embedding.
   * Provides methods to define commonly used
   * constraints and provides default column types.
   */
  protected abstract class SchemaTable[T](tag: Tag, tableName: String) extends Table[T](tag, tableName) {
    import scala.slick.driver.SQLiteDriver.simple._

    /**
     * Defines an unique index on the given columns.
     */
    protected def unique[C](cols: Column[C])(implicit shape: Shape[_, C, _, _]) =
      index("unq_idx_%s_%s".format(tableName, cols.toString), cols, unique = true)

    /**
     * Defines an index on the given columns.
     */
    protected def idx[C](cols: Column[C])(implicit shape: Shape[_, C, _, _]) =
      index("idx_%s_%s".format(tableName, cols.toString), cols)

    /**
     * Defines am "id" column as auto increment primary key.
     */
    protected def autoIncIdCol[C](implicit typeMapper: ColumnType[C]) =
      column("id", O.PrimaryKey, O.AutoInc)

    /**
     * Defines an "id" column as string primary key.
     */
    protected def stringIdCol(implicit typeMapper: ColumnType[String]) =
      column("id", DbType.stringIdentifier)
      
      /**
     * Defines an "id" column as uuid string primary key.
     */
    protected def stringUuidCol(implicit typeMapper: ColumnType[String]) =
      column("id", DbType.uuid)

    /**
     * Defines a "name" string column.
     */
    protected def nameCol(implicit typeMapper: ColumnType[String]) =
      column("name", DbType.name)

    /**
     * Defines a "active" boolean column.
     */
    protected def activeCol(implicit typeMapper: ColumnType[Boolean]) =
      column("active", O.Default(true))

    /**
     * Defines a "gdriveId" string column.
     */
    protected def gdriveIdCol(implicit typeMapper: ColumnType[String]) =
      column("gdrive_id", DbType.eMail)

    /**
     * Provides the default primary key name: "pk_tableName"
     */
    protected val pkName = "pk_" + tableName

    /**
     * Provides the default foreign key name: "fk_tableName_colName"
     */
    protected def fkName(col: String) = "fk_%s_%s".format(tableName, col)

    /**
     * Provides commonly used column types.
     */
    protected object DbType {
      def varchar(length: Int) =
        O.DBType("varchar(%d)".format(length))

      def blob =
        O.DBType("blob")

      def smallint =
        O.DBType("smallint")

      def char(length: Int) =
        O.DBType("char(%d)".format(length))

      def tinyint =
        O.DBType("tinyint")

      def stringIdentifier =
        varchar(16)

      def name =
        varchar(32)

      def comment =
        varchar(256)

      def bcrypt =
        char(60)

      def eMail =
        varchar(64)

      def uuid =
        varchar(36)

    }
  }
}