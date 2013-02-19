package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import scala.slick.lifted.ForeignKeyAction._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity
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
// retrieve user by identity provider and eMail
case class GetUserIdentity(provider: String, eMail: String) extends UserAction
// sets identity params for user and provider
case class SetUserIdentity(userId: Int, provider: String, eMail: String, password: Option[String] = None)  extends UserAction

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
    def uniqueName = index("unique_name", name, unique = true)
  }

  // represents the "user_identities" table in the database
  object UserIdentities extends Table[(Int, String, String, Option[String])]("user_identities") {
    def userId = column[Int]("user_id")
    def provider = column[String]("provider", O.DBType(varchar(32)))
    def eMail = column[String]("e_mail", O.DBType(varchar(255)))
    def password = column[Option[String]]("active", O.DBType(char(60)))
    def * = userId ~ provider ~ eMail ~ password
    // composite primary key
    def pk = primaryKey("pk", (userId, provider))
    def user = foreignKey("user_fk", userId, Users)(_.id, NoAction, Cascade)
    def uniqueEmail = index("unique_email", (provider, eMail), unique = true)
    def includeUser(provider: String, eMail: String) = for {
      i <- UserIdentities.where(e => e.provider === provider && e.eMail === eMail)
      u <- i.user
    } yield (i, u)
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
      // retrieve identity for provider and email
      case GetUserIdentity(provider, eMail) => answer {
    	  UserIdentities.includeUser(provider, eMail).firstOption.map {
    	    case (i, u) => UserIdentity(u, i._2, i._3, i._4)
    	  }
      }
      case SetUserIdentity(userId, provider, eMail, password) => answer {
        UserIdentities.where(i => i.userId === userId && i.provider === provider).delete(session)
        UserIdentities.insert(userId, provider, eMail, password)
      }
      // execute DDL for table "users"
      case InitDatabase => answer { (Users.ddl ++ UserIdentities.ddl).create(session) }
      case DropDatabase => answer { dropIgnoreErrors(UserIdentities.ddl ++ Users.ddl) }
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