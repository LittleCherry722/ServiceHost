package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit RelationAction
* are redirected to RelationPersistenceActor
*/
sealed abstract class RelationAction extends PersistenceAction
/* get all relation entries (Seq[model.Relation]) 
 * empty Seq is returned if no entities where found
*/
case class GetRelation() extends RelationAction
// save relation to db (nothing is returned)
case class SaveRelation(userId: Int, groupId: Int, responsibleId: Int, processId: Int) extends RelationAction
// delete relation from db (nothing is returned)
case class DeleteRelation(userId: Int, groupId: Int, responsibleId: Int, processId: Int) extends RelationAction

/**
 * Handles all database operations for table "relation".
 */
private[persistence] class RelationPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._
  
  // represents the "relation" table in the database
  object Relations extends Table[Relation]("relation") {
    def userId = column[Int]("userID")
    def groupId = column[Int]("groupID")
    def responsibleId = column[Int]("responsibleID")
    def processId = column[Int]("processID")
    // composite primary key
    def pk = primaryKey("pk", (userId, groupId, responsibleId, processId))
    def * = userId ~ groupId ~ responsibleId ~ processId <> (Relation, Relation.unapply _)
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all relations ordered by process id
      case GetRelation() => sender ! Relations.sortBy(_.processId).list
      // save relation to db
      case SaveRelation(userId, groupId, responsibleId, processId) =>
        save(userId, groupId, responsibleId, processId)
      // delete relation
      case DeleteRelation(userId, groupId, responsibleId, processId) =>
        delete(userId, groupId, responsibleId, processId)
      // execute DDL for "relation" table
      case InitDatabase => Relations.ddl.create(session)
    }
  }

  // delete existing relation from db
  private def delete(userId: Int, groupId: Int, responsibleId: Int, processId: Int)(implicit session: Session) = {
    Relations.where(r => r.userId === userId && r.groupId === groupId && r.responsibleId === responsibleId && r.processId === processId).delete(session)
  }

  // delete existing relation from db
  // and insert new entry with given values
  private def save(userId: Int, groupId: Int, responsibleId: Int, processId: Int)(implicit session: Session) = {
    delete(userId, groupId, responsibleId, processId)
    Relations.insert(Relation(userId, groupId, responsibleId, processId))
  }
}