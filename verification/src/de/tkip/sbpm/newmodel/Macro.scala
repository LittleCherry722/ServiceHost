package de.tkip.sbpm.newmodel

import ProcessModelTypes.StateId

case class Macro(name: String, startState: StateId, states: Set[State]) {
  
  require( 
  states map(_.id) contains startState,
  "The start-state muss be a state of the macro"
  )
  
  // helper methods
  private val stateMap = states map (s => (s.id, s)) toMap
  def state(id: StateId) = stateMap(id)
}