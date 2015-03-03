package de.tkip.sbpm.persistence

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.mchange.v2.c3p0.DataSources
import com.typesafe.config.{ ConfigFactory }
import scala.slick.driver.JdbcDriver.simple._
import scala.slick.jdbc.meta.MTable

object DatabaseAccess {
  import schema.Schema.driver.simple._
  import schema.InterfaceSchema
  import schema.GraphConversationsSchema
  import schema.GraphEdgesSchema
  import schema.GraphMacrosSchema
  import schema.GraphMessagesSchema
  import schema.GraphNodesSchema
  import schema.GraphSchema
  import schema.GraphSubjectsSchema
  import schema.GraphVariablesSchema
  import schema.GraphMergedSubjectsSchema
  import schema.GraphVarMansSchema
  import schema.ProcessEngineAddressSchema
  import DatabaseConnection.database

  val interfaces = InterfaceSchema.interfaces
  val graphConversations = GraphConversationsSchema.graphConversations
  val graphEdges = GraphEdgesSchema.graphEdges
  val graphMacros = GraphMacrosSchema.graphMacros
  val graphMessages = GraphMessagesSchema.graphMessages
  val graphNodes = GraphNodesSchema.graphNodes
  val graphs = GraphSchema.graphs
  val graphSubjects = GraphSubjectsSchema.graphSubjects
  val graphMergedSubjects = GraphMergedSubjectsSchema.graphMergedSubjects
  val graphVariables = GraphVariablesSchema.graphVariables
  val graphVarMans = GraphVarMansSchema.graphVarMans
  val addresses = ProcessEngineAddressSchema.addresses
  val db = database
  val driver = schema.Schema.driver

  private val tables = List(
    interfaces,
    addresses,
    graphConversations,
    graphEdges,
    graphMacros,
    graphMessages,
    graphNodes,
    graphVarMans,
    graphs,
    graphSubjects,
    graphVariables,
    graphMergedSubjects)

  private val ddl = tables.map(_.ddl).reduceLeft(_ ++ _)

  def createDatabase() : Unit = {
    db.withSession { session =>
      ddl.create(session)
    }
  }

  def dropDatabase() : Unit = {
    db.withSession { implicit session => {
        println("dropping all tables.. if a table doesn't exist that will fail for it, but the error will be ignored")
        executeIgnoreErrors(ddl.dropStatements)
      }
    }
  }

  def recreateDatabase() : Unit = {
    dropDatabase()
    createDatabase()
  }

  def optionalCreateDatabase() : Unit = {
    db.withSession { implicit session: Session =>
      if (MTable.getTables.list(session).isEmpty) {
        createDatabase()
      }
    }
  }

  def init() : Unit = {
    DatabaseConnection.configString("startupAction") match {
      case "create" => createDatabase()
      case "optional-create" => optionalCreateDatabase()
      case "recreate" => recreateDatabase()
      case x => println("DatabaseAccess.init: startupAction is '" + x + "', doing nothing")
    }
  }

  /**
   * Executes the given statements and skip all statements
   * that produce an exception.
   */
  private def executeIgnoreErrors(cmds: Iterator[String])(implicit session: Session) =
    for (s <- cmds) {
      try {
        session.withPreparedStatement(s)(_.execute)
      } catch {
        case e: Throwable => println("ignoring error: " + e.getMessage)
      }
    }
}

private object DatabaseConnection {
  // store for created connection pools
  // key is jdbc uri

  private val config = ConfigFactory.load()

  private var dataSources = Map[String, ComboPooledDataSource]()

  private val configPath = "sbpm.repo.db."

  // read string from akka config
  def configString(key: String) = {
    config.getString(configPath + key)
  }

  // read number from akka config
  def configInt(key: String) = {
    config.getInt(configPath + key)
  }

  /**
   * Create or reuse a database connection obtained from
   * the database connection pool.
   */
  private def connection() = synchronized {
    val url = configString("uri")

    // check if connection pool for the uri already exists
    if (!dataSources.contains(url)) {
      // create new connection pool data source
      val ds = new ComboPooledDataSource
      // read pool properties from akka config
      ds.setDriverClass(configString("jdbcDriver"))
      ds.setJdbcUrl(url)
      ds.setMinPoolSize(configInt("minPoolSize"))
      ds.setAcquireIncrement(configInt("poolAcquireIncrement"))
      ds.setMaxPoolSize(configInt("maxPoolSize"))
      // add to data source pool
      dataSources += (url -> ds)
    }
    Database.forDataSource(dataSources(url))
  }
  val database = connection()


  // close all connection pools
  private def cleanup() = {
    dataSources.values.foreach(DataSources.destroy)
    dataSources = Map()
  }
}
