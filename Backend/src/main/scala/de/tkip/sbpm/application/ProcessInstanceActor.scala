/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application

import java.util.{Date, UUID}

import akka.actor._
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.ProcessInstanceActor.NormalizeSubjectId
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.model.{Graph, Process, ProcessGraph, ProcessInstance, SubjectLike}
import de.tkip.sbpm.persistence.query._

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent._
import scala.concurrent.duration._

object ProcessInstanceActor {
  // This case class adds dynamically Subjects and Agents to this ProcessInstance
  case class RegisterSubjects(subjects: Map[SubjectID, SubjectLike])
  case class NormalizeSubjectId(subjectId: SubjectID) extends AnyVal
}

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends InstrumentedActor {
  import ProcessInstanceActor.RegisterSubjects

  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  import context.dispatcher
  implicit val timeout = Timeout(4 seconds)
  implicit val config = context.system.settings.config

  // this fields are set in the preStart, dont change them afterwards!!!
  private var id: ProcessInstanceID = _
  private val name = request.name
  private val startTime: Date = new Date()
  private val processID = request.processID
  private var processName: String = _
  private var persistenceGraph: Graph = _
  private var graph: ProcessGraph = _
  private var outgoingSubjectMap: Map[SubjectID, SubjectID] = _
  private var incomingSubjectMap: Map[SubjectID, SubjectID] = _
  // TODO: What exactly are "additional subjects"?
  private val additionalSubjects = MutableMap[SubjectID, SubjectLike]() // TODO: read all subjects from graph to avoid two subject maps

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  // this map stores all Subject(Container) with their IDs
  private val subjectMap = MutableMap[SubjectID, SubjectContainer]()

  val url = SystemProperties.akkaRemoteUrl
  private val processInstanceManger: ActorRef =
    // TODO not over context
    request.manager.getOrElse(context.actorOf(
      Props(new ProcessInstanceProxyManagerActor(request.processID, url, self)), "ProcessInstanceProxyManagerActor____" + UUID.randomUUID().toString))

  // this actor handles the blocking for answer to the user
  private val blockingHandlerActor = context.actorOf(Props[BlockingActor], "BlockingActor____" + UUID.randomUUID().toString)

  // this actor is used to exchange the subject ids for external input messages
  private var _proxyActor: Option[ActorRef] = None
  private def proxyActor = {
    lazy val pa = _proxyActor.getOrElse(context.actorOf(Props(new ProcessInstanceProxyActor(id, request.processID, incomingSubjectMap.map(_.swap), request)), "ProcessInstanceProxyActor____" + UUID.randomUUID().toString))
    _proxyActor = Some(pa)
    pa
  }

  override def preStart() {
    val persistenceActor = ActorLocator.persistenceActor

    // create combined futures
    val dataBaseAccessFuture = for {
      process <- (persistenceActor ?? Processes.Read.ById(processID)).mapTo[Option[Process]]
      processInstanceID <- (persistenceActor ?? ProcessInstances.Save(ProcessInstance(None, processID, process.get.activeGraphId.get, None))).mapTo[Option[Int]]
      graph <- (persistenceActor ?? Graphs.Read.ById(process.get.activeGraphId.get)).mapTo[Option[Graph]]

    } yield (process, processInstanceID, graph)

    // evaluate the Future
    val (process, idTemp, persistenceGraphTemp) = Await.result(dataBaseAccessFuture, timeout.duration)
    id = idTemp.get
    processName = process.get.name
    persistenceGraph = persistenceGraphTemp.get
    incomingSubjectMap = process.map(_.incomingSubjectMap).getOrElse(Map.empty)
    outgoingSubjectMap = process.map(_.outgoingSubjectMap).getOrElse(Map.empty)

    // parse the start-subjects into an Array
    val startSubjects: Iterable[SubjectID] = persistenceGraph.subjects.filter(_._2.isStartSubject.getOrElse(false)).keys
    // parse the graph into the internal structure
    graph = parseGraph(persistenceGraph)

    // TODO modify to the right version
    log.debug("All startSubjects: {}", startSubjects)
    log.debug("All subjects in this graph: {}", graph)
    for (startSubject <- startSubjects) {
      log.debug("StartSubject: {}", startSubject)
      // Create the subjectContainer
      subjectMap(startSubject) = createSubjectContainer(graph.subjects(startSubject))
      // the container shall contain a subject -> create
      subjectMap(startSubject).createSubject(request.userID)
    }

    // send processinstance created, when the block is closed
    blockingHandlerActor ! SendProcessInstanceCreated(request.userID)
  }

  def wrappedReceive = {

    case GetProxyActor =>
      sender !! proxyActor

    case _: SendProcessInstanceCreated =>
      trySendProcessInstanceCreated()

    case st: SubjectTerminated =>
      subjectMap(st.subjectID).handleSubjectTerminated(st)
      log.debug("process instance [" + id + "]: subject terminated " + st.subjectID)

    case sm: SubjectToSubjectMessage if graph.subjects.contains(sm.to) || additionalSubjects.contains(sm.to) =>
      /*
       * This message should originate from a SendStateActor of this process instance.
       * It's this ProcessInstanceActors job to forward the message to the target subject.
       * The target subject is determined only by the target subject ID of the message.
       * If the target subject has not already been initialized, it is now.
       */
      // Send the message to the container, it will deal with it
      log.info("Subject to Subject Message received. Updating subject map and forwarding message. Subject mapping now: {}", subjectMap)
      val subj: SubjectLike = if (graph.subjects.contains(sm.to)) { graph.subjects(sm.to) } else { additionalSubjects(sm.to) }
      subjectMap.getOrElseUpdate(sm.to, createSubjectContainer(subj)).send(sm)
      log.info("Subject Mapping after update: {}", subjectMap)

    case he: history.NewHistoryEntry =>
      he.process = history.NewHistoryProcessData(processName, id, name)
      context.parent.forward(he)

    case GetProcessInstanceManager => sender !! processInstanceManger

    case message: SubjectMessage if subjectMap.contains(message.subjectID) =>
      subjectMap(message.subjectID).send(message)

    case message: SubjectProviderMessage =>
      context.parent ! message

    // send forward if no subject has to be created else wait
    case message: ActionExecuted =>
      log.info("Executed " + message)
      createExecuteActionAnswer(message.ea)

    case answer: AnswerMessage =>
      context.parent.forward(answer)

    case message: ReadProcessInstance =>
      createReadProcessInstanceAnswer(message)

    case rs: RegisterSubjects =>
      registerAdditionalSubjects(rs.subjects)

    case NormalizeSubjectId(subjectId) =>
      val normalizedSubjectId = incomingSubjectMap.getOrElse(subjectId, subjectId)
      sender !! normalizedSubjectId

    case x => log.warning("ProcessInstanceActor did not handle: " + x)
  }

  private def registerAdditionalSubjects(subjects: Map[SubjectID, SubjectLike]): Unit = {
    additionalSubjects ++= subjects
  }

  private var sendProcessInstanceCreated = true
  private def createProcessInstanceData(actions: Array[AvailableAction]) =
    ProcessInstanceData(id, name, processID, processName, persistenceGraph, false, startTime, request.userID, actions)

  private def trySendProcessInstanceCreated() {

    if (sendProcessInstanceCreated) {
      val msg = AskSubjectsForAvailableActions(
        request.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ProcessInstanceCreated(request, self, createProcessInstanceData(actions)))

      context.parent ! msg

      sendProcessInstanceCreated = false
    }
  }

  private def createExecuteActionAnswer(req: ExecuteAction) {
    context.parent !
      AskSubjectsForAvailableActions(
        req.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ExecuteActionAnswer(req, createProcessInstanceData(actions)))
  }

  private def createReadProcessInstanceAnswer(req: ReadProcessInstance) {
    val msg = AskSubjectsForAvailableActions(
      req.userID,
      id,
      AllSubjects,
      (actions: Array[AvailableAction]) =>
        ReadProcessInstanceAnswer(req, createProcessInstanceData(actions)))

    context.parent ! msg

  }

  private def createSubjectContainer(subject: SubjectLike): SubjectContainer = {
    val subjectContainer = new SubjectContainer(
      subject,
      outgoingSubjectMap,
      processID,
      id,
      processInstanceManger,
      log,
      blockingHandlerActor,
      () => runningSubjectCounter += 1,
      () => runningSubjectCounter -= 1)
    log.info("New subject Container created: {}", subjectContainer)
    subjectContainer
  }

  private def getInterfacePartnerSubjects: Seq[SubjectLike] = {
    // TODO: additionalSubjects ?
    graph.subjects.values.filter(!_.external).toSeq
  }
}
