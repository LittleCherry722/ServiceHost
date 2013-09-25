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

import scala.concurrent.duration.DurationInt
import scala.reflect.runtime.universe
import scala.slick.driver.ExtendedProfile
import scala.slick.lifted.DDL
import scala.slick.session.Database
import scala.slick.session.Session
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import akka.util.Timeout
import com.mchange.v2.c3p0._
import com.typesafe.config.Config
import de.tkip.sbpm.logging.DefaultLogging
import de.tkip.sbpm.application.miscellaneous.SystemProperties

/**
 * Provides helper methods for connecting to database using slick.
 * Reads JDBC connection string and driver as well as slick driver
 * from akka config (sbpm.db.uri, sbpm.db.jdbcDriver and sbpm.db.slickDriver).
 * Actors who need DB access should mixin this trait.
 */
private[persistence] trait DatabaseAccess extends DefaultLogging { self: Actor =>

  // akka config to read db settings from
  protected implicit val config = context.system.settings.config
  // base path for db settings in akka config
  protected val configPath = DatabaseAccess.configPath

  // obtain a connection from the connection pool
  protected val database = DatabaseAccess.connection

  // default timeout for akka message sending
  protected implicit val timeout = Timeout(1 minute)

  /**
   * Send the result of the given function (executed in one transaction)
   * back to the sender or the exception if one occurs.
   */
  protected def answer[A](exec: Session => A) = sender ! {
    withTransaction(exec) match {
      case Success(result) => result
      case Failure(e)      => akka.actor.Status.Failure(e)
    }
  }

  /**
   * Send the result of the given functions (executed in one transaction)
   * back to the sender or the exception if one occurs.
   * exec: is executed in a database transaction
   * postProcess: can be used to modify db results after releasing database connection
   */
  protected def answerProcessed[A, B](exec: Session => A)(postProcess: A => B) = sender ! {
    withTransaction(exec) match {
      case Success(preResult) =>
        // db result ok -> execute post process function
        Try(postProcess(preResult)) match {
          case Success(result) => result
          case Failure(e)      => akka.actor.Status.Failure(e)
        }
      case Failure(e) => akka.actor.Status.Failure(e)
    }
  }

  /**
   * Send the result of the given functions (executed in one transaction)
   * back to the sender or the exception if one occurs.
   * exec: is executed in a database transaction returning single result as option
   * None is passed through to the sender without executing postProcess function
   * postProcess: can be used to modify db result after releasing database connection
   */
  protected def answerOptionProcessed[A, B](exec: Session => Option[A])(postProcess: A => B) = sender ! {
    withTransaction(exec) match {
      // db returned none -> pass it through
      case Success(None) => None
      // db result ok -> execute post process function
      case Success(Some(preResult)) =>
        Try(postProcess(preResult)) match {
          case Success(result) => Some(result)
          case Failure(e)      => akka.actor.Status.Failure(e)
        }
      case Failure(e) => akka.actor.Status.Failure(e)
    }
  }

  /**
   * Execute given function in a database transaction.
   * Returns Success or Failure whether exception occurred or not.
   */
  private def withTransaction[A](exec: Session => A) =
    database.withSession { session: Session =>
      session.withTransaction {
        Try(exec(session))
      }
    }

}

/**
 * Companion object for DatabaseTrait providing static methods.
 */
private[persistence] object DatabaseAccess {
  // store for created connection pools
  // key is jdbc uri
  private var dataSources = Map[String, ComboPooledDataSource]()

  // akka config prefix
  val configPath = "sbpm.db."

  // read string from akka config
  private def configString(key: String)(implicit config: Config) =
    config.getString(configPath + key)

  // read number from akka config
  private def configInt(key: String)(implicit config: Config) =
    config.getInt(configPath + key)

  /**
   * Create or reuse a database connection obtained from
   * the database connection pool.
   */
  def connection(implicit config: Config) = synchronized {
    val url = jdbcUrl

    // check if connection pool for the uri already exists
    if (!dataSources.contains(url)) {
      // create new connection pool data source
      val ds = new ComboPooledDataSource
      // read pool properties from akka config
      ds.setDriverClass(configString("jdbcDriver"))
      ds.setJdbcUrl(url)
      ds.setMinPoolSize(configInt("minPoolSize"));
      ds.setAcquireIncrement(configInt("poolAcquireIncrement"));
      ds.setMaxPoolSize(configInt("maxPoolSize"));
      // add to data source pool
      dataSources += (url -> ds)
    }
    Database.forDataSource(dataSources(url))
  }

  private def jdbcUrl(implicit config: Config) = {
    val uri = configString("uri")
    uri.replaceAll("\\{SBPM_PORT\\}", SystemProperties.sbpmPort.toString)
  }

  // close all connection pools
  def cleanup() = {
    dataSources.values.foreach(DataSources.destroy)
    dataSources = Map()
  }
}
