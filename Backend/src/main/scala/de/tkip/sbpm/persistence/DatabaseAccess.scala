
package de.tkip.sbpm.persistence
import scala.slick.session.Session
import scala.reflect.runtime.universe
import scala.slick.driver.ExtendedProfile
import scala.slick.session.Database
import akka.actor.Actor
import akka.actor.ActorLogging
import scala.slick.session.Session
import akka.util.Timeout
import akka.actor.ActorRef
import akka.actor.Status
import scala.util.Try
import scala.util.Success
import scala.util.Failure

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
  
  // akka config prefix
  protected val configPath = "sbpm.db."

  // read string from akka config
  protected def config(key: String) =
    context.system.settings.config.getString(configPath + key)

  /**
   * provides the driver profile specified in akka config
   * sub classes can import driver.simple._ to get access
   * to driver specific classes and methods
   */
  protected val driver: ExtendedProfile =
    DatabaseAccess.loadDriver(config("slickDriver"))

  /**
   * Create slick database connection using settings from akka config.
   */
  protected val database =
    Database.forURL(config("uri"), driver = config("jdbcDriver"))

  // default timeout for akka message sending
  protected implicit val timeout = Timeout(10)

  /**
   * Send the result of the given function back to the sender
   * or the Exception if one occurs.
   */
  protected def answer[A](f: => A)(implicit session: Session) {
    sender ! {
      Try(f) match {
        case Success(result) => result
        case Failure(e) => akka.actor.Status.Failure(e)
      }
    }
  }

  /**
   * Provides shortcuts for specifying column data type strings
   * used in slick's lifted table configuration.
   */
  object DBType {
    def varchar(length: Int) = "varchar(%d)".format(length)
    val blob = "blob"
    val smallint = "smallint"
  }
}

/**
 * Companion object for DatabaseAccess trait,
 * providing static methods used across JVM.
 */
private[persistence] object DatabaseAccess {
  /* store for currently loaded drivers by class name
  * objects cannot be loaded multiple times using reflection
  * and should therefore be stored if first loaded
  */
  private var loadedDrivers: Map[String, ExtendedProfile] = Map()
  // the scala reflection mirror for the current class loader
  private val reflection = universe.runtimeMirror(getClass.getClassLoader)

  /**
   * Load the slick driver object with the given class name using reflection.
   * If driver was loaded before the cached object instance is returned.
   * Executed synchronized to avoid race conditions.
   */
  def loadDriver(name: String): ExtendedProfile = this.synchronized {
    // try to use cached object, if not existent the object is
    // loaded using reflection
    loadedDrivers.getOrElse(name, reflectDriver(name))
  }

  // load driver by class name using reflection
  private def reflectDriver(name: String) = {
    // get object symbol
    val driverModule = reflection.staticModule(name)
    // get instance from object symbol
    val driver = reflection.reflectModule(driverModule).instance.asInstanceOf[ExtendedProfile]
    // save to cache
    loadedDrivers = loadedDrivers + (name -> driver)
    driver
  }

  // returns current timestamp in database format
  def currentTimestamp = new java.sql.Timestamp(java.lang.System.currentTimeMillis())
}