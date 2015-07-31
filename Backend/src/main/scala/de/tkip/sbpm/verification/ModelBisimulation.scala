package de.tkip.sbpm.verification

import de.tkip.sbpm.model.{Graph, GraphSubject}
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId
import de.tkip.sbpm.verification.lts._

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.collection.parallel.immutable.ParSet

import scala.collection.SortedMap
import scalaz.Scalaz._


object ModelBisimulation {
  type MsgMapCandidates = SortedMap[String, Set[String]]
  type SubjectMapCandidates = SortedMap[GraphSubject, Seq[GraphSubject]]
  type MsgMap = SortedMap[String, String]
  type SubjectMap = SortedMap[GraphSubject, GraphSubject]
  type SubjectIdMap = SortedMap[SubjectId, SubjectId]
  type LtsStateTrans = (LtsState, Set[LtsTransition])

  /*
   * Check two graphs for behavioral congruence. The graphs are required to be valid graphs
   * (as checked by the Verificator engine).
   * True if g1 is behavioral congruent to g2 and vice versa.
   */
  def checkGraphs(g1: Graph, g2: Graph): Boolean = {
    val processGraphs = for {
      pg1 <- ModelConverter.convertForVerification(g1)
      pg2 <- ModelConverter.convertForVerification(g2)
    } yield (pg1, pg2)

    processGraphs match {
      case Left(_) => false
      case Right((pg1, pg2)) => {
        val lts1 = Verification.buildLts(pg1, optimize = true)
        val lts2 = Verification.buildLts(pg2, optimize = true)

        // Check if a subejct message mapping for g1 and g2 exists, such that g1 and g2 are behavioral
        // congruent.
        // Subject and message maps can be quite large, however checking obviously non congruend graphs
        // is quite fast.
        possibleGraphMappings(g1, g2).exists(_.exists {case (subjectMap, msgMaps) =>
          val sIdMap = subjectMap.map(s => (s._1.id, s._2.id))
          // Paralellize the lts congruence checks
          ParSet(msgMaps.toSeq: _*).exists { msgMap =>
            val checks = for {
              c1 <- Future { checkLts(lts1, lts2, sIdMap, msgMap) }
              c2 <- Future { checkLts(lts2, lts1, sIdMap.map(_.swap), msgMap.map(_.swap)) }
            } yield Seq(c1, c2)
            Await.result(checks, 90.seconds).fold(true)(_ & _)
          }
        })
      }
    }
  }


  /*
   * CheckLts performs the simulation (one-way) test for behavioral congruence of leftLts and rightLts.
   * The left LTS is the LTS to be simulated by the right LTS.
   * The supplied subjectIdMap and MsgMap are required to be exhaustive, even for intra-subject communication.
   * Failing to comply with this requirement will result in runtime exceptions.
   *
   * Scoped in this function are other auxiliary functions that make use of the supplied variables and
   * makes calling these functions much more convienent since the necessary parameters like the subjectMap or msgMap
   * do not have to be passed again and again.
   */
  private def checkLts(leftLts: Lts, rightLts: Lts, subjectMap: SubjectIdMap, msgMap: MsgMap) : Boolean = {
    val leftMap = leftLts.fromStatesMap
    val rightMap = rightLts.fromStatesMap
    val initLeft = (leftLts.startState, leftMap(leftLts.startState))
    val initRight = (rightLts.startState, rightMap(rightLts.startState))


    /*
     * Find a transition in the right LTS, starting with the 'right' (LtsState, Set[LtsTransition]) pair that
     * matches the supplied left transition, while only traversing tau states. This is a depth-first search
     * and only returns the first matching transition, bundled with the set of right LtsStates traversed
     * to arrive at this transition (this set of states is useful for caching reasons).
     * Only supplying one transition might potentially be problematic, however I was unable to create a test case
     * out of real processes where this might have caused a BIsimulation to produce false negatives.
     */
    def getEqualTransition(lTrans: LtsTransition, right: LtsStateTrans): Option[(LtsTransition, Set[LtsState])] = {

      // Keep a cache of all previously visited states to fail early on loops.
      // Return value is the same as for getEqualTransition.
      def withCache(right: LtsStateTrans, cache: Set[LtsState]): Option[(LtsTransition, Set[LtsState])] = {
        val (rNode, rTransitions) = right
        if (cache.contains(rNode)) {
          None // loop detected
        } else {
          for (rTrans <- rTransitions) {
            if (rTrans.label == Tau) {
              val nextRight = (rTrans.toState, rightMap(rTrans.toState))
              // match result of the recursive call. This is sadly not tail recoursive, but
              // tau transitions should not be THAT nested to really cause troubles anyway.
              withCache(nextRight, cache + rNode) match {
                case None => ()
                case res@Some(_) => return res // return early if a matching transition has been found
              }
            } else {
              if (compareTransitions(lTrans, rTrans)) {
                // return early with the first spotted matching transition
                return Some((rTrans, cache))
              }
            }
          }
          // We return early for matching transitions, leaving the loop therefore means we could not
          // find any matching transitions, which is why we return None.
          None
        }
      }
      withCache(right, Set.empty)
    }

    /*
     * Check if the end state of the right LTS is accessible by traversing only tau-transitions.
     * This might also find illegal end states, but since we require the LTS to be valid in the first place,
     * these illegal states should not exist.
     * Returns None if the end state is not reacable, Some(Set[LtsState]) with the set of states
     * being the intermediate states traversed to get to the end state, if one has been found.
     */
    def endIsTauReachable(stateTrans: LtsStateTrans): Option[Set[LtsState]] = {
      def withCache(stateTrans: LtsStateTrans, cache: Set[LtsState]): Option[Set[LtsState]] = {
        val (state, trans) = stateTrans
        val newCache = cache + state
        def next(trans: LtsTransition): (LtsState, Set[LtsTransition]) = {
          (trans.toState, rightMap(trans.toState))
        }
        if (trans.isEmpty) {
          Some(cache)
        } else {
          // lazy mapping + find + flatten is more or less equivalent to "exists", but returns the
          // result of the method call (with Some representing true and None false)
          // Since this is a lazy collection, map(mf).find(ff).flatten should essentially be the
          // same as .find(ff . mf).map(mf).flatten but with less overhead and only calling mf once.
          trans.filter(_.label == Tau).view.map(tt => withCache(next(tt), newCache)).find(_.isDefined).flatten
        }
      }
      withCache(stateTrans, Set.empty)
    }

    /*
     * Compare two transitions, left and right, for equality.
     */
    def compareTransitions(left: LtsTransition, right: LtsTransition) : Boolean = {
      // Transitions have to be of equal type and be non-tau transitions (there is really no way of telling if TAU
      // transitions can be considered equal, this just does not make sense.
      // If these requirements are met, we are left with send and receive transitions. We now require
      // that the send and receive subjects in the left transition are equal (as defined in the subjectIdMap)
      // to the subjects in the right transition, and that the passed message are of equal type.
      // In the case of our processing engine, equal type means the same ID / name for the message.
      (left.label, right.label) match {
        case (SendLabel(lFrom, _, lMsg, lTo, _), SendLabel(rFrom, _, rMsg, rTo, _)) =>
          rFrom == subjectMap(lFrom) && rTo == subjectMap(lTo) && rMsg == msgMap(lMsg)
        case (ReceiveLabel(lTarget, _, lMsg, lSender, _), ReceiveLabel(rTarget, _, rMsg, rSender, _)) =>
          rTarget == subjectMap(lTarget) && rSender == subjectMap(lSender) && rMsg == msgMap(lMsg)
        case _ => false
      }
    }

    /*
     * Actually check the two LTS for behavioral congruence.
     * Left is the initial (state, transitions) tuple of the left LTS, whereas right is the same for the right LTS.
     * The cache is a map of previously visited left states and the states in the right LTS that simulate this left state.
     * This cache is very important to keep the runtime complexity down, since branching and combining LTS are quite
     * common and the cache eliminates the need to recompute the joining branches from scratch each time.
     * The result is None if the right LTS can not simulate the left LTS, and the map of LTS stats in the right LTS
     * (values) that simulate a state in the left LTS (keys) if the left LTS can be simulated by the right LTS.
     */
    def check(left: (LtsState, Set[LtsTransition])
             ,right: (LtsState, Set[LtsTransition])
             ,cache: Map[LtsState, Set[LtsState]]): Option[Map[LtsState, Set[LtsState]]] = {
      // Define some named intermediate variables for easy access
      val ((lNode, lTransitions), (rNode, rTransitions)) = (left, right)
      val (lTauTs, lNonTauTs) = lTransitions.partition(_.label == Tau)
      val rNodeSetOpt = cache.get(lNode)

      // Add the current right state to the set of states that simulate the current left state.
      val newCache = rNodeSetOpt match {
        case None => cache + (lNode -> Set(rNode))
        case Some(rNodeSet) => cache.updated(lNode, rNodeSet + rNode)
      }

      if (rNodeSetOpt.exists(_.contains(rNode))) {
        // loop detected, we are in identical positions as we have been before, all good. Just return the cache.
        // This makes the final result dependent on whether every other transition can also be simulated.
        // This would obviously be problematic for LTS with only loops and no end state, but these are not valid
        // and can therefore be ignored.
        Some(newCache)
      } else if (lTransitions.isEmpty && rTransitions.nonEmpty) {
        // Current left state is the end state, right state is not. This is only possible if we
        // arrived at the end state by traversing a TAU-transition in the left LTS, therefore we require
        // a matching (sequence of) TAU transition(s) in the right LTS to arrive at the end state.
        endIsTauReachable(right).map { extraCacheStates =>
          newCache |+| Map(lNode -> extraCacheStates)  // combine caches
        }
      } else if (lTransitions.isEmpty && rTransitions.isEmpty) {
        // Left and right state arrived in the end states, we fould a valid simulation!
        Some(newCache)
      } else {
        // We are in the middle (of beginning) of the LTS. Proceed with checking all left transitions for matching
        // transitions in the right LTS.

        // Determine if the left non-tau transitions of the current state are valid.
        // For every non-tau transition, check if this transition is valid and create a new
        // cache from the result of this check to be used for the checks of the other transitions
        // Short-circuits to None if any check return none.
        // these functions are defined as functions and not regular variables as a performance optimization
        // as to not check both Tau and non-Tau transitions in case the first check fails.
        // They are also not lazy vals (which would fit the bill!) for debugging purposes.
        def lNonTauTsValid = lNonTauTs.foldLeft(some(newCache)){ case (newCacheOpt, lTrans) =>
          newCacheOpt.flatMap { newCache =>
            val nextLeft = (lTrans.toState, leftMap(lTrans.toState))
            val rightTauTs = rTransitions.filter(_.label == Tau)
            val rightAnswerTs = rTransitions.filter(rTrans => compareTransitions(lTrans, rTrans))

            // Check if for the current left transition there is a matching right transitions
            // that is a valid simulation of the left LTS. We do not require all transitions to be valid simulations,
            // as this fact is checked by switching the LTS in the BIsimulation part.
            def rightAnswerTsValid = rightAnswerTs.view.map{ rTrans =>
              val nextRight = (rTrans.toState, rightMap(rTrans.toState))
              check(nextLeft, nextRight, newCache).map(b => b |+| newCache)
            }.find(_.isDefined).flatten

            // Check if a any tau transition in the right LTS can satisfy the simulation requirements by
            // leading to a state that can simulate the current left state.
            def rightTauAlternative = getEqualTransition(lTrans, (rNode, rightTauTs)).view.map { case (rTrans, newCacheStates) =>
              val newNewCache = newCache |+| Map(lNode -> newCacheStates)
              val nextRight = (rTrans.fromState, rightMap(rTrans.fromState))
              check(left, nextRight, newNewCache)
            }.find(_.isDefined).flatten

            // make the computation of matching tau or non-tau answers in the right LTS
            // lazy and append the current (new) cache to the result of this computation.
            lazy val nonTauValid = rightAnswerTsValid.map(a => a |+| newCache)
            lazy val tauValid = rightTauAlternative.map(a => a |+| newCache)
            // return the first result that is not None of either nonTauValid or tauValid.
            nonTauValid <+> tauValid
          }
        }

        // determine if the left tau transitions from this state are valid.
        def lTauTsValid = lTauTs.foldLeft(some(newCache)) { case (newCacheOpt, lTrans) =>
          val nextLeft = (lTrans.toState, leftMap(lTrans.toState))
          newCacheOpt.flatMap(newCache => some(newCache) |+| check(nextLeft, right, newCache))
        }
        // Both lNonTauTsValid and lTauTsValid have to be true, so we flatMap over them
        // and append the current cache to the result (if any). If one of the two answers
        // is none, the whole return value will be None.
        lNonTauTsValid.flatMap(a => lTauTsValid.map(b => a |+| b))
      }
    }
    check(initLeft, initRight, Map.empty).isDefined
  }

  /*
   * For two graphs g1 and g2, get all possible mappings (as defined by the static interface of
   * both these process graphs) of subjectIds to subjectIds and for each of those, a set of possible
   * message mappings.
   * Returns None if no mapping is possible and a non-empty list of mappings otherwise.
   */
  def possibleGraphMappings(g1: Graph, g2: Graph): Option[Seq[(SubjectMap, Set[MsgMap])]] = {
    // creates a list of Maps from subjects in g1 to subjects in g2, such that every map is a possible
    // combination of subjects. This also filters out duplicate assignents such that no subject of g2 is assigned
    // to any subject in g1 more than once. This also ensures that every subject in g2 is mapped to a subject in g1.
    def createSubjectMaps(map: SubjectMapCandidates, acc: SubjectMap): Seq[SubjectMap] = {
      map.keys.headOption match {
        case None => Seq(acc)
        case Some(k) =>
          map(k).flatMap { v =>
            if (acc.values.exists(av => av == v)) {
              Seq.empty
            } else {
              val newMap = map - k
              val newAcc = acc + (k -> v)
              createSubjectMaps(newMap, newAcc)
            }
          }
      }
    }

    // practically the same as createSubjectMaps but for messages
    def createMessageMaps(map: MsgMapCandidates, acc: MsgMap): Set[MsgMap] = {
      map.keys.headOption match {
        case None => Set(acc)
        case Some(k) =>
          map(k).flatMap { v =>
            if (acc.values.exists(av => av == v)) {
              Seq.empty
            } else {
              val newMap = map - k
              val newAcc = acc + (k -> v)
              createMessageMaps(newMap, newAcc)
            }
          }
      }
    }

    // Find the start subjects in g1 and g2 and return them in a tuple of (g1, g2) start subjects.
    // returns None if either graph does not have a start subjects, or if the degrees of the start subjects
    // in g1 and g2 do not match.
    // Also filters out graphs that have different numbers of subjects.
    val startSubjects = for {
      g1Start <- g1.startSubject if g1.subjects.size == g2.subjects.size
      g2Start <- g2.startSubject if (g1Start.inDegree, g2Start.outDegree) == (g2Start.inDegree, g2Start.outDegree)
    } yield (g1Start, g2Start)

    // If matching start subjects exist, continue. Otherwise the return value is None.
    startSubjects.flatMap { case (g1Start, g2Start) =>
      // lists of subjects in g1 and g2 without the two start subjects.
      val trimmedG1Subs = g1.subjects.values.filterNot(_.id == g1Start.id)
      val trimmedG2Subs = g2.subjects.values.filterNot(_.id == g2Start.id)

      // All possible (g1, g2) subject combinations of the filtered subjects,
      // plus the known combination for the start subjects, AND filtered to only allow combinations of subjects
      // that have the same degree of incoming and outgoing messages.
      // This brings the list of possible combinations down to a somewhat manageable number.
      val subjectCombinations = Seq((g1Start, g2Start)) ++ (for {
        g1s <- trimmedG1Subs
        g2s <- trimmedG2Subs if (g1s.inDegree, g1s.outDegree) == (g2s.inDegree, g2s.outDegree)
      } yield (g1s, g2s))

      // Transform the list of tuples of possible graph mappings to a sorted map of mapping candidates.
      val subjectMapList = subjectCombinations.foldLeft(SortedMap.empty[GraphSubject, Seq[GraphSubject]]) {
        case (acc, (s1, s2)) =>
          acc.get(s1) match {
            case Some(s2l) => acc.updated(s1, s2l :+ s2)
            case None => acc + (s1 -> Seq(s2))
          }
      }

      val subjectMaps = createSubjectMaps(subjectMapList, SortedMap.empty).flatMap { sMap =>
        val sIdMap = sMap.map(st => (st._1.id, st._2.id))
        val tmpMsgMap = sMap.keys.map { lSub =>
          val com = for {
            (lSubId, lMsg) <- lSub.outCom
            rMsgs = sMap(lSub).outCom.filter {
              case (rSubId, rMsg) => sIdMap(lSubId) == rSubId
            }.map(_._2)
          } yield (lMsg, rMsgs)

          com.foldLeft(Map.empty[String, Set[String]]) { case (acc, (lMsg, rMsgs)) =>
              val newRMsgs = acc.getOrElse(lMsg, Set.empty) ++ rMsgs
              acc.updated(lMsg, newRMsgs)
          }
        }.fold(Map.empty[String, Set[String]]) (_ |+| _)
        val potentialMsgMaps = createMessageMaps(SortedMap(tmpMsgMap.toSeq: _*), SortedMap.empty)
        val msgMaps = potentialMsgMaps.filter { msgMap =>
          sMap.keys.forall { lSub =>
            lSub.outCom.forall { case (lSubId, lMsg) =>
              sMap(lSub).outCom.exists { case (rSubId, rMsg) =>
                sIdMap(lSubId) == rSubId && msgMap(lMsg) == rMsg
              }
            }
          }
        }
        if (msgMaps.isEmpty) {
          None
        } else {
          Some((sMap, msgMaps))
        }
      }

      if (subjectMaps.isEmpty) {
        None
      } else {
        Some(subjectMaps)
      }
    }
  }
}