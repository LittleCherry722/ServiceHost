package de.tkip.sbpm.application.subject.behavior.state

protected class MacroStateActor(data: StateData) extends BehaviorStateActor(data) {

  def stateReceive = {
    case abc => //TODO
  }
  
  protected def getAvailableAction = Array()
}