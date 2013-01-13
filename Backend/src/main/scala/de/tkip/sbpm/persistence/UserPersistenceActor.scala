package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted

/*
* Messages for querying database
* all message classes that inherit UserAction
* are redirected to UserPersistenceActor
*/
sealed abstract class UserAction extends PersistenceAction
/* get user entry (Option[model.User]) by id 
* or all entries (Seq[model.User]) by sending None as id
* None or empty Seq is returned if no entities where found
*/
case class GetUser(id: Option[Int] = None) extends UserAction
// save user to db, if id is None a new process is created and its id is returned
case class SaveUser(id: Option[Int] = None, name: String, isActive: Boolean = true, inputPoolSize: Int = 8) extends UserAction
// delete user with id from db
case class DeleteUser(id: Int) extends UserAction

package model {
// represents a user in the db
  case class User(id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8)
}

/**
 * Handles all database operations for table "users".
 */
private[persistence] class UserPersistenceActor extends Actor with DatabaseAccess {
	import model._
	// import driver loaded according to akka config
  import driver.simple._
  import DBType._
  
  // represents the "users" table in the database
  object Users extends Table[User]("users") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.DBType(varchar(32)))
    def isActive = column[Boolean]("active", O.Default(true))
    def inputPoolSize = column[Int]("inputpoolsize", O.DBType(smallint), O.Default(8))
    def * = id.? ~ name ~ isActive ~ inputPoolSize <> (User, User.unapply _)
    // auto increment method returning generated id
    def autoInc = * returning id
  }
	
  def receive = database.withSession { implicit session => // execute all db operations in a session
    {
      // get all users ordered by id
      case GetUser(None) => sender ! Users.sortBy(_.id).list
      // get user with given id
      case GetUser(id) => sender ! Users.where(_.id === id).firstOption
      // create new user
      case SaveUser(None, name, isActive, inputPoolSize) => 
        sender ! Users.autoInc.insert(User(None, name, isActive, inputPoolSize)) 
      // save existing user
      case SaveUser(id, name, isActive, inputPoolSize) =>
        Users.where(_.id === id).update(User(id, name, isActive, inputPoolSize))
      // delete user with given id
      case DeleteUser(id) => Users.where(_.id === id).delete(session)
      // execute DDL for table "users"
      case InitDatabase => Users.ddl.create(session)
    }
  }

}