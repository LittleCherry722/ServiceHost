package de.tkip.sbpm.persistence.schema

import scala.slick.driver.ExtendedProfile
import scala.reflect.runtime.universe
import scala.slick.session.Database
import scala.slick.lifted.Shape
import scala.slick.lifted.Column
import scala.slick.lifted.TypeMapper

private[persistence] trait Schema {
  import Schema._

  protected implicit def config: com.typesafe.config.Config

  // akka config prefix
  protected def configPath: String

  private def configString(key: String) =
    config.getString(configPath + key)

  /**
   * provides the driver profile specified in akka config
   * sub classes can import driver.simple._ to get access
   * to driver specific classes and methods
   */
  protected val driver: ExtendedProfile =
    loadDriver(configString("slickDriver"))

  protected abstract class SchemaTable[T](tableName: String) extends driver.simple.Table[T](tableName) {

    protected def unique[C](cols: Column[C])(implicit shape: Shape[C, _, _]) =
      index("unq_idx_%s_%s".format(tableName, cols.toString), cols, unique = true)

    protected def idx[C](cols: Column[C])(implicit shape: Shape[C, _, _]) =
      index("idx_%s_%s".format(tableName, cols.toString), cols)

    protected def autoIncIdCol[C](implicit typeMapper: TypeMapper[C]) =
      column("id", O.PrimaryKey, O.AutoInc)

    protected def stringIdCol(implicit typeMapper: TypeMapper[String]) =
      column("id", DbType.stringIdentifier)

    protected def nameCol(implicit typeMapper: TypeMapper[String]) =
      column("name", DbType.name)

    protected def activeCol(implicit typeMapper: TypeMapper[Boolean]) =
      column("active", O.Default(true))

    protected val pkName = "pk_" + tableName

    protected def fkName(col: String) = "fk_%s_%s".format(tableName, col)

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

    }
  }
}

/**
 * Companion object for DatabaseAccess trait,
 * providing static methods used across JVM.
 */
private[schema] object Schema {
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
}