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

package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.User
import de.tkip.sbpm.model.UserIdentity

/**
 * PersistenceActor queries for "Users".
 */
object Users {
  // used to identify all Users queries
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    /**
     * returns all users (Seq[User])
     */
    case object All extends Query
    /**
     * returns all users with its identities (Map[User, Seq[UserIdentity]])
     */
    case object AllWithIdentities extends Query
    /**
     * returns user by id or None if not found (Option[User])
     */
    case class ById(id: Int) extends Query
    /**
     * returns user by id with its identities or None if not found (Option[(User, Seq[UserIdentitiy])])
     */
    case class ByIdWithIdentities(id: Int) extends Query
    /**
     * returns user by name or None if not found (Option[User])
     */
    case class ByName(name: String) extends Query

    /**
     * returns all users connect to a subject by their role (Seq[User])
     */
    case class BySubject(subjectId: String, processInstanceId: Int, processId: Int) extends Query
    
    /**
     *  returns email by id and provider
     */
    case class ByIdProvider(id: Int, provider: String) extends Query
    
    object Identity {
      def apply(provider: String, eMail: String) = ByEMail(provider, eMail)
      /**
       * returns user identity by provider and email or None if not found
       * (Option[UserIdentity])
       */
      case class ByEMail(provider: String, eMail: String) extends Query
      /**
       * returns user identity by provider and id or None if not found
       * (Option[UserIdentity])
       */
      case class ById(provider: String, userId: Int) extends Query
    }
  }

  object Save {
    def apply(user: User*) = Entity(user: _*)
    /**
     * saves all given users
     * if one entity given, returns generated id if
     * entry was created (because id in given user was None)
     * or None if entry was updated (Option[Int])
     * if multiple entities given, Seq[Option[Int]]
     * is returned respectively
     */
    case class Entity(user: User*) extends Query
    /**
     * save identity for a user
     * returns (userId, provider) if create or None if updated
     * (Option[(Int, String)])
     */
    case class Identity(userId: Int, provider: String, eMail: String, password: Option[String] = None) extends Query
  }

  object Delete {
    def apply(user: User) = ById(user.id.get)
    /**
     * deletes entity by id with empty result
     */
    case class ById(id: Int) extends Query
    object Identity {
      def apply(identity: UserIdentity) = ById(identity.user.id.get, identity.provider)
      /**
       * delete user identity by user id and provider with empty result
       */
      case class ById(userId: Int, provider: String) extends Query
    }
  }
}
