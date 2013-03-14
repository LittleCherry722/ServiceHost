package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

private[persistence] class MessagePersistenceActor extends Actor
  with DatabaseAccess with schema.MessagesSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.Messages._

  def toDomainModel(u: mapping.Message) =
    convert(u, Persistence.message, Domain.message)

  def toDomainModel(u: Option[mapping.Message]) =
    convert(u, Persistence.message, Domain.message)

  def toPersistenceModel(u: Message) =
    convert(u, Domain.message, Persistence.message)

  def receive = {
    // get all messages ordered by id
    case Read.All => answer { implicit session =>
      Query(Messages).list.map(toDomainModel)
    }
    // get message with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Messages).where(_.id === id).firstOption)
    }
    // create new message
    case Save.Entity(ms @ _*) => answer { implicit session =>
      ms.map {
        case m @ Message(None, _, _, _, _, _, _) => Some(Messages.autoInc.insert(toPersistenceModel(m)))
        case m @ Message(id, _, _, _, _, _, _) => update(id, m)
      }
    }
    // delete message with given id
    case Delete.ById(id) => answer { session =>
      Messages.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], m: Message) = answer { implicit session =>
    val res = Messages.where(_.id === id).update(toPersistenceModel(m))
    if (res == 0)
      throw new EntityNotFoundException("Message with id %d does not exist.", id.get)
    None
  }
}