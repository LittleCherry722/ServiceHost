package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.{model => M}
import de.tkip.sbpm.persistence.DatabaseAccess._
import scala.slick.driver.JdbcDriver.simple._


/**
 * Created by arne on 26.08.14.
 */
object InterfaceQuery {
  def loadInterfaces(): M.Interface = {
    val id = null
    id
  }

  def loadInterface(id: Int): M.Interface = {
    return null
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

  private def retrieveSubEntities(ids: Seq[Int])(implicit session: Session) = {
    (
      graphConversiationsForGraphIds(ids),
      graphMessagesForGraphIds(ids),
      graphSubjectsForGraphIds(ids),
      graphVariablesForGraphIds(ids),
      graphMacrosForGraphIds(ids),
      graphNodesForGraphIds(ids),
      graphVarMansForGraphIds(ids),
      graphEdgesForGraphIds(ids)
      )
  }

  private val graphConversiationsForGraphId = Compiled{ id: Column[Int] => graphConversations.filter(_.graphId === id) }
  private val graphMessagesForGraphId = Compiled{ id: Column[Int] => graphMessages.filter(_.graphId === id) }
  private val graphSubjectsForGraphId = Compiled{ id: Column[Int] => graphSubjects.filter(_.graphId === id) }
  private val graphVariablesForGraphId = Compiled{ id: Column[Int] => graphVariables.filter(_.graphId === id) }
  private val graphMacrosForGraphId = Compiled{ id: Column[Int] => graphMacros.filter(_.graphId === id) }
  private val graphNodesForGraphId = Compiled{ id: Column[Int] => graphNodes.filter(_.graphId === id) }
  private val graphVarMansForGraphId = Compiled{ id: Column[Int] => graphVarMans.filter(_.graphId === id) }
  private val graphEdgesForGraphId = Compiled{ id: Column[Int] => graphEdges.filter(_.graphId === id) }

  private def graphConversiationsForGraphIds(ids: Seq[Int])(implicit session: Session) = graphConversations.filter(_.graphId inSetBind ids).list
  private def graphMessagesForGraphIds(ids: Seq[Int])(implicit session: Session)       =      graphMessages.filter(_.graphId inSetBind ids).list
  private def graphSubjectsForGraphIds(ids: Seq[Int])(implicit session: Session)       =      graphSubjects.filter(_.graphId inSetBind ids).list
  private def graphVariablesForGraphIds(ids: Seq[Int])(implicit session: Session)      =     graphVariables.filter(_.graphId inSetBind ids).list
  private def graphMacrosForGraphIds(ids: Seq[Int])(implicit session: Session)         =        graphMacros.filter(_.graphId inSetBind ids).list
  private def graphNodesForGraphIds(ids: Seq[Int])(implicit session: Session)          =         graphNodes.filter(_.graphId inSetBind ids).list
  private def graphVarMansForGraphIds(ids: Seq[Int])(implicit session: Session)        =       graphVarMans.filter(_.graphId inSetBind ids).list
  private def graphEdgesForGraphIds(ids: Seq[Int])(implicit session: Session)          =         graphEdges.filter(_.graphId inSetBind ids).list
}