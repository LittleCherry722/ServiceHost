package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import scala.slick.lifted.ForeignKeyAction._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity
import akka.event.Logging

/**
 * Handles all database operations for table "users".
 */
private[persistence] class UserPersistenceActor extends Actor
  with DatabaseAccess with schema.UserIdentitiesSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.Users._

  def toDomainModel(u: mapping.User) =
    convert(u, Persistence.user, Domain.user)

  def toDomainModel(u: Option[mapping.User]) =
    convert(u, Persistence.user, Domain.user)

  def toPersistenceModel(u: User) =
    convert(u, Domain.user, Persistence.user)

  def receive = {
    // get all users ordered by id
    case Read.All => answerProcessed { implicit session: Session =>
      Query(Users).list
    }(_.map(toDomainModel))
    // get user with given id
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      Query(Users).where(_.id === id).firstOption
    }(toDomainModel)
    case Read.AllWithIdentities => answerProcessed { implicit session: Session =>
      (for {
        (u, i) <- Query(Users).leftJoin(Query(UserIdentities)).on(_.id === _.userId)
      } yield (u, i.provider.?, i.eMail.?, i.password)).list
    }(_.groupBy(e => toDomainModel(e._1)).mapValues(_.filter(_._2.isDefined).map { e =>
      UserIdentity(toDomainModel(e._1), e._2.get, e._3.get, e._4)
    }))
    // get user with given id
    case Read.ByIdWithIdentities(id) => answer { implicit session: Session =>
      val query = for {
        (u, i) <- Query(Users).where(_.id === id).leftJoin(Query(UserIdentities)).on(_.id === _.userId)
      } yield (u, i.provider.?, i.eMail.?, i.password)
      query.list.groupBy(e => toDomainModel(e._1)).mapValues(_.filter(_._2.isDefined).map { e =>
        UserIdentity(toDomainModel(e._1), e._2.get, e._3.get, e._4)
      }).toSeq.headOption
    }
    // get user with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session: Session =>
      Query(Users).where(_.name === name).firstOption
    }(toDomainModel)
    // create new user
    case Save.Entity(us @ _*) => answer { implicit session =>
      us.map {
        case u @ User(None, _, _, _) => Some(Users.autoInc.insert(toPersistenceModel(u)))
        case u @ User(id, _, _, _)   => update(id, u)
      } match {
        case ids if (ids.size == 1) => ids.head
        case ids                    => ids
      }
    }
    // delete user with given id
    case Delete.ById(id) => answer { implicit session =>
      Users.where(_.id === id).delete
    }
    // retrieve identity for provider and email
    case Read.Identity.ByEMail(provider, eMail) => answerOptionProcessed { implicit session: Session =>
      val q = for {
        i <- UserIdentities if (i.eMail === eMail && i.provider === provider)
        u <- i.user
      } yield (i, u)
      q.firstOption
    }(t => mapping.UserMappings.convert(t._1, t._2))
    // retrieve identity for provider and userId
    case Read.Identity.ById(provider, userId) => answerOptionProcessed { implicit session: Session =>
      val q = for {
        i <- UserIdentities if (i.userId === userId && i.provider === provider)
        u <- i.user
      } yield (i, u)
      q.firstOption
    }(t => mapping.UserMappings.convert(t._1, t._2))
    case Save.Identity(userId, provider, eMail, password) => answer { implicit session =>
      UserIdentities.where(i => i.userId === userId && i.provider === provider).delete
      UserIdentities.insert(mapping.UserIdentity(userId, provider, eMail, password))
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], u: User) = answer { implicit session =>
    val res = Users.where(_.id === id).update(toPersistenceModel(u))
    if (res == 0)
      throw new EntityNotFoundException("User with id %d does not exist.", id.get)
    None
  }
}