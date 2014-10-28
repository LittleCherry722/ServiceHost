package de.tkip.sbpm.verification.lts

import scala.collection.mutable
import de.tkip.sbpm.newmodel.ProcessModelTypes.AgentId
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.misc.HashCodeCache

case class Lts(states: Set[LtsState],
               transitions: Set[LtsTransition],
               startState: LtsState) extends HashCodeCache {

  lazy val fromStatesMap: Map[LtsState, Set[LtsTransition]] = {
    val mutableMap = mutable.Map[LtsState, mutable.Set[LtsTransition]]()
    for (trans @ LtsTransition(f, _, _) <- transitions) {
      val s = mutableMap.getOrElseUpdate(f, mutable.Set[LtsTransition]())
      s += trans
    }

    (mutableMap map (s => ((s._1, s._2 toSet))) toMap).withDefaultValue(Set())
  }

  lazy val toStatesMap: Map[LtsState, Set[LtsTransition]] = {
    val mutableMap = mutable.Map[LtsState, mutable.Set[LtsTransition]]()
    for (trans @ LtsTransition(_, _, t) <- transitions) {
      val s = mutableMap.getOrElseUpdate(t, mutable.Set[LtsTransition]())
      s += trans
    }

    (mutableMap map (s => ((s._1, s._2 toSet))) toMap).withDefaultValue(Set())
  }

  // validity check
  lazy val (valid, invalidNodes): (Boolean, Set[LtsState]) = {
    val endStateOpt = states.find(_.subjectMap.isEmpty)
    if (endStateOpt.isDefined) {
      val endState = endStateOpt.get
      import scala.collection.mutable
      val validNodes = mutable.Set.empty[LtsState]
      var workSet = mutable.Set(endState)

      while (workSet.nonEmpty) {
        validNodes ++= workSet
        val nextWorkSet = mutable.Set.empty[LtsState]

        for (
          w <- workSet;
          n <- toStatesMap(w)
        ) {
          nextWorkSet += n.fromState
        }

        workSet = nextWorkSet &~ validNodes
      }

      val inValidNodes = states &~ validNodes
      (inValidNodes.isEmpty, inValidNodes)
    } else (false, states)
  }

  /**
   * Easy T1 pruning
   * Remove after Tau Law: T1
   * T1 Law:
   * t := tau
   * l := (action)
   * l.t.T = l.T
   *
   * n_a -l> n_b -t> n_c
   * ==>
   * n_a -l> n_c
   *
   */
  private def removeSimpleTau: Lts = {
    val singleTauStartNodes = (fromStatesMap collect {
      case (n, s) if (s.size == 1 && s.head.label == Tau) => n
    }).toSet - startState

    val irrelevant = singleTauStartNodes map (fromStatesMap(_).head)

    val miniFromMap = irrelevant map (t => (t.fromState, t)) toMap

    def latestPathElem(s: LtsState): LtsState =
      if (!miniFromMap.contains(s)) s
      else latestPathElem(miniFromMap(s).toState)

    val tauPaths = for {
      trans <- irrelevant
      from = trans.fromState
      stepTrans <- toStatesMap(from)
    } yield stepTrans.copy(toState = latestPathElem(trans.toState))

    val delete = for (t <- irrelevant; s <- toStatesMap(t.fromState)) yield s

    val overStepTransitions = tauPaths.filterNot(singleTauStartNodes contains _.fromState)

    Lts(
      states -- singleTauStartNodes,
      transitions -- irrelevant -- delete ++ overStepTransitions,
      startState)
  }

  /**
   * T2 Law:
   * t := Tau
   * T + t.T = t.T
   * ==>
   * T := x.P (x in t, receive, send)
   * x.P + t.x.P = t.x.P
   * also
   * n_a -t> n_b -x> n_c
   *         -x>
   * wird ersetzt durch
   * n_a -t> n_b -x> n_c
   *
   */
  private def removeT2: Lts = {
    val singleTransitionsStartNodes = (fromStatesMap collect {
      case (n, s) if (s.size == 1) => n
    }).toSet - startState
    val singleTransitions = singleTransitionsStartNodes map (fromStatesMap(_).head)
    val doubleSteps = for {
      trans <- singleTransitions
      middle = trans.fromState
      first <- toStatesMap(middle)
      if (first.label == Tau)
    } yield trans.copy(fromState = first.fromState)

    copy(transitions = transitions -- doubleSteps)
  }

  /**
   * Pruning using T2 Law
   *
   * removing the first transitions if the successors are
   * equivalent (equal outgoing transitions)
   */
  private def advremoveT2: Lts = {
    val removeTransitions =
      for {
        // get all Tau-transitions
        trans <- transitions
        if (trans.label == Tau)
        from = trans.fromState
        to = trans.toState
        // get all outgoing Transitions from the both states
        // T
        predTransitions = fromStatesMap(from) filterNot (_.toState == to)
        fTrans = predTransitions map (t => (t.label, t.toState))
        // t.T'
        tTrans = fromStatesMap(to) map (t => (t.label, t.toState))
        // check if T = T'
        if (tTrans == fTrans)
        // if yes remove the first Transitions
        // T = t.T
        // remove the transitions from the predecessor
        predTrans <- predTransitions
      } yield predTrans

    copy(transitions = transitions -- removeTransitions)
  }

  /**
   * Remove Transitions with the 3rd Tau Law
   */
  private def removeT3: Lts = {
    val removeTrans = {
      transitions filter { t =>
        fromStatesMap(t.fromState) exists { r =>
          fromStatesMap(r.toState) exists { s =>
            s.label == t.label && s.toState == t.toState
          }
        }
      }
    }

    copy(transitions = transitions -- removeTrans)
  }

  /**
   * LTS removal using C2 Rule
   */
  private def removeC2: Lts = {

    val remove =
      for {
        center <- states
        targets = fromStatesMap(center) map (_.toState)

        // if there is one tau Transitions to this state
        preds = toStatesMap(center)
        if (preds.size == 1 && preds.head.label == Tau)
        pred = preds.head.fromState
        predTransitions = fromStatesMap(pred)

        // if there is one tau transition to the predecessorState
        transToPreds = toStatesMap(pred)
        if (transToPreds.size == 1 && transToPreds.head.label == Tau)
        transToPred = transToPreds.head
        if (transToPred.label == Tau)

        // it is possible to remove the Transitions, the predecessor has to the successor
        removeAble <- fromStatesMap(center)
        r = removeAble.copy(fromState = pred)
      } yield r

    copy(transitions = transitions -- remove)
  }

  /**
   * Removes the States, which are not connected in the LTS
   */
  private def removeUnusedStates: Lts = {
    val targetStates = transitions.map(_.toState) + startState

    val unusedStates = states -- targetStates
    val usedTransitions = transitions filterNot (t => unusedStates contains (t.fromState))

    Lts(
      targetStates,
      usedTransitions,
      startState)
  }

  /**
   * Removes the States with equal successors/transitions
   *
   * (Only removes 1 State)
   */
  private def removeEqualStates: Lts = {

    // calculate all equal states (states with equal outgoing transitions)
    val equalStateClass: Set[Set[LtsState]] = {
      // map [Label, TargetState] -> FromState
      val mapMaker =
        for {
          (s, ts) <- fromStatesMap.toSet
          t = ts map (t => (t.label, t.toState))
        } yield (t, s)
      // Filter the map to get the FromStates with equal transitions
      for {
        ts <- mapMaker map (_._1)
        equalStates = for {
          s <- mapMaker filter (ts == _._1) map (_._2)
        } yield s
      } yield equalStates
    }
    // for all equal states
    val equalStates = equalStateClass filter (_.size > 1)

    // select one state
    if (equalStates.size >= 1) {
      val eqs = equalStates.head

      // update all incoming transitions
      // create with the state as target
      // remove the old ones
      val eq1 = eqs.head

      val (removeTrans, addTrans) = {
        val both =
          for {
            e <- eqs
            if (e != eq1)
            t <- toStatesMap(e)
          } yield (t, t copy (toState = eq1))

        // Also remove the outgoing transitions!
        val outgoing =
          for {
            e <- eqs
            if (e != eq1)
            f <- fromStatesMap(e)
          } yield f

        (both.map(_._1) ++ outgoing, both.map(_._2))
      }
      copy(transitions = transitions -- removeTrans ++ addTrans)
      Lts(
        (states -- eqs) + eq1,
        transitions -- removeTrans ++ addTrans,
        startState)
    } else
      this
  }

  def prune: Lts = {
    // use the remove rules in a usefull order
    var lts = this
    var transSize = 0
    var stateSize = 0
    while (stateSize != lts.states.size && transSize != lts.transitions.size) {
      transSize = lts.transitions.size
      stateSize = lts.states.size

      lts = lts.removeSimpleTau
      lts = lts.removeT2
      lts = lts.removeC2
      lts = lts.advremoveT2
      lts = lts.removeUnusedStates
      lts = lts.removeEqualStates
      lts = lts.removeT3
    }

    transSize = 0
    stateSize = 0
    while (stateSize != lts.states.size && transSize != lts.transitions.size) {
      transSize = lts.transitions.size
      stateSize = lts.states.size
      lts = lts.removeUnusedStates
      lts = lts.removeEqualStates
    }
    transSize = 0
    stateSize = 0
    while (stateSize != lts.states.size && transSize != lts.transitions.size) {
      transSize = lts.transitions.size
      stateSize = lts.states.size

      lts = lts.removeSimpleTau
      lts = lts.removeT2
      lts = lts.removeC2
      lts = lts.removeUnusedStates
      lts = lts.removeT3

      lts = lts.removeEqualStates
      lts = lts.removeUnusedStates
    }
    lts
  }

  /**
   * Creates the Lts for the given subject
   */
  def createFor(subject: SubjectId, agentId: AgentId = 0): Lts = {
    val filteredTransitions =
      transitions.map {
        case trans @ LtsTransition(_, label, _) => label match {
          case l @ SendLabel(_, _, _, `subject`, s) if (s.contains(agentId)) =>
            trans.copy(label = l.copy(toAgents = Set(agentId)))
          case l @ ReceiveLabel(_, _, _, `subject`, s) if (s.contains(agentId)) =>
            trans.copy(label = l.copy(senderAgents = Set(agentId)))
          // replace other labels with Tau
          case t => trans.copy(label = Tau)
        }
      }

    copy(transitions = filteredTransitions)
  }
}