
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

/**
 * Provides helper methods for connecting to database using slick.
 * Reads JDBC connection string and driver as well as slick driver
 * from akka config (sbpm.db.uri, sbpm.db.jdbcDriver and sbpm.db.slickDriver).
 * Actors who need DB access should mixin this trait.
 */
private[persistence] trait DatabaseAccess extends ActorLogging { self: Actor =>

  override def preStart() {
    log.debug(getClass.getName + " starts...")
  }

  override def postStop() {
    log.debug(getClass.getName + " stopped.")
  }

  protected implicit val config = context.system.settings.config
  protected val configPath = DatabaseAccess.configPath

  protected val database = DatabaseAccess.connection

  // default timeout for akka message sending
  protected implicit val timeout = Timeout(30 seconds)

  /**
   * Send the result of the given function back to the sender
   * or the Exception if one occurs.
   */
  protected def answer[A](exec: Session => A) = sender ! {
    withTransaction(exec) match {
      case Success(result) => result
      case Failure(e) => akka.actor.Status.Failure(e)
    }
  }

  protected def answerProcessed[A, B](exec: Session => A)(postProcess: A => B) = sender ! {
    withTransaction(exec) match {
      case Success(preResult) =>
        Try(postProcess(preResult)) match {
          case Success(result) => result
          case Failure(e) => akka.actor.Status.Failure(e)
        }
      case Failure(e) => akka.actor.Status.Failure(e)
    }
  }

  protected def answerOptionProcessed[A, B](exec: Session => Option[A])(postProcess: A => B) = sender ! {
    withTransaction(exec) match {
      case Success(None) => None
      case Success(Some(preResult)) =>
        Try(postProcess(preResult)) match {
          case Success(result) => Some(result)
          case Failure(e) => akka.actor.Status.Failure(e)
        }
      case Failure(e) => akka.actor.Status.Failure(e)
    }
  }

  private def withTransaction[A](exec: Session => A) =
    database.withSession { session: Session =>
      session.withTransaction {
        Try(exec(session))
      }
    }

}

private[persistence] object DatabaseAccess {
  private var dataSources = Map[String, ComboPooledDataSource]()

  // akka config prefix
  val configPath = "sbpm.db."

  // read string from akka config
  private def configString(key: String)(implicit config: Config) =
    config.getString(configPath + key)

  private def configInt(key: String)(implicit config: Config) =
    config.getInt(configPath + key)

  def connection(implicit config: Config) = {
    val uri = configString("uri")
    if (!dataSources.contains(uri)) {
      val ds = new ComboPooledDataSource
      ds.setDriverClass(configString("jdbcDriver"))
      ds.setJdbcUrl(uri)
      ds.setMinPoolSize(configInt("minPoolSize"));
      ds.setAcquireIncrement(configInt("poolAcquireIncrement"));
      ds.setMaxPoolSize(configInt("maxPoolSize"));
      dataSources += (uri -> ds)
    }
    Database.forDataSource(dataSources(uri))
  }

  def cleanup() = {
    dataSources.values.foreach(DataSources.destroy)
    dataSources = Map()
  }
}
