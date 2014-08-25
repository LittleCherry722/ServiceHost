package de.tkip.sbpm.persistence

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.mchange.v2.c3p0.DataSources
import com.typesafe.config.{ ConfigFactory, Config }
import de.tkip.sbpm.model.Interface
import scala.slick.driver.JdbcDriver.simple._
import akka.event.Logging

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
  import schema.GraphVarMansSchema
  import schema.ProcessEngineAddressSchema
  import DatabaseConnection.{database => db}

  val interfaces = InterfaceSchema.interfaces
  val graphConversations = GraphConversationsSchema.graphConversations
  val graphEdges = GraphEdgesSchema.graphEdges
  val graphMacros = GraphMacrosSchema.graphMacros
  val graphMessages = GraphMessagesSchema.graphMessages
  val graphNodes = GraphNodesSchema.graphNodes
  val graphs = GraphSchema.graphs
  val graphSubjects = GraphSubjectsSchema.graphSubjects
  val graphVariables = GraphVariablesSchema.graphVariables
  val graphVarMans = GraphVarMansSchema.graphVarMans
  val addresses = ProcessEngineAddressSchema.addresses

  private val tables = List(
    graphConversations,
    graphEdges,
    graphMacros,
    graphMessages,
    graphNodes,
    graphVarMans,
    graphs,
    graphSubjects,
    graphVariables)

  private val ddl = tables.map(_.ddl).reduceLeft(_ ++ _)

  private val fullGraphs = graphs
    .join(graphConversations).on(_.id === _.graphId)
    .join(graphMessages).on(_._1.id === _.graphId)
    .join(graphSubjects).on(_._1._1.id === _.graphId)
    .join(graphVariables).on(_._1._1._1.id === _.graphId)
    .join(graphMacros).on(_._1._1._1._1.id === _.graphId)
    .join(graphNodes).on(_._1._1._1._1._1.id === _.graphId)
    .join(graphVarMans).on(_._1._1._1._1._1._1.id === _.graphId)

  def loadInterfaces(): Interface = {
    val interfaceGraphs = interfaces.join(fullGraphs)
      .on(_.graphId === _._1._1._1._1._1._1._1.id)
      .join(addresses).on(_._1.addressId === _.id)

    db.withSession { implicit session =>
      val res = interfaceGraphs.list
    }

    return null
  }

  def loadInterface(id: Int): Interface = {
    val interface = interfaces.filter(_.id === id).take(1)
    val interfaceGraphs = interface.join(fullGraphs)
      .on(_.graphId === _._1._1._1._1._1._1._1.id)
      .join(addresses).on(_._1.addressId === _.id)

    db.withSession { implicit session =>
      val res = interfaceGraphs.first
    }

    return null
  }

  def createDatabase() : Unit = {
    db.withSession { session =>
      ddl.create(session)
    }
  }
  def dropDatabase() : Unit = {
    db.withSession { implicit session =>
      executeIgnoreErrors(ddl.dropStatements)
    }
  }
  def recreateDatabase() : Unit = {
    dropDatabase()
    createDatabase()
  }

  /**
   * Executes the given statements and skip all statements
   * that produce an exception.
   */
  private def executeIgnoreErrors(cmds: Iterator[String])(implicit session: Session) =
    for (s <- cmds) {
      try {
        session.withPreparedStatement(s)(_.execute)
      }
    }
}

private[persistence] object DatabaseConnection {
  // store for created connection pools
  // key is jdbc uri

  private val config = ConfigFactory.load()

  private var dataSources = Map[String, ComboPooledDataSource]()

  private val configPath = "sbpm.repo.db."

  // read string from akka config
  private def configString(key: String) = {
    config.getString(configPath + key)
  }

  // read number from akka config
  private def configInt(key: String) = {
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