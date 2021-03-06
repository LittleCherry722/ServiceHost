/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.persistence

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor.Props
import scala.slick.lifted
import scala.slick.model.ForeignKeyAction._
import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity
import akka.event.Logging
import mapping.PrimitiveMappings._
import query.Users._

/**
 * Handles all database operations for table "users".
 */
private[persistence] class UserPersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.UserIdentitiesSchema with schema.GroupsUsersSchema
  with schema.GroupsSchema with schema.GroupsRolesSchema with schema.RolesSchema
  with schema.GraphSubjectsSchema with schema.ProcessInstancesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.User) =
    convert(u, Persistence.user, Domain.user)

  def toDomainModel(u: Option[mapping.User]) =
    convert(u, Persistence.user, Domain.user)

  def toPersistenceModel(u: User) =
    convert(u, Domain.user, Persistence.user)

  def wrappedReceive = {
    // get all users
    case Read.All => answerProcessed { implicit session =>
      users.list
    }(_.map(toDomainModel))
    // get user with given id
    case Read.ById(id) => answerOptionProcessed { implicit session =>
      users.filter(_.id === id).firstOption
    }(toDomainModel)
    // get users and all linked identities as Map[User, Seq[UserUdentity]]
    case Read.AllWithIdentities => answerProcessed { implicit session =>
      (for {
        // left join user with identities table
        (u, i) <- users.leftJoin(userIdentities).on(_.id === _.userId)
      } yield (u, i.provider.?, i.eMail.?, i.password)).list
    }(_.groupBy(e => toDomainModel(e._1)).mapValues(_.filter(_._2.isDefined).map { e =>
      // convert to domain model
      UserIdentity(toDomainModel(e._1), e._2.get, e._3.get, e._4)
    }))
    // get user with given id and all linked entities
    case Read.ByIdWithIdentities(id) => answer { implicit session =>
      val query = for {
        (u, i) <- users.filter(_.id === id).leftJoin(userIdentities).on(_.id === _.userId)
      } yield (u, i.provider.?, i.eMail.?, i.password)
      // create option tuple Option[(User, List[UserIdentity])]
      query.list.groupBy(e => toDomainModel(e._1)).mapValues(_.filter(_._2.isDefined).map { e =>
        // convert to domain model
        UserIdentity(toDomainModel(e._1), e._2.get, e._3.get, e._4)
      }).toSeq.headOption
    }
    // get user with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session =>
      users.filter(_.name === name).firstOption
    }(toDomainModel)
    // create or update user
    case Save.Entity(us @ _*) => answer { implicit session =>
      us.map {
        // id is None -> insert
        case u @ User(None, _, _, _, _) => Some(users returning users.map(_.id) += toPersistenceModel(u))
        // id given -> update
        case u @ User(id, _, _, _, _)   => update(id, u)

      } match {
        // only one user was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more users were given return all ids
        case ids                    => ids
      }
    }
    // delete user with given id
    case Delete.ById(id) => answer { implicit session =>
      users.filter(_.id === id).delete
    }
    // retrieve identity for provider and email
    case Read.Identity.ByEMail(provider, eMail) => answerOptionProcessed { implicit session =>
      // read identity and corresponding user
      val q = for {
        i <- userIdentities if (i.eMail === eMail && i.provider === provider)
        u <- i.user
      } yield (i, u)
      q.firstOption
    }(t => mapping.UserMappings.convert(t._1, t._2))

    case Read.ByIdProvider(id, provider) => answerOptionProcessed { implicit session =>
      //
      val q = for {
        i <- userIdentities if (i.userId === id && i.provider === provider)
      } yield (i.eMail, i.userId)
      q.firstOption
    }(t => t._1)

    // retrieve identity for provider and userId
    case Read.Identity.ById(provider, userId) => answerOptionProcessed { implicit session =>
      // read identity and corresponding user
      val q = for {
        i <- userIdentities if (i.userId === userId && i.provider === provider)
        u <- i.user
      } yield (i, u)
      q.firstOption
    }(t => mapping.UserMappings.convert(t._1, t._2))
    // save identity to db
    case Save.Identity(userId, provider, eMail, password) => answer { implicit session =>
      val res = deleteIdentity(userId, provider)
      userIdentities += mapping.UserIdentity(userId, provider, eMail, password)
      // return id if created
      if (res == 0)
        Some((userId, provider))
      else
        None
    }
    // delete user identity
    case Delete.Identity.ById(userId, provider) => answer { implicit session =>
      deleteIdentity(userId, provider)
    }
    // retrieve users connected to a subject by their role
    case Read.BySubject(subjectId, processInstanceId, processId) => answerProcessed { implicit session =>
      readBySubject(subjectId, processInstanceId, processId)
    }(_.map(toDomainModel))
  }

  // delete user identity
  private def deleteIdentity(userId: Int, provider: String)(implicit session: Session) = {
    userIdentities.where(i => i.userId === userId && i.provider === provider).delete
  }

  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], u: User) = answer { implicit session =>
    val res = users.where(_.id === id).update(toPersistenceModel(u))
    if (res == 0)
      throw new EntityNotFoundException("User with id %d does not exist.", id.get)
    None
  }

  private def readBySubject(subjectId: String, processInstanceId: Int, processId: Int)(implicit session: Session) = {
    val q = for {
      user <- users
      gu <- groupsUsers if gu.userId === user.id
      group <- gu.group
      gr <- groupsRoles if gr.groupId === group.id
      role <- gr.role
      graphSubject <- graphSubjects if (graphSubject.roleId === role.id && graphSubject.id === subjectId)
      graph <- graphSubject.graph if graph.processId === processId
      processInstance <- processInstances if processInstance.id === processInstanceId &&
        processInstance.processId === processId &&
        processInstance.graphId === graph.id
    } yield user
    q.sortBy(_.name).list.distinct
  }
}
