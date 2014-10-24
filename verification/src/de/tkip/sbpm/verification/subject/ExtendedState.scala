package de.tkip.sbpm.verification.subject

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._

sealed trait StateData
case class ModulJoinStateData(counter: Int) extends StateData
case class SendStateData(remaining: Set[AgentId]) extends StateData
case class MacroEntry(entryState: StateId, name: String)
case object MacroDone extends StateData

object ExtendedState {
  def apply(state: State): ExtendedState = ExtendedState(state, None, Nil, Nil)
}
case class ExtendedState(state: State,
                         data: Option[StateData],
                         // the stack for the macros
                         macroStates: List[MacroEntry],
                         // the stack for modal split transitions
                         modalSplitStack: List[Int]) {
  def id = state.id
  def text = state.text
  def stateType = state.stateType
  def transitions = state.transitions
  def serviceParams = state.serviceParams

  def exitTransitions =
    transitions -- cancelTransitions

  /**
   * return timeout and break up Transitions
   */
  def cancelTransitions =
    transitions collect {
      case t @ Transition(BreakUpParam | TimeoutParam(_), _, _) => t
    }

  def timeOutTransition: Option[de.tkip.sbpm.newmodel.Transition] = {
    (transitions collect { case t @ Transition(TimeoutParam(_), _, _) => t })
      .headOption
  }

  def breakUpTransitions =
    transitions collect { case t @ Transition(BreakUpParam, _, _) => t }

  def communicationTransitions =
    transitions collect { case t @ Transition(_: CommunicationParams, _, _) => t }

  def singleTransition: Transition = {
    assert(
      state.transitions.size == 1,
      "It is not possible to get the single Transition, when " +
        state.transitions.size + " Transitions exists")

    state.transitions.head
  }

  def removeData: ExtendedState = copy(data = None)

  def setData(data: StateData): ExtendedState = copy(data = Some(data))

  /**
   * Creates a new Extended State for this state
   * (removes the stateData)
   */
  def setState(newState: State): ExtendedState =
    this.removeData.copy(state = newState)

  def appendMacro(entry: MacroEntry): ExtendedState =
    this.removeData.copy(macroStates = entry :: macroStates)

  def appendModal(i: Int) =
    this.removeData.copy(modalSplitStack = i :: modalSplitStack)

  def popModal =
    this.removeData.copy(modalSplitStack = modalSplitStack.tail)

  def currentMacro: Option[String] =
    if (macroStates.isEmpty) None
    else Some(macroStates.head.name)
}