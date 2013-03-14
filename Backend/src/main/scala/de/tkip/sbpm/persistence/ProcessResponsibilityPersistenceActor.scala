package de.tkip.sbpm.persistence

import akka.actor.Actor
import de.tkip.sbpm.model._

/**
 * Handles all database operations for table "relation".
 */
private[persistence] class ProcessResponsibilityPersistenceActor extends Actor
  with DatabaseAccess with schema.ProcessResponsibilitiesSchema {
  import driver.simple._
  import mapping.PrimitiveMappings._
  import query.ProcessResponsibilities._

  def toDomainModel(u: mapping.ProcessResponsibility) =
    convert(u, Persistence.processResponsibility, Domain.processResponsibility)

  def toDomainModel(u: Option[mapping.ProcessResponsibility]) =
    convert(u, Persistence.processResponsibility, Domain.processResponsibility)

  def toPersistenceModel(u: ProcessResponsibility) =
    convert(u, Domain.processResponsibility, Persistence.processResponsibility)

  def receive = {
    case Read.All => answer { implicit session =>
      Query(ProcessResponsibilities).list.map(toDomainModel)
    }
    case Read.ById(processId, roleId, userId) => answer { implicit session =>
      toDomainModel(Query(ProcessResponsibilities).where(r => r.userId === userId && r.roleId === roleId && r.processId === processId).firstOption)
    }
    // save relation to db
    case Save.Entity(rs @ _*) => answer { implicit session =>
      rs.map(save)
    }
    // delete relation
    case Delete.ById(processId, roleId, userId) => answer { implicit session =>
      delete(processId, roleId, userId)
    }
  }

  // delete existing relation from db
  private def delete(processId: Int, roleId: Int, userId: Int)(implicit session: Session) = {
    ProcessResponsibilities.where(r => r.userId === userId && r.roleId === roleId && r.processId === processId).delete(session)
  }

  // delete existing relation from db
  // and insert new entry with given values
  private def save(r: ProcessResponsibility)(implicit session: Session) = {
    val res = delete(r.processId, r.roleId, r.userId)
    ProcessResponsibilities.insert(toPersistenceModel(r))
    if (res == 0)
      Some((r.processId, r.roleId, r.userId))
    else
      None
  }
}