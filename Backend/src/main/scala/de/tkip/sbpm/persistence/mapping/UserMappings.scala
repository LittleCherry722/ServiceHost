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

import de.tkip.sbpm.{ model => domainModel }
import PrimitiveMappings._

/**
 * Provides conversions from domain model entities
 * to db entities and vice versa for User entities.
 */
object UserMappings {
  
  /**
   * Convert user identity and user db entities to
   * merged domain model entitiy UserIdentity.
   */
  def convert(i: UserIdentity, u: User): domainModel.UserIdentity =
    domainModel.UserIdentity(
      PrimitiveMappings.convert(u, Persistence.user, Domain.user),
      i.provider,
      i.eMail,
      i.password)
}