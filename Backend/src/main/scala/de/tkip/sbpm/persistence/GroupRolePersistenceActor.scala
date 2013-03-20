package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

/**
 * Handles all DB operations for table "groups_roles".
 */
private[persistence] class GroupRolePersistenceActor extends Actor
  with DatabaseAccess with schema.GroupsRolesSchema {
  import driver.simple._
  import query.GroupsRoles._
  import mapping.PrimitiveMappings._

  def toDomainModel(u: mapping.GroupRole) =
    convert(u, Persistence.groupRole, Domain.groupRole)

  def toDomainModel(u: Option[mapping.GroupRole]) =
    convert(u, Persistence.groupRole, Domain.groupRole)

  def toPersistenceModel(u: GroupRole) =
    convert(u, Domain.groupRole, Persistence.groupRole)

  def receive = {
    // get all group -> role mappings ordered by group id
    case Read.All => answer { implicit session =>
      Query(GroupsRoles).list.map(toDomainModel)
    }
    // get all group -> role mappings for a role
    case Read.ByRoleId(roleId) => answer { implicit session =>
      Query(GroupsRoles).where(_.roleId === roleId).list.map(toDomainModel)
    }
    // get all group -> role mappings for a group
    case Read.ByGroupId(groupId) => answer { implicit session =>
      Query(GroupsRoles).where(_.groupId === groupId).sortBy(_.roleId).list.map(toDomainModel)
    }
    // get group -> role mapping
    case Read.ById(groupId, roleId) => answer { implicit session =>
      toDomainModel(Query(GroupsRoles).where(e => e.groupId === groupId && (e.roleId === roleId)).firstOption)
    }
    // save group -> role mapping
    case Save.Entity(grs @ _*) => answer { implicit session =>
      grs.map(save) match {
        case ids if (ids.size == 1) => ids.head
        case ids                    => ids
      }
    }
    // delete group -> role mapping
    case Delete.ById(groupId, roleId) => answer { implicit session =>
      delete(groupId, roleId)
    }
    case Delete.ByRoleId(roleId) => answer { implicit session =>
      GroupsRoles.where(_.roleId === roleId).delete
    }
    case Delete.ByGroupId(groupId) => answer { implicit session =>
      GroupsRoles.where(_.groupId === groupId).delete
    }
  }

  // delete existing entry with given group and role id 
  // and insert new record with given values
  private def save(gr: GroupRole)(implicit session: Session) = {
    val res = delete(gr.groupId, gr.roleId)
    GroupsRoles.insert(toPersistenceModel(gr))
    if (res == 0)
      Some((gr.groupId, gr.roleId))
    else
      None
  }

  // delete existing entry with given group and role id
  private def delete(groupId: Int, roleId: Int)(implicit session: Session) = {
    GroupsRoles.where(e => e.groupId === groupId && (e.roleId === roleId)).delete(session)
  }

}