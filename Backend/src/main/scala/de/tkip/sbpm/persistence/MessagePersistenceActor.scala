package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit MessageAction
* are redirected to MessagePersistenceActor
*/
sealed abstract class MessageAction extends PersistenceAction
/* get entry (Option[model.Message]) by id 
* or all entries (Seq[model.Message]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetMessage(id: Option[Int] = None) extends MessageAction
// save message to db, if id is None a new message is created and its id is returned
case class SaveMessage(id: Option[Int] = None, from: Int, to: Int, instanceId: Int, isRead: Boolean, data: String, date: java.sql.Timestamp) extends MessageAction
// delete message with id from db (nothing is returned)
case class DeleteMessage(id: Int) extends MessageAction


private[persistence] class MessagePersistenceActor extends Actor with DatabaseAccess {
  import de.tkip.sbpm.model._
  // import driver loaded according to akka config
  import driver.simple._
  import DBType._

  // represents the "messages" table in the database
  object Messages extends Table[Message]("messages") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def from = column[Int]("from")
    def to = column[Int]("to")
    def instanceId = column[Int]("instanceID")
    def isRead = column[Boolean]("read")
    def data = column[String]("data", O.DBType(blob))
    def date = column[java.sql.Timestamp]("date")
    def * = id.? ~ from ~ to ~ instanceId ~ isRead ~ data ~ date <> (Message, Message.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }

  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all messages ordered by id
      case GetMessage(None) => sender ! Messages.sortBy(_.id).list
      // get message with given id
      case GetMessage(id) => sender ! Messages.where(_.id === id).firstOption
      // create new message
      case SaveMessage(None, from, to, instanceId, isRead, data, date) =>
        sender ! Messages.autoInc.insert(Message(None, from, to, instanceId, isRead, data, date))
      // update existing message
      case SaveMessage(id, from, to, instanceId, isRead, data, date) =>
        Messages.where(_.id === id).update(Message(id, from, to, instanceId, isRead, data, date))
      // delete message with given id
      case DeleteMessage(id) => Messages.where(_.id === id).delete(session)
      // execute DDL for "messages" table
      case InitDatabase => Messages.ddl.create(session)
    }
  }

}