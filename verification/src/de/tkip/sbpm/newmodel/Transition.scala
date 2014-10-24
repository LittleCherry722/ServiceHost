package de.tkip.sbpm.newmodel

import ProcessModelTypes._
import StateTypes._

case class Transition(exitParams: ExitParams, priority: Int, successor: StateId)
