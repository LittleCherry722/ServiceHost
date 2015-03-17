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
import query.ProcessInstances._

/**
 * Handles all database operations for table "process_instances".
 */
private[persistence] class ProcessInstancePersistenceActor extends InstrumentedActor
  with DatabaseAccess with schema.ProcessInstancesSchema {
  // import current slick driver dynamically
  import driver.simple._

  // methods to convert internal persistence models to
  // application wide domain models and vice versa
  def toDomainModel(u: mapping.ProcessInstance) =
    convert(u, Persistence.processInstance, Domain.processInstance)

  def toDomainModel(u: Option[mapping.ProcessInstance]) =
    convert(u, Persistence.processInstance, Domain.processInstance)

  def toPersistenceModel(u: ProcessInstance) =
    convert(u, Domain.processInstance, Persistence.processInstance)

  def wrappedReceive = {
    // get all process instances
    case Read.All => answer { implicit session =>
      processInstances.list.map(toDomainModel)
    }
    // get process instance with given id
    case Read.ById(id) => answer { implicit session =>
      toDomainModel(processInstances.filter(_.id === id).firstOption)
    }

    // create or update process instance
    case Save.Entity(pis @ _*) => answer { implicit session =>
      pis.map {
        // insert if id is None
        case pi @ ProcessInstance(None, _, _, _) =>
          Some((processInstances returning processInstances.map(_.id)) += toPersistenceModel(pi))
        // otherwise update existing
        case pi @ ProcessInstance(id, _, _, _)   => update(id, pi)
      } match {
        // only one process instance was given, return it's id
        case ids if (ids.size == 1) => ids.head
        // more process instances were given return all ids
        case ids                    => ids
      }
    }
    // delete process instance with given id
    case Delete.ById(id) => answer { implicit session =>
      processInstances.filter(_.id === id).delete(session)
    }
  }

  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], pi: ProcessInstance)(implicit session: Session) {
    val res = processInstances.filter(_.id === id).update(toPersistenceModel(pi))
    if (res == 0)
      throw new EntityNotFoundException("Process instance with id %d does not exist.", id.get)
    None
  }
}
