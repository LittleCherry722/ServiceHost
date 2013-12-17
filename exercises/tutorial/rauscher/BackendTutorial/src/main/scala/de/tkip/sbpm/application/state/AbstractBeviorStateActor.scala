package de.tkip.sbpm.application.state

import akka.actor.Actor
import de.tkip.sbpm.model.State

abstract class AbstractBeviorStateActor(protected val state: State) extends Actor
