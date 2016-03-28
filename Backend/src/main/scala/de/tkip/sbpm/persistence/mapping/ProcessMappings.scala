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

import de.tkip.sbpm.{model => domainModel}

/**
 * Provides conversions from domain model entities
 * to db entities and vice versa for Process entities.
 */
object ProcessMappings {

  /**
   * Convert process and id of active graph to domain model.
   */

  def convert(pt: (Seq[VerificationError], Process, Option[Int], Map[Int, Map[String, String]], Map[Int, Map[String, String]])): domainModel.Process = {
    val (ves, p, graphId, subjectMap, messageMap) = pt
    domainModel.Process(
      id = p.id,
      interfaceId = p.interfaceId,
      verificationErrors = ves.map(_.message),
      publishInterface = p.publishInterface,
      name = p.name,
      subjectMap = subjectMap,
      messageMap = messageMap,
      implementationIds = p.implementationIds,
      isCase = p.isCase,
      startAble = Some(p.startAble),
      activeGraphId = graphId)
  }


  /**
   * Convert process option and id of active graph to domain model.
   */
  def convert(pOption: Option[(Seq[VerificationError],Process, Option[Int], Map[Int, Map[String, String]], Map[Int, Map[String, String]])]): Option[domainModel.Process] =
    pOption.map(convert)

  /**
   * Convert process from domain model to db entity and
   * extracts optional active graph id.
   */
  def convert(p: domainModel.Process): (Process, Option[Int], (Int) => (Seq[ProcessSubjectMapping], Seq[ProcessMessageMapping], Seq[VerificationError])) = {
    val mappings = (pId: Int) => {
      val subjectMap = p.subjectMap.flatMap {
        case (viewId, sMap) => sMap.map {
          case (from, to) => ProcessSubjectMapping(pId, viewId, from, to)
        }
      }.toSeq
      val messageMap = p.messageMap.flatMap {
        case (viewId, mMap) => mMap.map {
          case (from, to) => ProcessMessageMapping(pId, viewId, from, to)
        }
      }.toSeq
      val vErrors = p.verificationErrors.map(m => VerificationError(None, pId, m)).toSeq
      (subjectMap, messageMap, vErrors)
    }
    val process = Process(id = p.id,
      interfaceId = p.interfaceId,
      publishInterface = p.publishInterface,
      name = p.name,
      isCase = p.isCase,
      startAble = p.startAble.getOrElse(false),
      implementationIds = p.implementationIds)
    (process, p.activeGraphId, mappings)
  }
}