package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model.User
import akka.event.Logging

/*
* Messages for querying database
* all message classes that inherit UserAction
* are redirected to UserPersistenceActor
*/
sealed abstract class UserAction extends PersistenceAction
/* get user entry (Option[model.User]) by id, name
* or all entries (Seq[model.User]) by sending None as id and name
* None or empty Seq is returned if no entities where found
*/
case class GetUser(id: Option[Int] = None, name: Option[String] = None) extends UserAction
// save user to db, if id is None a new process is created and its id is returned
case class SaveUser(user: User) extends UserAction
// delete user with id from db
case class DeleteUser(id: Int) extends UserAction

/**
 * Handles all database operations for table "users".
 */
private[persistence] class UserPersistenceActor extends Actor with DatabaseAccess {

  import driver.simple._
  import DBType._
  import de.tkip.sbpm.model._

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
      case GetUser(None, None) => answer { Users.sortBy(_.id).list }
      // get user with given id
      case GetUser(id, None) => answer { Users.where(_.id === id).firstOption }
      // get user with given name
      case GetUser(None, name) => answer { Users.where(_.name === name).firstOption }
      // create new user
      case SaveUser(u @ User(None, _, _, _)) =>
        answer { Some(Users.autoInc.insert(u)) }
      // save existing user
      case SaveUser(u @ User(id, _, _, _)) => update(id, u)
      // delete user with given id
      case DeleteUser(id) => answer { Users.where(_.id === id).delete(session) }
      // execute DDL for table "users"
      case InitDatabase => answer { Users.ddl.create(session) }
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], u: User)(implicit session: Session) = answer {
    val res = Users.where(_.id === id).update(u)
    if (res == 0)
      throw new EntityNotFoundException("User with id %d does not exist.", id.get)
    None
  }
}