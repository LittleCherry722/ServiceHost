package de.tkip.sbpm.persistence

import com.typesafe.config.Config
import java.sql._
import scala.collection.mutable.ListBuffer
import akka.actor.ActorContext

object DatabaseConnection {
  val configPath = "sbpm.db."

  def apply(config: Config) = {
    Class.forName(config.getString(configPath + "driver"))
    DriverManager.getConnection(config.getString(configPath + "uri"))
  }
  
  implicit def connection(implicit context: ActorContext) = this(context.system.settings.config)

  def using[Closeable <: { def close(): Unit }, B](closeable: Closeable)(getB: Closeable => B): B =
    try {
      getB(closeable)
    } finally {
      closeable.close()
    }

  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    val ret = new ListBuffer[T]
    while (test) ret += block
    ret.toList
  }

  /** Executes the SQL and processes the result set using the specified function. */
  def query[B](sql: String)(process: ResultSet => B)(implicit connection: Connection): B =
    using(connection) { connection =>
      using(connection.createStatement) { statement =>
        using(statement.executeQuery(sql)) { results =>
          process(results)
        }
      }
    }

  /** Executes the SQL and uses the process function to convert each row into a T. */
  def queryEach[T](sql: String)(process: ResultSet => T)(implicit connection: Connection): List[T] =
    query(sql) { results =>
      bmap(results.next) {
        process(results)
      }
    }
}