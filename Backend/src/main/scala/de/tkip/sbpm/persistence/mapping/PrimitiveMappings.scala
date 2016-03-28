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

package de.tkip.sbpm.persistence.mapping

import shapeless._
import de.tkip.sbpm.{ model => domainModel }

/**
 * Provides domain -> persistence model mappings for entities
 * that are structural equivalent.
 * Converts domain model entities to db entities and vice versa.
 * Simply copies values from one case class to another using
 * shapeless HLists.
 */
object PrimitiveMappings {

  /**
   * Conversions for persistence models.
   */
  object Persistence {
    val user = Iso.hlist(User.apply _, User.unapply _)
    val group = Iso.hlist(Group.apply _, Group.unapply _)
    val role = Iso.hlist(Role.apply _, Role.unapply _)
    val processInstance = Iso.hlist(ProcessInstance.apply _, ProcessInstance.unapply _)
    val groupRole = Iso.hlist(GroupRole.apply _, GroupRole.unapply _)
    val groupUser = Iso.hlist(GroupUser.apply _, GroupUser.unapply _)
    val message = Iso.hlist(Message.apply _, Message.unapply _)
    val configuration = Iso.hlist(Configuration.apply _, Configuration.unapply _)
  }

  /**
   * Conversions for domain models.
   */
  object Domain {
    val user = Iso.hlist(domainModel.User.apply _, domainModel.User.unapply _)
    val group = Iso.hlist(domainModel.Group.apply _, domainModel.Group.unapply _)
    val role = Iso.hlist(domainModel.Role.apply _, domainModel.Role.unapply _)
    val processInstance = Iso.hlist(domainModel.ProcessInstance.apply _, domainModel.ProcessInstance.unapply _)
    val groupRole = Iso.hlist(domainModel.GroupRole.apply _, domainModel.GroupRole.unapply _)
    val groupUser = Iso.hlist(domainModel.GroupUser.apply _, domainModel.GroupUser.unapply _)
    val message = Iso.hlist(domainModel.UserToUserMessage.apply _, domainModel.UserToUserMessage.unapply _)
    val configuration = Iso.hlist(domainModel.Configuration.apply _, domainModel.Configuration.unapply _)
  }

  /**
   * Convert from one model to another.
   */
  def convert[A, B, L <: HList](a: A, fromIso: Iso[A, L], toIso: Iso[B, L]): B =
    toIso.from(fromIso.to(a))

  /**
   * Convert from one model to another, wrapped in an Option.
   */
  def convert[A, B, L <: HList](a: Option[A], fromIso: Iso[A, L], toIso: Iso[B, L]): Option[B] =
    a match {
      case None     => None
      case Some(aa) => Some(convert(aa, fromIso, toIso))
    }

}