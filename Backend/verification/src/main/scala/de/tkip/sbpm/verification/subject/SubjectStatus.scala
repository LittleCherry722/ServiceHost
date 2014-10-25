package de.tkip.sbpm.verification.subject

import scala.collection.immutable.Queue
import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.misc.HashCodeCache

case class SubjectStatus(subject: Subject,
                         channel: Channel,
                         ip: InputPool,
                         variables: Variables,
                         activeStates: Set[ExtendedState])
  extends VerificationSubject with HashCodeCache {

  /**
   * Returns if the other subject has an equal internal status,
   * which means that all fields are the same except the channel
   */
  def ~~(other: SubjectStatus): Boolean = {
    other.copy(channel = channel) == this
  }

  def id = subject.id

  def currentStates = activeStates

  lazy val observerPossible: Boolean = {
    activeStates.exists { s =>
      s.stateType == Observer && {
        s.transitions.collect { case Transition(c: CommunicationParams, _, _) => c }
          .exists { c =>
            c.min match {
              case Number(n) => n <= countMessages(c.messageType, c.subject)
              case AllMessages => true
              case _ => false
            }
          }
      }
    }
  }

  /**
   * Changes the statedate for the given state
   */
  def setStateData(state: ExtendedState, data: StateData): SubjectStatus = {
    assert(
      activeStates contains state,
      "the State to set the StateData musst exists")

    copy(activeStates = activeStates - state + state.setData(data))
  }
  /**
   * exchanges the state with the new state
   * (use with care!)
   */
  def updateState(state: ExtendedState, newState: ExtendedState): SubjectStatus = {
    assert(
      activeStates contains state,
      "the State to set the StateData musst exists")

    //    SubjectStatus(subject, channel, ip, variables, activeStates - state + newState)
    copy(activeStates = activeStates - state + newState)
  }

  private def successorState(stateId: StateId, pred: ExtendedState): State = {
    val macro = pred.currentMacro
    val state =
      if (macro.isDefined) subject.macro(macro.get).state(stateId)
      else subject.state(stateId)
    state
  }

  private def extendsModalJoin(stateId: StateId, pred: ExtendedState): Set[ExtendedState] = {

    val state = successorState(stateId, pred)

    if (state.stateType == Join) {
      val joinState = activeStates.find({
        state =>
          state.stateType == Join && state.id == stateId &&
            state.macroStates == pred.macroStates
      })
      if (joinState.isDefined) {
        val n = joinState.get
        val Some(ModulJoinStateData(count)) = n.data
        val newState =
          n.setData(ModulJoinStateData(count + 1))

        return activeStates - pred - n + newState
      } else {
        return activeStates - pred +
          pred.setState(state).setData(ModulJoinStateData(1))

      }
    }
    activeStates - pred + pred.setState(state)
  }

  private def extend(stateId: StateId, pred: ExtendedState): ExtendedState = {
    val macro = pred.currentMacro
    val state =
      if (macro.isDefined) subject.macro(macro.get).state(stateId)
      else subject.state(stateId)
    // ModalJoins musst count incoming transitions

    if (state.stateType == Join) {
      val joinState = activeStates.find({
        state =>
          state.stateType == Join && state.id == stateId &&
            state.macroStates == pred.macroStates
      })
      if (joinState.isDefined) {

        System.err.println("ERROR");
        return pred
      } else {
        return ExtendedState(state, Some(ModulJoinStateData(1)), pred.macroStates, pred.modalSplitStack)
      }
    }

    ExtendedState(state, None, pred.macroStates, pred.modalSplitStack)
  }

  // Inputpool access methods
  /**
   * Retuns whether the InputPool is empty
   */
  def isIpEmpty: Boolean = ip.isEmpty

  def hasMessage(messageType: MessageType, channel: Channel): Boolean =
    hasMessage(messageType, channel.subjectId)
  def hasMessage(messageType: MessageType,
                 subjectId: SubjectId): Boolean =
    ip.hasMessage(messageType, subjectId)

  def countMessages(messageType: MessageType,
                    subjectId: SubjectId): Int =
    ip.countMessages(messageType, subjectId)

  def receiveMessages(messageType: MessageType,
                      subjectId: SubjectId,
                      count: Int,
                      storeVar: Option[String]): SubjectStatus = {
    if (count == 0) {
      if (storeVar.isDefined) {
        // if the storevar ist defined store an empty var
        copy(variables = variables.add(storeVar.get, MessageList(Nil)))
      } else {
        // if count == 0 and no storevar, do nothing
        this
      }
    } else {
      val (messageList, newInputPool) =
        ip.pullMessage(messageType, subjectId, count)

      val newVar = storeVar match {
        case None => variables
        case Some(name) => variables.add(name, messageList)
      }
      copy(ip = newInputPool, variables = newVar)
    }
  }

  private lazy val isTerminated = activeStates.exists(_.stateType == End)
  def hasInputPoolSpace(messageType: MessageType,
                        channel: Channel): Boolean = {
    if (isTerminated) false
    else countMessages(messageType, channel.subjectId) < subject.ipSize
  }

  /**
   * Puts a message in the InputPool of this subject
   */
  def putMessage(content: MessageContent,
                 messageType: MessageType,
                 channel: Channel): SubjectStatus = {

    assert(
      countMessages(messageType, channel.subjectId) < subject.ipSize,
      this.channel + ": It is not possible to put a message into a full InputPool;" +
        " message: %s to %s, %s; ip: %s"
        .format(content, messageType, channel, ip))

    copy(ip = ip.putMessage(content, messageType, channel))
  }

  /**
   * Adds a variable to this subject
   */
  def addVar(name: VarName, content: MessageList) =
    copy(variables = variables.add(name, content))

  /**
   * Open the InputPool
   */
  def open(openIP: OpenIP): SubjectStatus =
    copy(ip = ip.open(openIP))

  /**
   * Close the InputPool
   */
  def close(closeIP: Set[(SubjectId, MessageType)]): SubjectStatus =
    copy(ip = ip.close(closeIP))

  def activateState(stateId: StateId) = {
    copy(activeStates = activeStates + ExtendedState(subject.state(stateId)))
  }

  def deactivateState(id: StateId): SubjectStatus = {
    copy(activeStates =
      activeStates.filterNot(s => s.macroStates == Nil && s.id == id))
  }

  def killStates(states: Set[ExtendedState]): SubjectStatus = {
    assert(
      states forall { state => activeStates contains state },
      "The state to kill is not a member of the active states")

    if (states.isEmpty) this
    else copy(activeStates = activeStates -- states)
  }

  /**
   * Enters a macro
   */
  def enterMacro(state: ExtendedState, macroName: String): SubjectStatus = {
    val macro = subject.macro(macroName)
    val s = macro.state(macro.startState)

    // create the first macro state
    val newState =
      state.setState(s).appendMacro(MacroEntry(macro.startState, macroName))

    copy(activeStates = activeStates - state + newState)
  }

  /**
   * Leaves a macro
   */
  def leaveMacro(state: ExtendedState): SubjectStatus = {
    assert(
      state.macroStates.size > 0,
      "It is only possible to leave a Macro in a Macro")
    val newMacroStates = state.macroStates.tail

    // the entrystate into the macro
    val entryState = state.macroStates.head.entryState
    val s =
      if (newMacroStates.size > 0)
        subject.macro(newMacroStates.head.name).state(entryState)
      else subject.state(entryState)

    val newState =
      ExtendedState(s, Some(MacroDone), newMacroStates, state.modalSplitStack)

    copy(activeStates = activeStates - state + newState)
  }

  /**
   * Fires a specific transition of a state
   */
  def fireTransition(state: ExtendedState, transition: Transition): SubjectStatus = {
    assert(state.transitions contains transition,
      "It is not possible to fire a transition, which is not in the state")

    exchangeState(state, transition.successor)
  }

  /**
   * Fires the Transition of the State (if the state only has only)
   */
  def fireTransitionOf(state: ExtendedState): SubjectStatus = {
    exchangeState(state, state.singleTransition.successor)
  }

  /**
   * Fires the Modal Split Transitions (does not create the Modal Split Stack)
   */
  def fireModalSplit(state: ExtendedState): SubjectStatus = {
    // append the new Modal Join info
    //    val s = state.appendModal(state.transitions.size)
    assert(
      state.stateType == Split,
      "It is only possible do enter a modal split in a modal split state")

    exchangeState(state, state.transitions map (_.successor))
  }

  /**
   * Changes the state to the successor
   */
  private def exchangeState(from: ExtendedState, to: StateId): SubjectStatus = {
    assert(
      activeStates contains from,
      "The state to change is not a member of the active states")

    copy(activeStates = extendsModalJoin(to, from))
  }

  /**
   * Changes the state to several successors
   */
  private def exchangeState(from: ExtendedState, to: Set[StateId]): SubjectStatus = {
    assert(
      activeStates contains from,
      "The state to change is not a member of the active states")

    copy(activeStates = activeStates - from ++ to.map(extend(_, from)))
  }

  override def toString = mkString

  def mkString =
    "[%s, %s]@%s'%s %s"
      .format(
        channel.subjectId,
        channel.agentId,
        activeStates.map(
          e => {
            val s = e.id
            val state =
              if (e.currentMacro.isDefined) subject.macro(e.currentMacro.get).state(s)
              else subject.state(s)
            "%s%s%s%s".format(
              s,
              state.stateType(0).toUpper,
              state.serviceParams match {
                case NoServiceParams => ""
                case x => x.toString()(0)
              },

              e.data match {
                case None => ""
                case Some(SendStateData(r)) => s"<${r.size}>"
                case Some(ModulJoinStateData(r)) => s"<$r>"
                case Some(MacroDone) => "_"
              })
          }).mkString(", "),
        ip.mkString,
        variables.mkShortString)
}