package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/**
 * Handles all database operations for table "process_instance".
 */
private[persistence] class ProcessInstancePersistenceActor extends Actor
  with DatabaseAccess with schema.ProcessInstancesSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.ProcessInstances._

  def toDomainModel(u: mapping.ProcessInstance) =
    convert(u, Persistence.processInstance, Domain.processInstance)

  def toDomainModel(u: Option[mapping.ProcessInstance]) =
    convert(u, Persistence.processInstance, Domain.processInstance)

  def toPersistenceModel(u: ProcessInstance) =
    convert(u, Domain.processInstance, Persistence.processInstance)

  def receive = {
    // get all process instance ordered by id
    case Read.All => answer { implicit session =>
      Query(ProcessInstances).list.map(toDomainModel)
    }
    // get process instance with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(ProcessInstances).where(_.id === id).firstOption)
    }
    // create new process instance
    case Save.Entity(pis @ _*) => answer { implicit session =>
      pis.map {
        case pi @ ProcessInstance(None, _, _, _) => Some(ProcessInstances.autoInc.insert(toPersistenceModel(pi)))
        case pi @ ProcessInstance(id, _, _, _)   => update(id, pi)
      } match {
        case ids if (ids.size == 1) => ids.head
        case ids                    => ids
      }
    }
    // delete process instance with given id
    case Delete.ById(id) => answer { implicit session =>
      ProcessInstances.where(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], pi: ProcessInstance)(implicit session: Session) {
    val res = ProcessInstances.where(_.id === id).update(toPersistenceModel(pi))
    if (res == 0)
      throw new EntityNotFoundException("Process instance with id %d does not exist.", id.get)
    None
  }
}