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

import java.util.Date
import scala.concurrent._
import scala.concurrent.duration._
import java.util.UUID
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.ProcessInstance
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject._
import akka.event.Logging
import scala.collection.mutable.Map
import akka.actor.Status.Failure
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.SubjectLike
import de.tkip.sbpm.model.ExternalSubject

case class MappingInfo(subjectId: SubjectID, processId: ProcessID, address: String)

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends Actor {
  private val logger = Logging(context.system, this)
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

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  private def isTerminated = runningSubjectCounter == 0
  // this map stores all Subject(Container) with their IDs 
  private val subjectMap = collection.mutable.Map[SubjectID, SubjectContainer]()

  val url = SystemProperties.akkaRemoteUrl
  private val processInstanceManger: ActorRef =
    // TODO not over context
    request.manager.getOrElse(context.actorOf(
      Props(new ProcessInstanceProxyManagerActor(request.processID, url, self)), "ProcessInstanceProxyManagerActor____" + UUID.randomUUID().toString()))

  // this actor handles the blocking for answer to the user
  private val blockingHandlerActor = context.actorOf(Props[BlockingActor], "BlockingActor____" + UUID.randomUUID().toString())

  // this actory is used to exchange the subject ids for external input messages
  private lazy val proxyActor = context.actorOf(Props(new ProcessInstanceProxyActor(id, request.processID, graph, request)), "ProcessInstanceProxyActor____" + UUID.randomUUID().toString())

  override def preStart() {
    logger.debug("subject mapping: {}", request.subjectMapping)

    try {
      // TODO schoener machen
      val dataBaseAccessFuture = for {
        // get the process
        processFuture <- (ActorLocator.persistenceActor ?
          Processes.Read.ById(processID)).mapTo[Option[Process]]
        // save this process instance in the persistence
        processInstanceIDFuture <- (ActorLocator.persistenceActor ?
          ProcessInstances.Save(ProcessInstance(None, processID, processFuture.get.activeGraphId.get, None)))
          .mapTo[Option[Int]]

        // get the corresponding graph
        graphFuture <- (ActorLocator.persistenceActor ?
          Graphs.Read.ById(processFuture.get.activeGraphId.get)).mapTo[Option[Graph]]
      } yield (processInstanceIDFuture.get, processFuture.get.name, graphFuture.get)
      // evaluate the Future
      val (idTemp, processNameTemp, graphTemp) =
        Await.result(dataBaseAccessFuture, timeout.duration)
      id = idTemp
      processName = processNameTemp
      persistenceGraph = graphTemp

      // parse the start-subjects into an Array
      val startSubjects: Iterable[SubjectID] = graphTemp.subjects.filter(_._2.isStartSubject.getOrElse(false)).keys
      // parse the graph into the internal structure
      graph = parseGraph(graphTemp)

      // TODO modify to the right version
      for (startSubject <- startSubjects) {
        // Create the subjectContainer
        subjectMap(startSubject) = createSubjectContainer(graph.subjects(startSubject))
        // the container shall contain a subject -> create
        subjectMap(startSubject).createSubject(request.userID)
      }
      // send processinstance created, when the block is closed
      blockingHandlerActor ! SendProcessInstanceCreated(request.userID)
    } catch {
      case e: NoSuchElementException => {
        request.sender !
          Failure(new Exception("ProcessInstance creation failed, required " +
            "resource does not exists."))
      }

      // TODO processInstanceManger ! Register....
    }
  }

  def receive = {

    case GetProxyActor => {
      sender ! proxyActor
    }

    case _: SendProcessInstanceCreated => {
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      logger.debug("process instance [" + id + "]: subject terminated " + st.subjectID)
    }

    case sm: SubjectToSubjectMessage if (graph.subjects.contains(sm.to)) => {
      val to = sm.to
      // Send the message to the container, it will deal with it
      subjectMap.getOrElseUpdate(to, createSubjectContainer(graph.subjects(to))).send(sm)
    }

    case he: history.NewHistoryEntry => {
      he.process = history.NewHistoryProcessData(processName, id, name)
      val traceLogger = Logging(context.system, this)
      traceLogger.debug("TRACE: from " + this.self + " to " + context.parent + " " + he.toString)
      context.parent.forward(he)
    }

    case message: SubjectMessage if (subjectMap.contains(message.subjectID)) => {
      subjectMap(message.subjectID).send(message)
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    // send forward if no subject has to be created else wait
    case message: ActionExecuted => {
      System.err.println("Executed " + message)
      createExecuteActionAnswer(message.ea)
    }

    case answer: AnswerMessage => {
      val traceLogger = Logging(context.system, this)
      traceLogger.debug("TRACE: from " + this.self + " to " + context.parent + " " + answer.toString)
      context.parent.forward(answer)
    }

    case message: ReadProcessInstance => {
      createReadProcessInstanceAnswer(message)
    }

    case message: GetSubjectMapping => {
      sender ! SubjectMappingResponse(createSubjectMapping(message.processId, message.url).toMap)
    }
  }

  private var sendProcessInstanceCreated = true
  private def createProcessInstanceData(actions: Array[AvailableAction]) =
    ProcessInstanceData(id, name, processID, processName, persistenceGraph, false, startTime, request.userID, actions)
  private def trySendProcessInstanceCreated() {

    if (sendProcessInstanceCreated) {
      context.parent !
        AskSubjectsForAvailableActions(
          request.userID,
          id,
          AllSubjects,
          (actions: Array[AvailableAction]) =>
            ProcessInstanceCreated(request, self, createProcessInstanceData(actions)))
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
    context.parent !
      AskSubjectsForAvailableActions(
        req.userID,
        id,
        AllSubjects,
        (actions: Array[AvailableAction]) =>
          ReadProcessInstanceAnswer(req, createProcessInstanceData(actions)))
  }

  private def createSubjectContainer(subject: SubjectLike): SubjectContainer = {
    val optionalId = if (subject.external) {
      Some(externalSubjectMapping(subject.asInstanceOf[ExternalSubject]))
    } else {
      None
    }

    new SubjectContainer(
      subject,
      processID,
      id,
      processInstanceManger,
      logger,
      blockingHandlerActor,
      optionalId,
      () => runningSubjectCounter += 1,
      () => runningSubjectCounter -= 1)
  }

  private def externalSubjectMapping(subject: ExternalSubject): MappingInfo = {
    if (request.subjectMapping.contains(subject.id)) {
      request.subjectMapping(subject.id)
    } else {
      MappingInfo(subject.relatedSubjectId.get, subject.relatedProcessId.get, subject.url.get)
    }
  }

  private def createSubjectMapping(processId: ProcessID, url: String): Map[SubjectID, MappingInfo] = {
    logger.debug("create subject mapping for {}@{}", processId, url)

    import scala.collection.mutable.{ Map => MutableMap }
    val subjectMapping = MutableMap[SubjectID, MappingInfo]()
    for (subject <- graph.subjects.values if subject.external) {
      val externalSubject = subject.asInstanceOf[ExternalSubject]

      if (externalSubject.relatedProcessId.exists(_ == processId) && externalSubject.url.exists(_ == url)) {
        val connectedSubject = findConnectedSubject(externalSubject.id)
        val ownUrl = SystemProperties.akkaRemoteUrl

        logger.debug("found connect subject {} for subject {}", connectedSubject.id, externalSubject.id)

        val mappingA = (externalSubject.relatedSubjectId.get -> MappingInfo(externalSubject.id, processID, ownUrl))
        val mappingB = (externalSubject.relatedInterfaceId.get -> MappingInfo(connectedSubject.id, processID, ownUrl))

        subjectMapping += mappingA
        subjectMapping += mappingB
      }
    }
    subjectMapping
  }

  private def findConnectedSubject(subjectId: SubjectID): SubjectLike = {
    val subject = persistenceGraph.subjects(subjectId)
    val mainMacro = subject.macros("##main##")

    val randomSendEdge = mainMacro.edges.find(_.target.isDefined)

    randomSendEdge match {
      case Some(edge) => graph.subjects(edge.target.get.subjectId)
      case None       => throw new IllegalStateException("could not find connected subject for subjectID " + subjectId)
    }
  }
}
