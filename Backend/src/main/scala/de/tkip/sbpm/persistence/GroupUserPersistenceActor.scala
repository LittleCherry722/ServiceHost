package de.tkip.sbpm.persistence
import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._

private[persistence] class GroupUserPersistenceActor extends Actor
  with DatabaseAccess with schema.GroupsUsersSchema {
  import driver.simple._
  import query.GroupsUsers._
  import mapping.PrimitiveMappings._

  def toDomainModel(u: mapping.GroupUser) =
    convert(u, Persistence.groupUser, Domain.groupUser)

  def toDomainModel(u: Option[mapping.GroupUser]) =
    convert(u, Persistence.groupUser, Domain.groupUser)

  def toPersistenceModel(u: GroupUser) =
    convert(u, Domain.groupUser, Persistence.groupUser)

  def receive = {
    // get all group -> user mappings ordered by group id
    case Read.All => answer { implicit session =>
      Query(GroupsUsers).list.map(toDomainModel)
    }
    // get all group -> user mappings for a user
    case Read.ByUserId(userId) => answer { implicit session =>
      Query(GroupsUsers).where(_.userId === userId).sortBy(_.groupId).list.map(toDomainModel)
    }
    // get all group -> user mappings for a group
    case Read.ByGroupId(groupId) => answer { implicit session =>
      Query(GroupsUsers).where(_.groupId === groupId).sortBy(_.userId).list.map(toDomainModel)
    }
    // get group -> user mapping
    case Read.ById(groupId, userId) => answer { implicit session =>
      toDomainModel(Query(GroupsUsers).where(e => e.groupId === groupId && (e.userId === userId)).firstOption)
    }
    // save group -> user mapping
    case Save.Entity(gus @ _*) => answer { implicit session =>
      gus.map(save)
    }
    // delete group -> user mapping
    case Delete.ById(groupId, userId) => answer { implicit session =>
      delete(groupId, userId)
    }
    case Delete.ByUserId(userId) => answer { implicit session =>
      GroupsUsers.where(_.userId === userId).delete
    }
    case Delete.ByGroupId(groupId) => answer { implicit session =>
      GroupsUsers.where(_.groupId === groupId).delete
    }
  }

  // delete existing exntry with given group an user id 
  // and insert new record with given values
  private def save(gu: GroupUser)(implicit session: Session) = {
    val res = delete(gu.groupId, gu.userId)
    GroupsUsers.insert(toPersistenceModel(gu))
    if (res == 0)
      Some((gu.groupId, gu.userId))
    else
      None
  }

  // delete existing entry with given group an user id 
  private def delete(groupId: Int, userId: Int)(implicit session: Session) = {
    GroupsUsers.where(e => e.groupId === groupId && (e.userId === userId)).delete(session)
  }

}