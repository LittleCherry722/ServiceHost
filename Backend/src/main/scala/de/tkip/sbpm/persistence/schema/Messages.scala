package de.tkip.sbpm.persistence.schema
import de.tkip.sbpm.persistence.mapping._
import scala.slick.lifted.ForeignKeyAction._

trait MessagesSchema extends ProcessInstancesSchema with UsersSchema {
  import driver.simple._

  object Messages extends SchemaTable[Message]("messages") {
    def id = autoIncIdCol[Int]
    def fromUserId = column[Int]("from_user_id")
    def toUserId = column[Int]("to_user_id")
    def processInstanceId = column[Int]("process_instance_id")
    def isRead = column[Boolean]("read")
    def data = column[String]("data", DbType.blob)
    def date = column[java.sql.Timestamp]("date")

    def * = id.? ~ fromUserId ~ toUserId ~ processInstanceId ~
      isRead ~ data ~ date <> (Message, Message.unapply _)
    def autoInc = * returning id

    def fromUser =
      foreignKey(fkName("users_from"), fromUserId, Users)(_.id)
    def toUser =
      foreignKey(fkName("users_to"), toUserId, Users)(_.id)
    def processInstance =
      foreignKey(fkName("process_instances"), processInstanceId, ProcessInstances)(_.id, NoAction, Cascade)
  }

}