package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

private[persistence] class GroupPersistenceActor extends Actor
  with DatabaseAccess with schema.GroupsSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.Groups._

  def toDomainModel(u: mapping.Group) =
    convert(u, Persistence.group, Domain.group)

  def toDomainModel(u: Option[mapping.Group]) =
    convert(u, Persistence.group, Domain.group)

  def toPersistenceModel(u: Group) =
    convert(u, Domain.group, Persistence.group)

  def receive = {
    // get all groups ordered by id
    case Read.All => answer { implicit session =>
      Query(Groups).list.map(toDomainModel)
    }
    // get group with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Groups).where(_.id === id).firstOption)
    }
    // get group with given name
    case Read.ByName(name) => answer { implicit session =>
      toDomainModel(Query(Groups).where(_.name === name).firstOption)
    }
    // create new group
    case Save.Entity(gs @ _*) => answer { implicit session =>
      gs.map {
        case g @ Group(None, _, _) => Some(Groups.autoInc.insert(toPersistenceModel(g)))
        case g @ Group(id, _, _) => update(id, g)
      }
    }
    // delete group with given id
    case Delete.ById(id) => answer { implicit session =>
      Groups.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], g: Group)(implicit session: Session) = {
    val res = Groups.where(_.id === id).update(toPersistenceModel(g))
    if (res == 0)
      throw new EntityNotFoundException("Group with id %d does not exist.", id.get)
    None
  }

}