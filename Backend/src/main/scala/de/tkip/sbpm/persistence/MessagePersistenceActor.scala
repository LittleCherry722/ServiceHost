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
import de.tkip.sbpm.model._
import mapping.PrimitiveMappings._
import query.Messages._

/**
 * Handle all db operation for table "messages".
 */
private[persistence] class MessagePersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.MessagesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.Message) =
    convert(u, Persistence.message, Domain.message)

  def toDomainModel(u: Option[mapping.Message]) =
    convert(u, Persistence.message, Domain.message)

  def toPersistenceModel(u: UserToUserMessage) =
    convert(u, Domain.message, Persistence.message)

  def wrappedReceive = {
    // get all messages
    case Read.All => answer { implicit session =>
      messages.list.map(toDomainModel)
    }

    case Read.WithTarget(userID) => answer { implicit session =>
      messages.filter(_.toUserId === userID).list.map(toDomainModel)
    }
    case Read.WithSource(userID) => answer { implicit session =>
      messages.filter(_.fromUserId === userID).list.map(toDomainModel)
    }
    // get message with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(messages.filter(_.id === id).firstOption)
    }
    // create or update message
    case Save.Entity(ms @ _*) => answer { implicit session =>
      ms.map {
        // insert if id is None
        case m @ UserToUserMessage(None, _, _, _, _, _, _) => Some((messages returning messages.map(_.id)) += toPersistenceModel(m))
        // otherwise update
        case m @ UserToUserMessage(id, _, _, _, _, _, _)   => update(id, m)
      } match {
        // only one message was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more messages were given return all ids
        case ids                    => ids
      }
    }
    // delete message with given id
    case Delete.ById(id) => answer { implicit session =>
      messages.filter(_.id === id).delete
    }
  }

  // update entity or throw exception if it does not exist
  def update(id: Option[Int], m: UserToUserMessage) = answer { implicit session =>
    val res = messages.filter(_.id === id).update(toPersistenceModel(m))
    if (res == 0)
      throw new EntityNotFoundException("Message with id %d does not exist.", id.get)
    None
  }
}
