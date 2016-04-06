package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.JsonProtocol._
import de.tkip.sbpm.model.{Address, InterfaceImplementation, View}
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.persistence.DatabaseAccess._
import de.tkip.sbpm.persistence.DatabaseAccess.driver.simple._
import de.tkip.sbpm.persistence.mapping.DomainModelMappings.{convertGraph, convertInterface, convertView}
import de.tkip.sbpm.persistence.{mapping => D}
import de.tkip.sbpm.verification.ModelBisimulation.{SubjectIdMap, checkGraphs}
import de.tkip.sbpm.{model => M}


object InterfaceQuery {
  case class InterfaceSaveResult(id: Int, outgoingSubjectMap: Map[String, String], incomingSubjectMap: Map[String, String])
  implicit val interfaceSaveResultFormat = jsonFormat3(InterfaceSaveResult)

  case class IdResult(id: Int)
  implicit val idResultFormat = jsonFormat1(IdResult)

  private sealed trait GraphOrViewId
  private case class GraphId(id: Int) extends GraphOrViewId
  private case class ViewId(id: Int, subjectIdMap: SubjectIdMap) extends GraphOrViewId

  // Done?
  def loadInterfaces(): Seq[M.Interface] = {
    var is : Seq[M.Interface] = null
    db.withSession { implicit session =>
      session.withTransaction {
        val filteredIs = interfaces.filter{interface => views.filter(_.interfaceId === interface.id).exists}
        is = filteredIs.buildColl[Seq].map { i =>
          println("getting views")
          val views = retrieveViews(i.id.get)
          println("getting process engine address for id")
          val dbAddress = processEngineAddressForId(i.addressId).run.head
          println("converting interface")
          convertInterface(i, dbAddress, views)
        }
      }
    }
    is
  }

  // Done?
  def loadInterface(id: Int): Option[M.Interface] = {
    var interface : Option[M.Interface] = None
    db.withSession { implicit session =>
      session.withTransaction {
        interfaceForId(id).run.headOption match {
          case Some(dbInterface) =>
            val views = retrieveViews(id)
            val dbAddress = processEngineAddressForId(dbInterface.addressId).run.head
            interface = Some(convertInterface(dbInterface, dbAddress, views))
          case None => interface = None
        }
      }
    }
    interface
  }

  // DONE
  def findImplementations(subjectId: String): Seq[InterfaceImplementation] = {
    var returnSubjects = Seq[InterfaceImplementation]()
    db.withSession { implicit session =>
      val subjects = implementationsForSubjectId(subjectId).distinct
//      val implementationIds = subjects.flatMap(_._1.id)
      returnSubjects = subjects.map {
        case (implementation, address) =>
          InterfaceImplementation(
            viewId = implementation.viewId,
            ownProcessId = implementation.processId,
            ownAddress = Address(id = None, ip = address.ip, port = address.port),
            ownSubjectId = implementation.ownSubjectId,
            dependsOnInterface = implementation.dependsOnInterface)
      }
    }
    returnSubjects
  }

  // DONE
  def deleteInterfaceById(interfaceId: Int) : Boolean = {
    var deleteSuccess = false
    var nextInterfaceIds: Seq[Int] = Seq.empty
    db.withSession { implicit session =>
      session.withTransaction {
        deleteSuccess = interfaceForId(interfaceId).run.headOption match {
          case None => false
          case Some(interface) =>
            val emptyViewsQ = emptyViews.filter(_.interfaceId === interface.id)
            // If this interface has (only) empty views, delete those and the interface
            // Interfaces with empty views are never referenced by other interfaces
            // so they are save to delete.
            if (emptyViewsQ.exists.run) {
              val deletedMainInterface = for {
                ev <- emptyViewsQ
                v <- views if v.id === ev.viewId
                i <- interfaces if i.deleted && i.id === v.interfaceId
              } yield i
              val otherInterfaces = for {
                ev <- emptyViewsQ
                oev <- emptyViews if oev.viewId === ev.viewId && oev.interfaceId =!= ev.interfaceId
              } yield oev.id
              emptyViewSubjectMappings.filter(_.emptyViewId in emptyViewsQ.map(_.id)).delete
              emptyViewsQ.delete
              interfaces.filter(_.id === interfaceId).delete
              if (!otherInterfaces.exists.run) {
                // If no other interfaces exist for the main interface this interface references,
                // and the main interface is marked as deleted, also delete the main interface.
                nextInterfaceIds = deletedMainInterface.map(_.id).run
              }
            } else {
              // No emptyViews reference this view, which means this interface is a regular
              // interface with a graph and everything.
              val viewsQ = views.filter(_.interfaceId === interface.id)
              val graphIds = viewsQ.map(_.graphId).run
              val otherViewsQ = for {
                v <- viewsQ
                ev <- emptyViews if ev.viewId === v.id
              } yield ev.id
              if (otherViewsQ.exists.run) {
                // Interfaces depending on this one exist, mark as deleted but do not delete
                interfaces.filter(_.id === interfaceId).map(_.deleted).update(true)
              } else {
                // No other interfaces exist that depend on this interface, completely delete this interface
                interfaces.filter(_.id === interfaceId).delete
                interfaceImplementations.filter(_.viewId in viewsQ.map(_.id)).delete
                viewsQ.delete
                deleteSubEntities(graphIds)
              }
            }
            true
        }
      }
    }
    nextInterfaceIds.map(deleteInterfaceById)
    deleteSuccess
  }

  def saveInterface(i: M.Interface, localSubjectId: SubjectId) : InterfaceSaveResult = {
    var interfaceSaveResult: InterfaceSaveResult = null
    db.withSession { implicit session =>
      session.withTransaction {
        interfaceSaveResult = persistInterface(i, localSubjectId)
      }
    }
    interfaceSaveResult
  }

  def saveImplementation(ti: M.InterfaceImplementation): Int = {
    println(s"saving implementation: $ti")
    var implementationId: Int = 0
    db.withSession { implicit session =>
      session.withTransaction {
        implementationId = saveImplementationWithTransaction(ti)
      }
    }
    implementationId
  }

  def deleteImplementation(iId: Int) = {
    db.withSession { implicit session =>
      session.withTransaction {
        interfaceImplementations.filter(_.id === iId).delete
      }
    }
  }

  private def saveImplementationWithTransaction(i: M.InterfaceImplementation, localSubjectId: Option[SubjectId] = None)(implicit session: Session): Int = {
    lazy val viewSubjectId = views.filter(_.id === i.viewId).map(_.mainSubjectId).first
    val address = D.ProcessEngineAddress(None, ip = i.ownAddress.ip, port = i.ownAddress.port)
    val addressId = (addresses returning addresses.map(_.id)) += address
    val implementation = D.InterfaceImplementation(id = None,
      processId = i.ownProcessId,
      addressId = addressId,
      ownSubjectId = localSubjectId.getOrElse(viewSubjectId),
      viewId = i.viewId,
      dependsOnInterface = i.dependsOnInterface)
    (interfaceImplementations returning interfaceImplementations.map(_.id)) += implementation
  }

  private def persistInterface(i: M.Interface, localSubjectId: SubjectId)(implicit session: Session): InterfaceSaveResult = {
    println("persisting interface")
    val vs = i.views.values.toSeq
    val graphModels = allGraphs()
    val viewGraphIds: Seq[(View, GraphOrViewId)] = vs.map{ v =>
      val mappings: Option[(Int, SubjectIdMap)] = graphModels.foldLeft(None: Option[(Int, SubjectIdMap)]){
        case (res@Some(_), _) => res
        case (None, otherGraph) => checkGraphs(v.graph, otherGraph)
      }
      val graphOrViewId = mappings match {
        case None => GraphId(saveGraph(v.graph))
        case Some((otherGraphId, subjectIdMap)) =>
          val otherViewId = views.filter{ov => ov.graphId === otherGraphId}.map(_.id).take(1).run.head
          println(s"found other view: $otherViewId for graph with id: $otherGraphId")
          ViewId(id = otherViewId, subjectIdMap = subjectIdMap)
      }
      (v, graphOrViewId)
    }

    val a = i.address
    val dbAddress = processEngineAddressForIpPort(a.ip, a.port).run.headOption match {
      case Some(address) => address
      case None => (addresses returning addresses.map(_.id) into ((user,id) => user.copy(id=Some(id)))) += D.ProcessEngineAddress(id = None, ip = a.ip, port = a.port)
    }

    val addressId = dbAddress.id.get
    val dbInterface = D.Interface(
      interfaceType = i.interfaceType.toString,
      id = i.id,
      name = i.name,
      addressId = addressId,
      processId = i.processId
    )

    val interfaceId = dbInterface.id match {
      case None     => (interfaces returning interfaces.map(_.id)) += dbInterface
      case Some(id) =>
        val oldViews = views.filter(_.interfaceId === i.id).run
        if (oldViews.isEmpty) {
          (interfaces returning interfaces.map(_.id)) += dbInterface
        } else {
          deleteSubEntities(oldViews.map(_.graphId))
          views.filter(_.id inSet oldViews.flatMap(_.id)).delete
          interfaces.filter(_.id === id).map { i => (i.name, i.addressId, i.processId) }.update(
            (dbInterface.name, dbInterface.addressId, dbInterface.processId)
          )
          id
        }
    }

    val mappings: (Map[SubjectId, SubjectId], Map[SubjectId, SubjectId]) = viewGraphIds.map {
      case (v, GraphId(graphId)) =>
        val view = D.View(id = None, interfaceId = interfaceId, mainSubjectId = v.mainSubjectId, graphId = graphId)
        val viewId = (views returning views.map(_.id)) += view
        if (v.mainSubjectId == localSubjectId) {
          val implementation = InterfaceImplementation(viewId = viewId, ownAddress = i.address, ownProcessId = i.processId, ownSubjectId = localSubjectId, dependsOnInterface = None)
          saveImplementationWithTransaction(implementation, Some(localSubjectId))
        }
        (Map.empty, Map.empty)
      case (v, ViewId(otherViewId, subjectIdMap)) =>
        val outInSubjectMapping = if (v.mainSubjectId == localSubjectId) {
          val implementation = InterfaceImplementation(viewId = otherViewId, ownAddress = i.address, ownProcessId = i.processId, ownSubjectId = localSubjectId, dependsOnInterface = None)
          saveImplementationWithTransaction(implementation, Some(localSubjectId))
          val normalizedMainSubjectId = subjectIdMap(v.mainSubjectId)
          val outSubjectIdMap = v.graph.subjects.keys.filter(_ != v.mainSubjectId).map{ sId =>
            (sId, normalizedMainSubjectId)
          }.toMap
          (outSubjectIdMap, subjectIdMap)
        } else {
          (Map.empty, Map.empty)
        }
        val view = D.EmptyView(id = None, interfaceId = interfaceId, viewId = otherViewId)
        val vId = (emptyViews returning emptyViews.map(_.id)) += view
        println(s"inserting new empty view: ${view.copy(id = Some(vId))}")
        println(s"currently inserted empty views: ${emptyViews.run}")
        val subjectMappings = subjectIdMap.map {
          case (from, to) => D.EmptyViewSubjectMap(emptyViewId = vId, from = from, to = to)
        }.toSeq
        emptyViewSubjectMappings.insertAll(subjectMappings: _*)
        outInSubjectMapping
    }.foldLeft((Map.empty[String, String], Map.empty[String, String])) {
      case ((accSubjectIdMap, accMsgMap), (newSubjectIdMap, newMsgMap)) =>
        (accSubjectIdMap ++ newSubjectIdMap, accMsgMap ++ newMsgMap)
    }
    InterfaceSaveResult(id = interfaceId, outgoingSubjectMap = mappings._1, incomingSubjectMap = mappings._2)
  }

  private def saveGraph(g: M.Graph)(implicit session: Session): Int = {
    var graphId : Int = 0
    // convert domain model graph to db entities
    val (graph, mergedSubjects, conversations, messages, subjects, variables, macros, nodes, varMans, edges) =
      convertGraph(g) match {
        // only graph was converted, because it's a new graph (no id exits)
        case Left(model: D.Graph) =>
          println("graph has no id, inserting new")
          // insert graph to get it's id
          graphId = (graphs returning graphs.map(_.id)) += model
          // then convert model again with known graph id
          convertGraph(g.copy(Some(graphId))).right.get
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

    deleteSubEntities(Seq(graphId))

    // delete all dependent entities of graph and
    // insert them with new values again

    println(s"deleted sub entities for graphId $graphId")

    graphMergedSubjects.insertAll(mergedSubjects: _*)
    graphConversations.insertAll(conversations: _*)
    graphMessages.insertAll(messages: _*)
    graphVariables.insertAll(variables: _*)
    graphSubjects.insertAll(subjects: _*)
    graphMacros.insertAll(macros: _*)
    graphNodes.insertAll(nodes: _*)
    graphVarMans.insertAll(varMans: _*)
    graphEdges.insertAll(edges: _*)

    graphId
  }

  private def allGraphs()(implicit session: Session): Seq[M.Graph] = {
    val graphIds: Seq[D.Graph] = graphs.run
    graphIds.flatMap{ g =>
      g.id.map( gId =>
        convertGraph(g, retrieveSubEntities(gId))
      )
    }
  }

  private def retrieveViews(interfaceId: Int)(implicit session: Session): Seq[M.View] = {
    for{
      view <- allViewsForInterfaceId(interfaceId).run
      subModels = retrieveSubEntities(view.graphId)
      implementations = view.id.map(id => retrieveImplementations(id)).getOrElse(Seq.empty)
      domainModelView = convertView(view, implementations, subModels)
    } yield domainModelView
  }

  /**
   * Delete all dependent entities of graphs with given ids.
   */
  private def deleteSubEntities(graphIds: Seq[Int])(implicit session: Session) = {
    graphEdges.filter(_.graphId inSet graphIds).delete
    graphNodes.filter(_.graphId inSet graphIds).delete
    graphVarMans.filter(_.graphId inSet graphIds).delete
    graphMacros.filter(_.graphId inSet graphIds).delete
    graphVariables.filter(_.graphId inSet graphIds).delete
    graphSubjects.filter(_.graphId inSet graphIds).delete
    graphMessages.filter(_.graphId inSet graphIds).delete
    graphConversations.filter(_.graphId inSet graphIds).delete
    graphMergedSubjects.filter(_.graphId inSet graphIds).delete
  }


  private def retrieveSubEntities(graphId: Int)(implicit session: Session) = {
    (
      graphMergedSubjectsForGraphId(graphId).run,
      graphConversationsForGraphId(graphId).run,
      graphMessagesForGraphId(graphId).run,
      graphSubjectsForGraphId(graphId).run,
      graphVariablesForGraphId(graphId).run,
      graphMacrosForGraphId(graphId).run,
      graphNodesForGraphId(graphId).run,
      graphVarMansForGraphId(graphId).run,
      graphEdgesForGraphId(graphId).run
      )
  }

  private def retrieveImplementations(viewId: Int)(implicit session: Session) = {
    val implsQuery = for {
      i <- interfaceImplementations if i.viewId === viewId
      a <- addresses if a.id === i.addressId
    } yield (i, a)
    val impls = implsQuery.run
    val implIds = impls.flatMap { case (i, a) => i.id }
    val impSubjectMappings = implementationSubjectMappings.filter(_.implementationId.inSet(implIds)).run.groupBy(_.implementationId)
    impls.map{ case (i, a) =>
      val subjMap = i.id.flatMap(id => impSubjectMappings.get(id)).getOrElse(Seq.empty)
      (i, a, subjMap)
    }
  }

  private val interfaceForId = Compiled { id: Column[Int] => interfaces.filter(_.id === id).take(1) }
  private val interfacesForType = Compiled { interfaceType: Column[String] => interfaces.filter(_.interfaceType === interfaceType) }
  private val processEngineAddressForId = Compiled{ id: Column[Int] => addresses.filter(_.id === id).take(1) }
  private val processEngineAddressForIpPort = Compiled{ (ip: Column[String], port: Column[Int]) => addresses.filter(_.ip === ip).filter(_.port === port).take(1) }
  private val graphConversationsForGraphId = Compiled{ id: Column[Int] => graphConversations.filter(_.graphId === id) }
  private val graphMessagesForGraphId = Compiled{ id: Column[Int] => graphMessages.filter(_.graphId === id) }
  private val graphSubjectsForGraphId = Compiled{ id: Column[Int] => graphSubjects.filter(_.graphId === id) }
  private def allViewsForInterfaceId(interfaceId: Column[Int]) =  {
    val emptyViewIds = emptyViews.filter(_.interfaceId === interfaceId).map(_.viewId)
    views.filter(v => v.interfaceId === interfaceId || v.id.in(emptyViewIds))
  }
  private def implementationsForSubjectId(id: String)(implicit session: Session) = {
    def interfaceImplemented(iId: Int): Boolean = {
      val viewIds = allViewsForInterfaceId(iId).map(_.id).run
      viewIds.map { vId =>
        val isTrivial = interfaceImplementations.filter(i => i.dependsOnInterface.isEmpty && i.viewId === vId).exists.run
        lazy val isComplex = interfaceImplementations.filter(i => i.dependsOnInterface.isDefined && i.viewId === vId).run.exists(implementationValid)
        isTrivial || isComplex
      }.forall(_ == true)
    }
    def implementationValid(i: D.InterfaceImplementation): Boolean = {
      i.dependsOnInterface match {
        case None => true
        case Some(iId) => interfaceImplemented(iId)
      }
    }
    val implementationsWithAddress = for {
      implementation <- interfaceImplementations.filter(_.viewId in interfaceImplementations.filter(_.ownSubjectId === id).map(_.viewId))
      address <- addresses if address.id === implementation.addressId
    } yield (implementation, address)
    val res =  implementationsWithAddress.run.filter {
      case (i, _) => implementationValid(i)
    }
    res
  }

  private val graphVariablesForGraphId = Compiled{ id: Column[Int] => graphVariables.filter(_.graphId === id) }
  private val graphMergedSubjectsForGraphId = Compiled{ id: Column[Int] => graphMergedSubjects.filter(_.graphId === id) }
  private val graphMacrosForGraphId = Compiled{ id: Column[Int] => graphMacros.filter(_.graphId === id) }
  private val graphNodesForGraphId = Compiled{ id: Column[Int] => graphNodes.filter(_.graphId === id) }
  private val graphVarMansForGraphId = Compiled{ id: Column[Int] => graphVarMans.filter(_.graphId === id) }
  private val graphEdgesForGraphId = Compiled{ id: Column[Int] => graphEdges.filter(_.graphId === id) }
}
