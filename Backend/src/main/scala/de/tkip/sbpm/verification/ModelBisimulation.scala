package de.tkip.sbpm.verification

import de.tkip.sbpm.newmodel.Channel
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.verification.lts._

/**
 * Created by arne on 23.10.14.
 */
object ModelBisimulation {

  def testCheck = {
    checkStructure(leftLts, rightLts)
  }

  def leftLts = {
    val receive = ReceiveLabel("", 0, "", "", Set())
    val send = SendLabel("", 0, "", "", Set())
    val states = Seq(
      LtsState(Map(Channel("0", 0) -> null)),
      LtsState(Map(Channel("1", 1) -> null)),
      LtsState(Map(Channel("2", 2) -> null))
    )
    val trans = Seq(
      LtsTransition(states(0), send, states(1)),
      LtsTransition(states(1), send, states(2)),
      LtsTransition(states(1), receive, states(2))
    )
    val start = states.head
    Lts(states.toSet, trans.toSet, start)
  }

  def rightLts = {
    val receive = ReceiveLabel("", 0, "", "", Set())
    val send = SendLabel("", 0, "", "", Set())
    val states = Seq(
      LtsState(Map(Channel("0", 0) -> null)),
      LtsState(Map(Channel("1", 1) -> null)),
      LtsState(Map(Channel("2", 2) -> null)),
      LtsState(Map(Channel("3", 3) -> null)),
      LtsState(Map(Channel("4", 4) -> null)),
      LtsState(Map(Channel("5", 5) -> null)),
      LtsState(Map(Channel("6", 6) -> null))
    )
    val trans = Seq(
      LtsTransition(states(0), Tau, states(1)),
      LtsTransition(states(0), Tau, states(2)),
      LtsTransition(states(1), send, states(3)),
      LtsTransition(states(2), send, states(3)),
      LtsTransition(states(3), send, states(4)),
      LtsTransition(states(3), receive, states(5)),
      LtsTransition(states(4), Tau, states(6)),
      LtsTransition(states(5), Tau, states(6))
    )
    val start = states.head
    Lts(states.toSet, trans.toSet, start)
  }

  private var cache = Map[LtsState, Set[LtsState]]()
  private var leftMap = Map[LtsState, Set[LtsTransition]]()
  private var rightMap = Map[LtsState, Set[LtsTransition]]()
  private var subjectMap = Map[SubjectId, SubjectId]()

  def checkStructure(left: Lts, right: Lts) : Option[Seq[(LtsState, Set[LtsState])]] = {
    leftMap = left.fromStatesMap
    rightMap = right.fromStatesMap
    val initLeft = (left.startState, leftMap(left.startState))
    val initRight = (right.startState, rightMap(right.startState))
    cache = Map()
    if (checkState(initLeft, initRight)) {
      Some(cache.toSeq)
    } else {
      None
    }
  }

  def checkSubjects = {

  }

  private def checkState(left:  (LtsState, Set[LtsTransition]),
                      right: (LtsState, Set[LtsTransition])) : Boolean  = {
    val (lNode, lTrans) = left
    val (rNode, rTrans) = right
    if(cache.getOrElse(lNode, Set()).contains(rNode)) {
      true
    } else if (transitionSubset(lTrans, rTrans) || rTrans.exists(_.label == Tau)) {
      cache = cache + ((lNode, cache.getOrElse(lNode, Set()) + rNode))
      lTrans.map{ lt =>
        if (lt.label == Tau) {
          val nextLeft = (lt.toState, leftMap(lt.toState))
          val nextRight = right
          checkState(nextLeft, nextRight)
        } else if (rTrans.exists{ rt => compareTransitions(lt, rt)}) {
          rTrans.filter{ rt => compareTransitions(lt, rt) }.map{ rt =>
            val nextLeft = (lt.toState, leftMap(lt.toState))
            val nextRight = (rt.toState, rightMap(rt.toState))
            checkState(nextLeft, nextRight)
          }.forall(_ == true)
        } else {
          true
        }
      }.forall(_ == true) && rTrans.filter(_.label == Tau).map{ rt =>
        val nextLeft = left
        val nextRight = (rt.toState, rightMap(rt.toState))
        checkState(nextLeft, nextRight)
      }.forall(_ == true)
    } else {
      false
    }
  }

  private def compareStates(left: LtsState, right: LtsState) : Boolean = {
    false
  }

  private def compareTransitions(left: LtsTransition, right: LtsTransition) : Boolean = {
    false
  }

  private def transitionSubset(left: Set[LtsTransition], right: Set[LtsTransition]) : Boolean = {
    left.filter(_.label != Tau).forall{ lt => right.exists{ rt => compareTransitions(lt, rt) } }
  }
}