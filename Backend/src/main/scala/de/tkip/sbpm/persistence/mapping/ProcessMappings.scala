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

/**
 * Provides conversions from domain model entities
 * to db entities and vice versa for Process entities.
 */
object ProcessMappings {

  /**
   * Convert process and id of active graph to domain model.
   */
  def convert(p: (Process, Option[Int])): domainModel.Process =
    domainModel.Process(p._1.id,
      p._1.interfaceId,
      p._1.publishInterface,
      p._1.name,
      p._1.isCase,
      Some(p._1.startAble),
      p._2)

  /**
   * Convert process option and id of active graph to domain model.
   */
  def convert(p: Option[(Process, Option[Int])]): Option[domainModel.Process] =
    if (p.isDefined) Some(convert(p.get))
    else None

  /**
   * Convert process from domain model to db entity and
   * extracts optional active graph id.
   */
  def convert(p: domainModel.Process): (Process, Option[Int]) =
    (Process(p.id, p.interfaceId, p.publishInterface, p.name, p.isCase, p.startAble.getOrElse(false)), p.activeGraphId)
}