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

import akka.actor.Actor
import akka.actor.Props
import scala.slick.lifted
import de.tkip.sbpm.model._
import mapping.PrimitiveMappings._
import query.Messages._

/**
 * Handle all db operation for table "messages".
 */
private[persistence] class MessagePersistenceActor extends Actor
  with DatabaseAccess with schema.MessagesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.Message) =
    convert(u, Persistence.message, Domain.message)

  def toDomainModel(u: Option[mapping.Message]) =
    convert(u, Persistence.message, Domain.message)

  def toPersistenceModel(u: Message) =
    convert(u, Domain.message, Persistence.message)

  def receive = {
    // get all messages
    case Read.All => answer { implicit session =>
      Query(Messages).list.map(toDomainModel)
    }
    // get message with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(Query(Messages).where(_.id === id).firstOption)
    }
    // create or update message
    case Save.Entity(ms @ _*) => answer { implicit session =>
      ms.map {
        // insert if id is None
        case m @ Message(None, _, _, _, _, _, _) => Some(Messages.autoInc.insert(toPersistenceModel(m)))
        // otherwise update
        case m @ Message(id, _, _, _, _, _, _)   => update(id, m)
      } match {
         // only one message was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more messages were given return all ids
        case ids                    => ids
      }
    }
    // delete message with given id
    case Delete.ById(id) => answer { implicit session =>
      Messages.where(_.id === id).delete
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