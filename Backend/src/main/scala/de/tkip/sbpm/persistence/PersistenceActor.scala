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

import scala.reflect.ClassTag
import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm._
import java.util.UUID
import akka.event.Logging

/**
 * Handles all DB operations using slick (http://slick.typesafe.com/).
 * Redirects table specific actions to sub actors.
 */
class PersistenceActor extends InstrumentedActor with ActorLogging {
  private val processInspectActor = context.actorOf(Props[ProcessInspectActor],"ProcessInspectActor____"+UUID.randomUUID().toString())
  private lazy val changeActor = ActorLocator.changeActor

  def wrappedReceive = {
    // redirect all request to responsible sub actors
    case q: Users.Query            => forwardTo[UserPersistenceActor](q)
    case q: Groups.Query           => forwardTo[GroupPersistenceActor](q)
    case q: Roles.Query            => forwardTo[RolePersistenceActor](q)
    case q: GroupsRoles.Query      => forwardTo[GroupRolePersistenceActor](q)
    case q: GroupsUsers.Query      => forwardTo[GroupUserPersistenceActor](q)
    case q: Messages.Query         => forwardTo[MessagePersistenceActor](q)
    case q: ProcessInstances.Query => forwardTo[ProcessInstancePersistenceActor](q)
    case q: Processes.Query        => {
      processInspectActor forward q
      println("!!!!!!!!!!!  the query is: "+q)
//    changeActor forward q
    }
    case q: Graphs.Query           => forwardTo[GraphPersistenceActor](q)
    case q: Schema.Query           => forwardTo[SchemaActor](q)
  }

  /**
   * Forwards a query to the specified Actor.
   * The actor is automatically stopped after processing the
   * query using PoisonPill message.
   */
  private def forwardTo[A <: Actor: ClassTag](query: BaseQuery) = {
    val actor = context.actorOf(Props[A])
    actor.forward(query)
    actor ! PoisonPill
  }

  // close all db connections on shutdown
  override def postStop() {
    DatabaseAccess.cleanup();
  }
}
