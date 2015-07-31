package de.tkip.sbpm.anonymization

import de.tkip.sbpm.model._
import de.tkip.sbpm.newmodel.ProcessModelTypes.SubjectId

import scala.annotation.tailrec


object Anonymizer {
  type GraphNodeId = Short

  // Create a view of the graph originalGraph for the subject with the specified viewSubjectId.
  // TODO: Implement advanced Pruning
  def createView(viewSubjectId: SubjectId, originalGraph: Graph): Either[String, View] = {
    // make sure the of the currently local subjects, only one is communicating with the world.
    // If this is not the case, report the violation and return
    // At the moment, we require that exactly one local subject is in contact with all
    // other interface subjects.
    // Ideally, we would require every isolated group of interface subject
    // to be communicating with only one local subject, so that there can be two independent
    // groups of interface subject that do not directly communicate with each other in the
    // same process. Simple example: IS1 <--msg--> LS1 <--msg--> LS2 <--msg--> IS2
    val proxies = getProxySubjects(originalGraph)
    if (proxies.size != 1) {
      Left("Exactly one local subject must be in contact with interface subjects.")
    } else {
      val proxy = proxies.head

      // Squash all subjects to (hopefully) one single subject.
      squashLocalSubjects(originalGraph, proxy) match {
        case None => Left("Cannot squash local subjects")
        case Some(newGraph) =>
          // Mark the viewSubject as local single subject and all others as interface subjects
          squashInterfaceSubjects(flipSubjectTypes(newGraph, viewSubjectId)) match {
            case None => Left("Cannot squash interface subjects")
            case Some(g) => Right(View(viewSubjectId, g))
          }
      }
    }
  }

  // Given a process graph and a proxy subject that is not to be removed, prune all
  // other local (aka single) subjects that can safely be removed.
  // Fore more details about the pruning procedure, see pruneSubject and removableSubjects
  // STATUS: DONE
  @tailrec
  private def squashLocalSubjects(graph: Graph, localProxy: GraphSubject): Option[Graph] = {
    val localSubs = getLocalSubjects(graph)
    if (localSubs.size == 1) {
      Some(graph)
    } else if (localSubs.size < 1) {
      None
    } else {
      val removeCandidates = localSubs.filter(_.id != localProxy.id)
      val newGraph: Option[Graph] = removableSubjects(removeCandidates, graph).headOption.map {
        case (remSub, fromSub) => pruneSubject(remSub.id, fromSub.id, graph)
      }.orElse {
        None
        // View creates a lazy view on the set of subjects. Mapping over the lazy list and then finding
        // ensures the mergeSubject function is only executed exactly as often as needed to find
        // a subject that can be merged.
        // Put more clearly, the mapping function is called for the first element of the list,
        // then the find function is called for this element. If an element is found, the search can terminate
        // early. If not, the mapping function is called for the next element in the list, etc.
//        val mergePairs = for {
//          distantSub <- localSubs - localProxy
//          comSubId <- distantSub.transitions.filter(s => s.isReceive || s.isSend).flatMap(_.targetSubjectId.filterNot(_.isEmpty))
//        } yield (comSubId, distantSub.id)
//        mergePairs.view.map(remSubPair => mergeSubject(remSubPair._1, remSubPair._2, graph)).find(g =>
//          g.subjects.size < graph.subjects.size
//        )
      }

      // hand-rolled map, since map itself is a function after all and would make this function
      // not be tail-recursive.
      newGraph match {
        case None => None // no reduction was possible, but we still have more than one lcoal subject
        case Some(nextGraph) => squashLocalSubjects(nextGraph, localProxy)
      }
    }
  }

  // Given a process graph, prune all interface subjects that can safely be removed. Does not modify
  // local subjects.
  // Fore more details about the pruning procedure, see pruneSubject and removableSubjects
  // STATUS: DONE
  private def squashInterfaceSubjects(graph: Graph): Option[Graph] = {
    getLocalSubjects(graph).headOption match {
      case None => None
      case Some(viewSubject) =>
        val subjects = graph.subjects.values.toSet

        // Gather subjects that are to be deleted.
        // These are subjects who are not in direct communication with the viewSubject.
        val neighborSubjects = for {
          subject <- subjects
          transition <- subject.transitions
          relatedSubject = subject if transition.interactsWith(viewSubject) || subject.id == viewSubject.id
        } yield relatedSubject
        val distantSubjects = graph.subjects.values.toSet -- neighborSubjects

        // return the graph if only direct neighbor subjects are left. This is our goal ultimately
        if (distantSubjects.isEmpty) {
          Some(graph)
        } else {
          val newGraph: Option[Graph] = removableSubjects(distantSubjects, graph).headOption.map {
            case (remSub, fromSub) =>
              pruneSubject(remSub.id, fromSub.id, graph)
          }.orElse {
            None
            // View creates a lazy view on the set of subjects. Mapping over the lazy list and then finding
            // ensures the mergeSubject function is only executed exactly as often as needed to find
            // a subject that can be merged.
            // Put more clearly, the mapping function is called for the first element of the list,
            // then the find function is called for this element. If an element is found, the search can terminate
            // early. If not, the mapping function is called for the next element in the list, etc.
//            val mergePairs = for {
//              distantSub <- distantSubjects
//              comSubId <- distantSub.transitions.filter(s => s.isReceive || s.isSend).flatMap(_.targetSubjectId.filterNot(_.isEmpty))
//            } yield (comSubId, distantSub.id)
//            mergePairs.view.map(remSubPair => mergeSubject(remSubPair._2, remSubPair._1, graph)).find(g =>
//              g.subjects.size < graph.subjects.size
//            )
          }

          newGraph match {
            case None =>
              Some(graph)
            case Some(g) =>
              squashInterfaceSubjects(g)

          }
        }
    }
  }

  // Given a process graph, get all local subjects (aka single subjects)
  private def getLocalSubjects(graph: Graph): Set[GraphSubject] = {
    graph.subjects.values.filter(_.subjectType == SubjectType.SingleSubjectType).toSet
  }

  // Given a process graph, get all interface subjects
  private def getInterfaceSubjects(graph: Graph): Set[GraphSubject] = {
    graph.subjects.values.filter { s =>
      s.subjectType == SubjectType.ExternalSubjectType && s.externalType.contains(SubjectExternalType.InterfaceSubjectType)
    }.toSet
  }

  // Get all the local subjects in this graph (process) that are in contact
  // with interface subjects.
  // STATUS: DONE
  private def getProxySubjects(graph: Graph): Set[GraphSubject] = {
    val localSubjects = getLocalSubjects(graph)
    val interfaceSubjects = getInterfaceSubjects(graph).toSeq
    // Create a set of (LocalSubjectId, InterafaceSubjectId) tuples that model
    // the communication of localSubject with InterfaceSubject
    val remoteInteractions = localSubjects.filter { s =>
      s.transitions.filter { t => t.isReceive || t.isSend }.exists { t =>
        val targetIdOption = t.edge.target.map(_.subjectId)
        interfaceSubjects.exists { is =>
          targetIdOption.contains(is.id)
        }
      }
    } // toSet kindly removes duplicates for us
    remoteInteractions
  }

  // Modify all subject types in the graph to have the focusSubject be the local subject
  // while all other subjects are modified to be interface subjects.
  // STATUS: DONE
  private def flipSubjectTypes(graph: Graph, focusSubjectId: SubjectId): Graph = {
    val subjects = graph.subjects.mapValues { s =>
      if (s.id == focusSubjectId) {
        s.copy(subjectType = SubjectType.SingleSubjectType, externalType = None)
      } else {
        s.subjectType match {
          case SubjectType.MultiSubjectType =>
            s.copy(subjectType = SubjectType.MultiExternalSubjectType
              , externalType = Some(SubjectExternalType.InterfaceSubjectType)
            )
          case SubjectType.SingleSubjectType =>
            s.copy(subjectType = SubjectType.ExternalSubjectType
              , externalType = Some(SubjectExternalType.InterfaceSubjectType)
            )
          case SubjectType.ExternalSubjectType => s
          case SubjectType.MultiExternalSubjectType => s
        }
      }
    }
    val finalGraph = graph.copy(subjects = subjects)
    clearManualPositions(finalGraph)
  }

  // Get a list of removable subjects from a list of subjects to check for pruning.
  // The returned set of subject tuples is in the form Set[(RemovableSubject, FromSubject)]
  // STATUS: DONE
  private def removableSubjects(subs: Set[GraphSubject], graph: Graph): Set[(GraphSubject, GraphSubject)] = {
    val subjectMap = graph.subjects

    // RemoveCandidates are all subjects that have a degree of one (only communicate
    // with one subject) and do not contain any modal split or modal join states.
    // The last condition is not obvious but important since messages in parallel execution paths
    // act as a form of synchronization that would not be present in the case of TAUs instead
    // of send / receive states.
    // Removing this synchronization would confuse the verification engine and more importantly
    // the similarity analysis.
    // This condition could therefore be weakened to only remove those subjects that do not contain
    // receive states in parallel code paths, but this is out of scope for now.
    val removeCandidates = subs
      .filterNot { s =>
      // This removes all those candidates, that interact with this subject in
      // parallel branches introduced by modal split/join. This is important since
      // messages act as a form of synchronization that would be missing if
      val sMacro = s.macros("##main##")
      val edges = sMacro.edges
      val neighborMacros = degree(s).map(subjectMap).map(_.macros("##main##"))
      val modalBranches = neighborMacros.map(modalBranchNodes).map(_.filter(_.size > 1))
      modalBranches.exists(_.exists(_.map(_.exists { n =>
        lazy val targets = edges.filter(e => e.startNodeId == n.id).flatMap(_.target).map(_.subjectId)
        (n.isReceive || n.isSend) && targets.contains(s.id)
      }).count(t => t) > 1))
    }.flatMap { s =>
      val deg = degree(s)
      if (deg.size == 1) {
        Some((s, deg.head))
      } else {
        None
      }
    }

    removeCandidates.filter { st =>
      val (removeSubject, from) = st
      val remove = removeSubject.id
      val fromSubject = subjectMap(from)


      // Check that variables received from the removeSubject do not leave
      // the fromSubject. This also includes all variables that are in some way influenced
      // by any of the received variables.
      val receivedVariables = fromSubject.transitions
        .filter(t => t.isReceive && t.interactsWith(remove))
        .flatMap(_.saveToVariable)
      val allVars = withAffectedVariables(receivedVariables, fromSubject)

      val varsLeaving = fromSubject.transitions.flatMap { t =>
        if (t.isSend && !t.interactsWith(remove)) {
          t.edge.variableId.filter(allVars.contains)
        } else None
      }
      varsLeaving.isEmpty
    }.map { st =>
      val (removeSubject, from) = st
      val fromSubject = subjectMap(from)
      (removeSubject, fromSubject)
    }
  }

  /* Remove (prune / anonymize) a subject from another subject.
   * Remove and from subjects can be interchanged, except for the case that
   * both subjects communicate only with eachother. In this case, remove is removed
   * from the form subject.
   * This function also checks for preconditions, that is if the subject can safely be removed
   * from the graph and the communication pruned / anonymized.
   * The preconditions are:
   *   - removeSubject only communicates with the fromSubject
   *   - fromSubject does not save messages received from removeSubject in variables
   *     and relays these variables, directly or indirectly, to another subject
   *
   * STATUS: DONE
   */
  private def pruneSubject(remove: SubjectId, from: SubjectId, graph: Graph): Graph = {
    val fromSubject = graph.subjects(from)
    val removeSubject = graph.subjects(remove)

    // If removeSubject can not be pruned, do not modify the graph and return
    if (removableSubjects(Set(removeSubject), graph).isEmpty) {
      graph
    } else {
      // Actually remove all messages. Sets all edges and nodes that denote communication to
      // the subject to be removed to TAU.
      val trimmedSubjects = graph.subjects - remove
      val updatedSubject = fromSubject.copy(macros = fromSubject.macros.mapValues { m =>
        val relatedTransitions = fromSubject.transitions.filter(_.interactsWith(remove))
        m.copy(edges = m.edges.map { e =>
          if (relatedTransitions.map(_.edge).contains(e)) {
            tau(e)
          } else {
            e
          }
        }, nodes = m.nodes.mapValues { n =>
          if (relatedTransitions.map(_.fromNode).contains(n)) {
            tau(n)
          } else {
            n
          }
        })
      })
      val updatedSubjects = trimmedSubjects.updated(updatedSubject.id, updatedSubject)
      graph.copy(subjects = updatedSubjects)
    }
  }

  /* For the list of variables to be checked for this subject,
   * add all variables to this set that are in some way derived from
   * this set of variables, e.g. by being involved in variable manipulations.
   *
   * For example, given variable v1 to check, and a varMan operation in this
   * subject that combines v1 with v3 and saves it to v3, v3 will be added to
   * the set of variables to be checked. This check is then recursively
   * executed on this newly obtained set of variables, until no new variables
   * are found.
   * STATUS: DONE
   */
  private def withAffectedVariables(vars: Iterable[String], subject: GraphSubject): Set[String] = {
    @tailrec
    def recAddVars(vars1: Set[String], vars2: Set[String]): Set[String] = {
      if (vars2.isEmpty) {
        vars2 // No variables to check
      } else if (vars1 == vars2) {
        vars2 // No new variables added in this iteration
      } else {
        val vars3 = addVars(vars2)
        recAddVars(vars2, vars3)
      }
    }
    def addVars(vars: Set[String]): Set[String] = {
      vars ++ subject.transitions.flatMap { t =>
        if (t.fromNode.nodeType == StateType.VariableManipulationString && t.fromNode.varMan.isDefined) {
          val v = t.fromNode.varMan.get
          t.fromNode.varMan.flatMap { v =>
            if (vars.contains(v.var1Id) || vars.contains(v.var2Id)) {
              Some(v.storeVarId)
            } else None
          }
        } else {
          None
        }
      }.toSet
    }
    recAddVars(Set.empty, vars.toSet)
  }


  /*
   * Removes Choose Agent states in subject that target itself.
   * The steps in this removal are:
   *   - Gather a list of all choose agent states targeting this subject
   *   - Get the variableIDs these choose agent states save their channel in
   *   - For every of these variables, recursively call the removeVariable function on the
   *     subject for the current variable, and replace the current subject with the resulting subject
   *   - In case of errors (variable cannot be removed), ignore this variable and carry on
   *
   * The removeVariable function will:
   *  - remove the choose agent state
   *  - check that the variable does not leave the subject
   *  - chek that the variable is not processed in a varMan operation
   *  - remove the variable (channel) target of messages being sent to this variable
   *
   * STATUS: DONE
   */
  private def sanitizeChooseAgentStates(subject: GraphSubject): GraphSubject = {
    @tailrec
    def removeVariables(sub: GraphSubject, varList: List[SubjectId]): GraphSubject = {
      varList match {
        case Nil => sub
        case v :: vs =>
          removeVariable(v, sub) match {
            case None => removeVariables(sub, vs)
            case Some(s) => removeVariables(s, vs)
          }
      }
    }
    // Get all choose agent states targeting the current subject or the other subject
    // (the subject now merged into the current subject) and extract the variables
    val vars = subject.transitions.filter { t =>
      t.isChooseAgent && t.fromNode.chooseAgentSubject.contains(subject.id)
    }.flatMap(_.edge.variableId).toSet
    removeVariables(subject, vars.toList)
  }

  // Remove a single variable if it is not used in variable manipulations
  // or sent to other subjects. This will also remove variable targets, that is
  // variables used as channels.
  // This function is (only) intended to be used by the santizeChooseAgentStates function
  // and is quite unsafe to call otherwise.
  // Return type of None means the variable could not be removed (used in varMan, sent
  // to other subjects, etc.
  // STATUS: DONE
  private def removeVariable(varId: String, subject: GraphSubject): Option[GraphSubject] = {
    val allVars = withAffectedVariables(Seq(varId), subject)
    val variableLeavesSubject = subject.transitions.exists { t =>
      t.isSend && !t.interactsWith(subject.id) && t.edge.variableId.exists(v => allVars.contains(v))
    }
    if (variableLeavesSubject || allVars.size > 1) {
      None
    } else {
      val newMacros = subject.macros.mapValues { m =>
        val edges = m.edges
        val edgesMap = edges.map(e => (e.startNodeId, e)).toMap
        val newNodes = m.nodes.mapValues { n =>
          val e = if (n.isStart) {
            None
          } else {
            edgesMap.get(n.id)
          }
          val varUsedInVarMan = n.varMan.exists { vm =>
            allVars.contains(vm.var1Id) ||
              allVars.contains(vm.var2Id) ||
              allVars.contains(vm.storeVarId)
          }
          if (n.isVarMan && varUsedInVarMan) {
            tau(n)
          } else if (n.isChooseAgent && e.exists(e => e.variableId.exists(v => allVars.contains(v)))) {
            tau(n)
          } else if (n.variableId.exists(v => allVars.contains(v))) {
            n.copy(variableId = None)
          } else {
            n
          }
        }
        val newEdges = edges.map { e =>
          val n = m.nodes(e.startNodeId)
          val targetsVariable = e.target.exists(_.variableId.exists(v => allVars.contains(v)))
          if ((n.isReceive || n.isSend) && targetsVariable) {
            e.copy(target = e.target.map(_.copy(variableId = None)))
          } else {
            e
          }
        }
        m.copy(nodes = newNodes, edges = newEdges)
      }
      Some(subject.copy(macros = newMacros, variables = subject.variables -- allVars))
    }
  }

  // Wrap the current subject behavior of this subject in modal split/join statements.
  // The current implementation mandates that the behavior only has a single start state,
  // multiple end states are okay however.
  // This is a necessary step to merge two subjects into one by inserting the behavior of the subject
  // to be merged in the alternative branch of the modal split/join duo created by this function.
  // STATUS: DONE
  private def splitSubjectBehavior(subject: GraphSubject): (GraphNodeId, GraphNodeId, GraphSubject) = {
    // retrieve nodes and edges of the main macro. We only work on the main macro
    val mainMacro = subject.macros("##main##")
    val nodes = mainMacro.nodes
    val edges = mainMacro.edges

    // We only support a single start node at the moment. This is not a limitation of the algorithm
    // but a limitation of this implementation.
    // and modal join states would have to be introduced.
    val startNodeId = nodes.values.filter(_.isStart).head.id
    val endNodeIds = nodes.values.filter(_.isEnd).map(_.id).toSeq

    // Insert modal split as first state and make it the major start node.
    // Also make the previous start node a normal node instead and connect modal split
    // and the previous start node.
    val filteredNodes = nodes.values.flatMap { n =>
      if (n.isStart) {
        Some(n.id, n.copy(isStart = false, isMajorStartNode = false))
      } else if (n.isEnd) {
        None
      } else {
        Some(n.id, n)
      }
    }.toMap
    val nextNodeId = (nodes.keys.max + 1).toShort
    val modalSplitNodeId = nextNodeId
    val modalJoinNodeId = (nextNodeId + 1).toShort
    val newEndNodeId = (nextNodeId + 2).toShort
    val newNodes = filteredNodes + (
      modalSplitNodeId -> GraphNode(
        id = modalSplitNodeId
        , text = "Merged subjects split"
        , isStart = true
        , isMajorStartNode = true
        , nodeType = StateType.ModalSplitStateString)
      ) + (
      modalJoinNodeId -> GraphNode(
        id = modalJoinNodeId.toShort
        , text = "Merged subjects join"
        , nodeType = StateType.ModalJoinStateString)
      ) + (
      newEndNodeId -> GraphNode(
        id = newEndNodeId
        , text = "End"
        , isEnd = true
        , nodeType = StateType.EndStateString)
      )

    val newEdges = edges.map { e =>
      if (endNodeIds.contains(e.endNodeId)) {
        e.copy(endNodeId = modalJoinNodeId)
      } else {
        e
      }
    } :+ GraphEdge(
      startNodeId = modalSplitNodeId
      , endNodeId = startNodeId
      , text = ""
      , edgeType = "exitcondition"
    ) :+ GraphEdge(
      startNodeId = modalJoinNodeId
      , endNodeId = newEndNodeId
      , text = ""
      , edgeType = "exitcondition"
    )

    val newMainMacro = mainMacro.copy(edges = newEdges, nodes = newNodes)
    val newSubject = subject.copy(
      macros = subject.macros.updated("##main##", newMainMacro)
    )
    (modalSplitNodeId, modalJoinNodeId, newSubject)
  }


  // Insert from Subject into the Into subject, connecting the start node from the subject to be merged
  // to the start GraphNodeId, as well as settint the target of edges formerly targeting an end Node to
  // target the end GraphNodeId instead. End Nodes from the from Subject are therefore removed and
  // not merged into the into Subject.
  // All NodeIds of the merged nodes and edges are changed to seamlessly join the nodeIds of the IntoSubject.
  // Furthermore, all variable names and IDs from the from Subject are changed to be prefixed with the
  // from Subject ID.
  // STATUS: DONE
  private def insertSubject(from: GraphSubject, into: GraphSubject, start: GraphNodeId, end: GraphNodeId): GraphSubject = {
    val mainMacro = into.macros("##main##")
    val maxNodeId = mainMacro.nodes.keys.max
    val fromMainMacro = from.macros("##main##")
    val fromStartNode = fromMainMacro.nodes.values.filter(_.isStart).head

    // Create a mapping of old node IDs to new node IDs of the "into" subject
    val nodeIdMap = fromMainMacro.nodes.values.foldRight(Map.empty[GraphNodeId, GraphNodeId]) { (node, acc) =>
      if (acc.contains(node.id)) {
        acc
      } else if (node.isEnd) {
        acc + (node.id -> end)
      } else {
        val nextId = if (acc.isEmpty) {
          maxNodeId + 1
        } else {
          acc.values.max + 1
        }
        acc + (node.id -> nextId.toShort)
      }
    }

    def getOldStartNodes(nodeIds: Seq[Short]) = {
      nodeIds.flatMap{id =>
        if (fromMainMacro.nodes(id).isModalSplit) {
          fromMainMacro.edges.filter(_.startNodeId == id).map(_.endNodeId)
        } else {
          Seq(id)
        }
      }
    }
    val oldStartNodeIds = getOldStartNodes(fromMainMacro.nodes.values.find(_.isStart).map(_.id).toSeq)
    val deactivateNodeId = (Seq(nodeIdMap.keys.max, nodeIdMap.values.max).max + 1).toShort
    val endNodeId = into.macros("##main##").nodes.values.find(_.isEnd).get.id
    val startEndPair = Seq(
      Seq(endNodeId)
      , Stream.from(deactivateNodeId).map(_.toShort).take(oldStartNodeIds.size)
    ).flatten.zip(Stream.from(deactivateNodeId).map(_.toShort)).take(oldStartNodeIds.size)

    val renameVar = (varName: String) => {
      if (varName.isEmpty) {
        ""
      } else {
        s"${from.id}::$varName}"
      }
    }

    // add nodes and edges to the into subject, adjusting nodeIDs and prefixing variables with
    // the old subject's ID. We do not adjust conversations or macros states and IDs, as
    // theses S-BPM concepts are currently not very well supported and every example
    // in the Bachelor's Thesis this code belongs to does not use correlations or macros.
    val newNodes: Map[GraphNodeId, GraphNode] = fromMainMacro.nodes.foldRight(mainMacro.nodes) { (nodeT, acc) =>
      val (nodeId, node) = nodeT
      if (node.isEnd) {
        acc // End node gets replaced by modal join
      } else {
        val newNode = node.copy(
          id = nodeIdMap(node.id)
          , manualPositionOffsetX = None
          , manualPositionOffsetY = None
          , isStart = false // Modal split node will be the new start node
          , isMajorStartNode = false // see above
          , variableId = node.variableId.map(renameVar)
          , options = node.options.copy(nodeId = node.options.nodeId.map(o => nodeIdMap(o)))
          , varMan = node.varMan.map { varMan => varMan.copy(var1Id = renameVar(varMan.var1Id)
            , var2Id = renameVar(varMan.var2Id)
            , storeVarId = renameVar(varMan.storeVarId))
          }
        )
        acc + (nodeIdMap(nodeId) -> newNode)
      }
    }.flatMap { case (nodeId, node) =>
      if (node.isEnd) {
        val (Some(nextId), deactivateNodes) = oldStartNodeIds.foldLeft((Option.empty[Short], Seq.empty[GraphNode])) { case ((nId, acc), oldStartId) =>
          val id = nId match {
            case None => nodeId
            case Some(i) => i
          }
          val nextNId = nId.map(n => (n + 1).toShort).getOrElse(deactivateNodeId)
          val newAcc = acc :+ GraphNode(
              id = id
            , text = "deactivate"
            , options = GraphNodeOptions(nodeId = Some(nodeIdMap(oldStartId)))
            , nodeType = StateType.DeactivateStateString)
          (Some(nextNId), newAcc)
        }
        (deactivateNodes :+ node.copy(id = nextId.toShort)).map(n => (n.id, n))
      } else {
        Seq((nodeId, node))
      }
    }

    // now for the edges. Here we also have to add a edge from the modalSplit (start) node to
    // the former start node. Please note that multiple start nodes are explicitly not supported.
    val newEdges: Seq[GraphEdge] = (fromMainMacro.edges.foldRight(mainMacro.edges) { (edge, acc) =>
      acc :+ edge.copy(
        startNodeId = nodeIdMap(edge.startNodeId)
        , endNodeId = nodeIdMap(edge.endNodeId)
        , manualPositionOffsetLabelX = None
        , manualPositionOffsetLabelY = None
        , target = edge.target.map { t => t.copy(variableId = t.variableId.map(renameVar)) }
        , variableId = edge.variableId.map(renameVar)
      )
    } :+ GraphEdge(
      startNodeId = start
      , endNodeId = nodeIdMap(fromStartNode.id)
      , text = ""
      , edgeType = "exitcondition"
      , comment = Some(s"Branch from subject ${from.id}")
    ))  ++ startEndPair.map { case (from, to) =>
      GraphEdge(
        startNodeId = from
        , endNodeId = to
        , text = ""
        , edgeType = "exitcondition"
      )
    }

    // somewhat crude and possibly brakes the graph library as variable IDs (not names)
    // are not following the naming scheme "vVarId" (like v1, v2, v3) anymore...
    val newVariables = from.variables.foldRight(into.variables) { (variableT, acc) =>
      val (varId, variable) = variableT
      val newVarId = renameVar(varId)
      val newVarName = renameVar(varId)
      acc + (newVarId -> variable.copy(id = newVarId, name = newVarName))
    }

    val newMainMacro = mainMacro.copy(nodes = newNodes, edges = newEdges)
    into.copy(
      macros = into.macros.updated(newMainMacro.id, newMainMacro)
      , variables = newVariables
      , inputPool = addInputPools(from.inputPool, into.inputPool)
    )
  }

  // This is obviously a hack. Simply adding the input pools might make the
  // new process misbehave in certain conditions and verifiability is not
  // guaranteed. Together with the other aspects of inlining, merging two subjects
  // in a verified process will most likely make the resulting process fail verification.
  // However, we are not so much concerned with strict verification as we
  // are with minimal interfaces and similarity analysis
  // STATUS: Done
  private def addInputPools(p1: Short, p2: Short): Short = {
    if (p1 < 0) {
      p1
    } else if (p2 < 0) {
      p2
    } else {
      (p1 + p2).toShort
    }
  }

  // Remap all receive and send states in graph to target newTargetId instead of oldTargetId.
  // Also removes the sending to a variable / channel part of the target, since
  // this is no longer relevant in the context of a single subject.
  // STATUS: DONE
  private def remapMessages(graph: Graph, oldTargetId: SubjectId, newTargetId: SubjectId): Graph = {
    graph.copy(subjects = graph.subjects.mapValues { s =>
      s.copy(macros = s.macros.mapValues { m =>
        val newEdges = m.edges.map { e =>
          val node = m.nodes(e.startNodeId)
          if (node.isReceive || node.isSend) {
            if (e.target.map(_.subjectId).exists { id => id == newTargetId || id == oldTargetId }) {
              e.copy(target = e.target.map { t => t.copy(subjectId = newTargetId, variableId = None) })
            } else {
              e
            }
          } else {
            e
          }
        }
        val newNodes = m.nodes.mapValues { n =>
          if (n.isChooseAgent && n.chooseAgentSubject.contains(oldTargetId)) {
            n.copy(chooseAgentSubject = Some(newTargetId))
          } else {
            n
          }
        }
        m.copy(edges = newEdges, nodes = newNodes)
      })
    })
  }

  // Remove all custom positions on all edges and nodes of this graph
  // STATUS: DONE
  private def clearManualPositions(graph: Graph): Graph = {
    graph.copy(subjects = graph.subjects.mapValues { s =>
      s.copy(macros = s.macros.mapValues { m =>
        m.copy(edges = m.edges.map { e =>
          e.copy(manualPositionOffsetLabelX = None, manualPositionOffsetLabelY = None)
        },
          nodes = m.nodes.mapValues { n =>
            n.copy(manualPositionOffsetX = None, manualPositionOffsetY = None)
          })
      })
    })
  }

  private def validateSubject(subject: GraphSubject): Option[String] = {
    subject.macros.values.flatMap { m =>
      val linkedNodes = m.edges.flatMap(e => Seq(e.startNodeId, e.endNodeId))
      linkedNodes.flatMap { nId =>
        if (m.nodes.contains(nId)) {
          None
        } else {
          Some(nId)
        }
      }.headOption.map(id => s"node $id not found in macro ${m.id}")
    }.headOption
  }

  /*
   * Splits a macro into a list of a list of GraphNodes in parallel modal branches without the modal states.
   * For example, a simple macro without modal split / join would be a Seq(Seq(Set(State1, State2, ...)))
   * A macro with a regular state, a modal split with 3 branches, two of them with one state, the other with another
   * two-branched modal split with each two states looks like this:
   * Seq(Seq(Set(StartState)), Seq(Set(State1), Set(State2), Set()), Seq(Set(NestedL1, NestedL2), Set(NestedR1, NestedR2)))
   * One Set is empty since modal split / join states are removed, and this branch only contains modal split and join
   * states on the top level.
   * STATUS: DONE
   */
  private def modalBranchNodes(makro: GraphMacro): Seq[Seq[Set[GraphNode]]] = {
    val nodes = makro.nodes
    val edges = makro.edges
    def addNodes(current: GraphNode, depth: Int, acc: Set[Either[GraphNode, GraphNode]]): Set[GraphNode] = {
      val allAccIds = acc.map(_.merge).map(_.id)
      lazy val nextIds = edges.filter(e => e.startNodeId == current.id && !allAccIds.contains(e.endNodeId)).map(_.endNodeId)
      lazy val nextNodes = nextIds.map(id => nodes(id))
      lazy val next = nextNodes.head
      if (depth < 0) {
        acc.flatMap(_.right.toOption)
      } else if (current.isModalSplit) {
        val newAcc = nextNodes.flatMap(n => addNodes(n, 0, acc + Left(current))).toSet
        newAcc ++ addNodes(next, depth + 1, acc + Left(current))
      } else if (current.isModalJoin) {
        addNodes(next, depth - 1, acc + Left(current))
      } else {
        if (depth == 0) {
          if (current.isEnd) {
            (acc + Right(current)).flatMap(_.right.toOption)
          } else {
            nextNodes.flatMap(n => addNodes(n, depth, acc + Right(current))).toSet
          }
        } else {
          addNodes(next, depth, acc + Left(current))
        }
      }
    }
    val startNodes = Seq(nodes.values.filter(_.isStart).toSeq)
    val splitNodes = nodes.values.filter(_.isModalSplit).toSeq
    val afterSplitNodes = splitNodes.map(n => edges.filter(e => e.startNodeId == n.id).map(e => nodes(e.endNodeId)))
    (startNodes ++ afterSplitNodes).map(_.map(n => addNodes(n, 0, Set.empty)))
  }

  // get a list of subjectIds of the subjects this subject is in contact with
  // STATUS: DONE
  private def degree(subject: GraphSubject): Seq[SubjectId] = {
    subject.transitions.filter { t => t.isReceive || t.isSend }
      .flatMap(_.edge.target)
      .map(_.subjectId)
      .filter(_ != subject.id)
  }

  // Anonymize a GraphEdge
  // STATUS: DONE
  private def tau(edge: GraphEdge): GraphEdge = {
    edge.copy(target = None, text = "", transportMethod = Seq.empty)
  }

  // Anonymize a GraphNode
  // STATUS: DONE
  private def tau(node: GraphNode): GraphNode = {
    node.copy(text = "tau", nodeType = StateType.TauStateString, chooseAgentSubject = None, varMan = None)
  }
}
