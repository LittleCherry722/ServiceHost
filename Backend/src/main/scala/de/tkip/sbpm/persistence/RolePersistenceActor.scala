package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/**
 * Handles all database operations for table "roles".
 */
private[persistence] class RolePersistenceActor extends Actor
  with DatabaseAccess with schema.RolesSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.Roles._

  def toDomainModel(u: mapping.Role) =
    convert(u, Persistence.role, Domain.role)

  def toDomainModel(u: Option[mapping.Role]) =
    convert(u, Persistence.role, Domain.role)

  def toPersistenceModel(u: Role) =
    convert(u, Domain.role, Persistence.role)

  def receive = {
    // get all roles ordered by id
    case Read.All => answer { implicit session =>
      Query(Roles).list.map(toDomainModel)
    }
    // get role with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Roles).where(_.id === id).firstOption)
    }
    // get role with given name
    case Read.ByName(name) => answer { implicit session =>
      toDomainModel(Query(Roles).where(_.name === name).firstOption)
    }
    // save role
    case Save.Entity(rs @ _*) => answer { implicit session =>
      rs.map {
        case r @ Role(None, _, _) => Some(Roles.autoInc.insert(toPersistenceModel(r)))
        case r @ Role(id, _, _)   => update(id, r)
      } match {
        case ids if (ids.size == 1) => ids.head
        case ids                    => ids
      }
    }
    // delete role with given id
    case Delete.ById(id) => answer { implicit session =>
      Roles.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], r: Role) = answer { implicit session =>
    val res = Roles.where(_.id === id).update(toPersistenceModel(r))
    if (res == 0)
      throw new EntityNotFoundException("Role with id %d does not exist.", id.get)
    None
  }

}