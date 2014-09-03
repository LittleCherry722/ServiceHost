package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.{Address, InterfaceImplementation}
import de.tkip.sbpm.{model => M}
import de.tkip.sbpm.persistence.{mapping => D}
import de.tkip.sbpm.persistence.DatabaseAccess._
import de.tkip.sbpm.persistence.mapping.DomainModelMappings.convert
import driver.simple._

/**
 * Created by arne on 26.08.14.
 */
object InterfaceQuery {
  def loadInterfaces(): Seq[M.Interface] = {
    var is : Seq[M.Interface] = null
    db.withSession { implicit session =>
      session.withTransaction {
        is = interfaces.buildColl[Seq].map { i =>
          val subEntities = retrieveSubEntities(i.graphId)
          val dbAddress = processEngineAddressForId(i.addressId).run.head
          convert(i, dbAddress, subEntities)
        }
      }
    }
    is
  }

  def loadInterface(id: Int): Option[M.Interface] = {
    var interface : Option[M.Interface] = None
    db.withSession { implicit session =>
      session.withTransaction {
        interfaceForId(id).run.headOption match {
          case Some(dbInterface) =>
            val subEntities = retrieveSubEntities(dbInterface.graphId)
            val dbAddress = processEngineAddressForId(dbInterface.addressId).run.head
            println(s"conversations: ${subEntities._1}")
            println(s"messages: ${subEntities._2}")
            println(s"subjects: ${subEntities._3}")
            println(s"variables: ${subEntities._4}")
            println(s"macros: ${subEntities._5}")
            println(s"nodes: ${subEntities._6}")
            println(s"varmans: ${subEntities._7}")
            println(s"edges: ${subEntities._8}")
            interface = Some(convert(dbInterface, dbAddress, subEntities))
          case None => interface = None
        }
      }
    }
    interface
  }

  def findImplementations(subjectId: String): Seq[InterfaceImplementation] = {
    db.withSession { implicit session =>
      val subjects = graphSubjectsForSubjectId(subjectId).run
      return subjects.map { s =>
        InterfaceImplementation(
          processId = s._1,
          address = Address(id = None, ip = s._2, port = s._3),
          subjectId = subjectId)
      }
    }
  }

  def deleteInterfaceById(interfaceId: Int) : Boolean = {
    db.withSession { implicit session =>
      session.withTransaction {
        return interfaceForId(interfaceId).run.headOption match {
          case None => false
          case Some(interface) =>
            graphs.filter(_.id === interface.graphId).delete
            deleteSubEntities(interface.graphId)
            interfaces.filter(_.id === interfaceId).delete
            true
        }
      }
    }
  }

  // update entity or throw exception if it does not exist
  def saveInterface(i: M.Interface) : Int = {
    var returnInterfaceId : Int = 0
    val g = i.graph
    db.withSession { implicit session =>
      session.withTransaction {
        var graphId : Int = 0
        // convert domain model graph to db entities
        val (graph, conversations, messages, subjects, variables, macros, nodes, varMans, edges) =
          convert(g) match {
            // only graph was converted, because it's a new graph (no id exits)
            case Left(model: D.Graph) =>
              println("graph has no id, inserting new")
              // insert graph to get it's id
              graphId = (graphs returning graphs.map(_.id)) += model
              // then convert model again with known graph id
              convert(g.copy(Some(graphId))).right.get
            case Right(models) =>
              println("graph has id, updating")
              graphId = g.id.get
              // id of graph was given -> update existing
              // first check if graph really exists
              val q = graphs.filter(_.id === models._1.id)
//              if (!q.firstOption.isDefined) throw new Exception("Graph with id %d does not exist.", models._1.id.get)
              // update graph
              q.update(models._1)
              models
          }

        val a = i.address
        val dbAddress = processEngineAddressForIpPort(a.ip, a.port).run.headOption match {
          case Some(address) => address
          case None => (addresses returning addresses.map(_.id) into ((user,id) => user.copy(id=Some(id)))) += D.ProcessEngineAddress(id = None, ip = a.ip, port = a.port)
        }

        val addressId = dbAddress.id.get
        val dbInterface = D.Interface(
          id = i.id, name = i.name,
          addressId = addressId,
          graphId = graphId,
          processId = i.processId
        )
        deleteSubEntities(graph.id.get)

        // delete all dependent entities of graph and
        // insert them with new values again

        println(s"deleted sub entities for graphId ${graph.id.get}")

        graphConversations.insertAll(conversations: _*)
        graphMessages.insertAll(messages: _*)
        graphVariables.insertAll(variables: _*)
        graphSubjects.insertAll(subjects: _*)
        graphMacros.insertAll(macros: _*)
        graphNodes.insertAll(nodes: _*)
        graphVarMans.insertAll(varMans: _*)
        graphEdges.insertAll(edges: _*)
        println("inserted new sub enties")
        println(s"graphConversations: $conversations")
        println(s"messages: $messages")
        println(s"macros: $macros")
        println(s"nodes: $nodes")
        println(s"varMans: $varMans")
        println(s"edges: $edges")
        println(s"subjects: $subjects")



        returnInterfaceId = dbInterface.id match {
          case None     => (interfaces returning interfaces.map(_.id)) += dbInterface
          case Some(id) =>
            (interfaces.filter(_.id === id) returning interfaces.map(_.id)).insertOrUpdate(dbInterface).head
        }
      }
      returnInterfaceId
    }

    // only return id if graph was inserted
    // on update return None

  }

  /**
   * Delete all dependent entities of a graph with given id.
   */
  private def deleteSubEntities(graphId: Int)(implicit session: Session) = {
    graphEdges.filter(_.graphId === graphId).delete
    graphNodes.filter(_.graphId === graphId).delete
    graphVarMans.filter(_.graphId === graphId).delete
    graphMacros.filter(_.graphId === graphId).delete
    graphVariables.filter(_.graphId === graphId).delete
    graphSubjects.filter(_.graphId === graphId).delete
    graphMessages.filter(_.graphId === graphId).delete
    graphConversations.filter(_.graphId === graphId).delete
  }

  private def retrieveSubEntities(id: Int)(implicit session: Session) = {
    (
      graphConversiationsForGraphId(id).run,
      graphMessagesForGraphId(id).run,
      graphSubjectsForGraphId(id).run,
      graphVariablesForGraphId(id).run,
      graphMacrosForGraphId(id).run,
      graphNodesForGraphId(id).run,
      graphVarMansForGraphId(id).run,
      graphEdgesForGraphId(id).run
      )
  }

  private val interfaceForId = Compiled { id: Column[Int] => interfaces.filter(_.id === id).take(1) }
  private val processEngineAddressForId = Compiled{ id: Column[Int] => addresses.filter(_.id === id).take(1) }
  private val processEngineAddressForIpPort = Compiled{ (ip: Column[String], port: Column[Int]) => addresses.filter(_.ip === ip).filter(_.port === port).take(1) }
  private val graphConversiationsForGraphId = Compiled{ id: Column[Int] => graphConversations.filter(_.graphId === id) }
  private val graphMessagesForGraphId = Compiled{ id: Column[Int] => graphMessages.filter(_.graphId === id) }
  private val graphSubjectsForGraphId = Compiled{ id: Column[Int] => graphSubjects.filter(_.graphId === id) }
  private val graphSubjectsForSubjectId = Compiled{ id: Column[String] =>
    for {
      subject <- graphSubjects.filter(_.id === id).filter(_.subjectType === "single")
      interface <- interfaces if interface.graphId == subject.graphId
      address <- addresses if address.id == interface.addressId
    } yield (interface.processId, address.ip, address.port)
  }
  private val graphVariablesForGraphId = Compiled{ id: Column[Int] => graphVariables.filter(_.graphId === id) }
  private val graphMacrosForGraphId = Compiled{ id: Column[Int] => graphMacros.filter(_.graphId === id) }
  private val graphNodesForGraphId = Compiled{ id: Column[Int] => graphNodes.filter(_.graphId === id) }
  private val graphVarMansForGraphId = Compiled{ id: Column[Int] => graphVarMans.filter(_.graphId === id) }
  private val graphEdgesForGraphId = Compiled{ id: Column[Int] => graphEdges.filter(_.graphId === id) }


}