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

package de.tkip.sbpm.persistence.schema

import de.tkip.sbpm.persistence.mapping._
import scala.slick.model.ForeignKeyAction._

/**
 * Defines the database schema of UserIdentities.
 * If you want to query UserIdentities database table mix this trait
 * into the actor performing the queries.
 */
trait UserIdentitiesSchema extends UsersSchema {
  // import current slick driver dynamically
  import driver.simple._

  // represents schema if the "user_identities" table in the database
  // using slick's lifted embedding API
  class UserIdentities(tag: Tag) extends SchemaTable[UserIdentity](tag, "user_identities") {
    def userId = column[Int]("user_id")
    def provider = column[String]("provider", DbType.stringIdentifier)
    def eMail = column[String]("e_mail", DbType.eMail)
    def password = column[Option[String]]("password", DbType.bcrypt)
    
    def * = (userId, provider, eMail, password) <> (UserIdentity.tupled, UserIdentity.unapply)

    def pk = primaryKey(pkName, (userId, provider))

    def uniqueEmail =
      index("unq_" + tableName + "_provider_e_mail", (provider, eMail), unique = true)

    def user =
      foreignKey(fkName("users"), userId, users)(_.id, NoAction, Cascade)
  }

  val userIdentities = TableQuery[UserIdentities]
}