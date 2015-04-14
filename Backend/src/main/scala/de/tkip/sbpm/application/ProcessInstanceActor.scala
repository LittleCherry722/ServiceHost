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
import scala.collection.immutable
import scala.collection.mutable.{ Map => MutableMap }
import akka.actor.Status.Failure
import scalaj.http.{Http, HttpOptions}
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.{SubjectLike, ExternalSubject, Subject}
import de.tkip.sbpm.instrumentation.InstrumentedActor
import spray.json._
import DefaultJsonProtocol._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import de.tkip.sbpm.rest.JsonProtocol._
import de.tkip.sbpm.repository.RepositoryPersistenceActor.{AgentsMappingResponse, GetAgentsMapMessage}

object ProcessInstanceActor {
  /*
   * Variable, mesasge, message content definitions etc. are mainly as an example
   * of how variables could and should be structured to allow
   * sending to variables, sending variables, etc.
   *
   * The Main obstacles in using regular SubjectToSubjectMessages as a means of channel
   * transmissions are:
   *   - Current variables implementation is not compatible
   *   - Sending to Variables / Channels is not currently supported (sending to the sender of a message),
   *     in order to send to someone, this exact subject has to be in the graph, a subjectContainer has
   *     to be created etc. Ideally, sending to a graph subject that has not been instanciated, sending
   *     to an already existing graph subject, sending to a channel extracted from a message / variable
   *     and sending to an new or existing external subject should just consist of sending the same
   *     SubjectToSubjectMessage to an actorRef.
   *   - Variable manipulation states have to be implemented for recursively defined variables
   *   - Frontend needs support for sending variables to a subject, not only sending a message to a
   *     variable. This also needs support from the Backend though, as the Send state could and should
   *     just be auomatically executed withoud user interaction.
   */
  type Variable = Set[Message]

  case class Message(channel: Channel, content: MessageContent)

  sealed trait MessageContent {
    def channels : Set[Channel] = Set.empty
  }
  case class MessageSet(messages: Set[Message]) extends MessageContent {
    override def channels : Set[Channel] = messages.map(_.channel)
  }
  case class TextContent(content: String) extends MessageContent
  case class FileContent(content: Array[Byte]) extends MessageContent
  case object EmptyContent extends MessageContent

  case class Channel(subjectId: SubjectID, agent: Agent)

  // AgentMapping trait and AgentCandidates are not currently used, but might
  // be necessary for the blackbox / service host implementation
  sealed trait AgentMapping
  case class AgentCandidates(candidates: Set[Agent]) extends AgentMapping
  case class Agent(processId: Int,
                   address: AgentAddress,
                   subjectId: String) extends AgentMapping

  case class AgentAddress(ip: String, port: Int) {
    def toUrl = "@" + ip + ":" + port
  }

  type AgentsMap = immutable.Map[SubjectID, Agent]

  // This case class adds dynamically Subjects and Agents to this ProcessInstance
  case class RegisterSubjects(subjects: Map[SubjectID, SubjectLike], agentsMapping: AgentsMap)
}

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends InstrumentedActor {
  import ProcessInstanceActor.{ AgentsMap, Agent, AgentAddress, RegisterSubjects }

  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  import context.dispatcher
  implicit val timeout = Timeout(4 seconds)
  implicit val config = context.system.settings.config

  // this fields are set in the preStart, dont change them afterwards!!!
  private var id: ProcessInstanceID = _
  private val name = request.name
  private val startTime: Date = new Date()
  private var processID = request.processID
  private var processName: String = _
  private var persistenceGraph: Graph = _
  private var graph: ProcessGraph = _

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  private def isTerminated = runningSubjectCounter == 0
  // this map stores all Subject(Container) with their IDs
  private val subjectMap = MutableMap[SubjectID, SubjectContainer]()
  // dirty hack to discard every subject that is internal for this PE.
  // TODO Should much rather compare the Graph and subject URLs,
  // as one PE could, in theory, implement its own interface.
  private var agentsMap = request.agentsMap // TODO: Mutable ?

  val url = SystemProperties.akkaRemoteUrl
  private val processInstanceManger: ActorRef =
    // TODO not over context
    request.manager.getOrElse(context.actorOf(
      Props(new ProcessInstanceProxyManagerActor(request.processID, url, self)), "ProcessInstanceProxyManagerActor____" + UUID.randomUUID().toString()))

  // this actor handles the blocking for answer to the user
  private val blockingHandlerActor = context.actorOf(Props[BlockingActor], "BlockingActor____" + UUID.randomUUID().toString)

  // this actory is used to exchange the subject ids for external input messages
  private lazy val proxyActor = context.actorOf(Props(new ProcessInstanceProxyActor(id, request.processID, graph, request)), "ProcessInstanceProxyActor____" + UUID.randomUUID().toString())

  override def preStart() {
    log.debug("subject mapping: {}", agentsMap)

//    try {
      val persistenceActor = ActorLocator.persistenceActor

      // get the process
      val processFuture = (persistenceActor ?? Processes.Read.ById(processID)).mapTo[Option[Process]]

      val process = Await.result(processFuture, timeout.duration);

      // save this process instance in the persistence
      val processInstanceIDFuture = (persistenceActor ?? ProcessInstances.Save(ProcessInstance(None, processID, process.get.activeGraphId.get, None))).mapTo[Option[Int]]

      // get the corresponding graph
      val graphFuture = (persistenceActor ?? Graphs.Read.ById(process.get.activeGraphId.get)).mapTo[Option[Graph]]

      // create combined futures
      val dataBaseAccessFuture = for {
        processInstanceID <- processInstanceIDFuture
        graph <- graphFuture
      } yield (processInstanceID, graph)

      // evaluate the Future
      val (idTemp, persistenceGraphTemp) = Await.result(dataBaseAccessFuture, timeout.duration)
      id = idTemp.get
      processName = process.get.name
      persistenceGraph = persistenceGraphTemp.get

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
//    } catch {
//      case e: NoSuchElementException => {
//        blockingHandlerActor ! SendProcessInstanceCreated(request.userID)
//        request.sender !
//          Failure(new Exception("ProcessInstance creation failed, required " +
//            "resource does not exists."))
//      }

      // TODO processInstanceManger ! Register....
//    }
  }

  def wrappedReceive = {

    case GetProxyActor => {
      sender !! proxyActor
    }

    case _: SendProcessInstanceCreated => {
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      log.debug("process instance [" + id + "]: subject terminated " + st.subjectID)

      if (isTerminated && false) { // deactivated, see SBPM-1009
        log.debug("process instance [" + id + "] is going to terminate")
        val terminate = ProcessInstanceTerminated(id)
        context.parent ! terminate
        log.debug("process instance [" + id + "] terminates ")
        context.stop(self)
      }

    }

    case sm: SubjectToSubjectMessage if (graph.subjects.contains(sm.to)) => {
      val to = sm.to
      // Send the message to the container, it will deal with it
      log.info("Subject to Subject Message received. Updating subject map and forwarding message. Subject mapping now: {}", subjectMap)
      val subj: SubjectLike = graph.subjects(to)
      lazy val newSubjectContainer = createSubjectContainer(subj)
      subjectMap.getOrElseUpdate(to, newSubjectContainer)
      log.info("Subject Mapping after update: {}", subjectMap)
      subjectMap(to).send(sm)
    }

    case he: history.NewHistoryEntry => {
      he.process = history.NewHistoryProcessData(processName, id, name)
      context.parent.forward(he)
    }

    case SetAgentForSubject(subjectId, agent) => {
      this.agentsMap = this.agentsMap ++ Map(subjectId -> agent)
    }

    case message: SubjectMessage if subjectMap.contains(message.subjectID) => {
      subjectMap(message.subjectID).send(message)
    }

    case message: SubjectProviderMessage => {
      context.parent ! message
    }

    // send forward if no subject has to be created else wait
    case message: ActionExecuted => {
      log.info("Executed " + message)
      createExecuteActionAnswer(message.ea)
    }

    case answer: AnswerMessage => {
      context.parent.forward(answer)
    }

    case message: ReadProcessInstance => {
      createReadProcessInstanceAnswer(message)
    }

    case message: GetAgentsList => {
      log.info("GetAgentsList: " + message)
      val mappingResponse = GetAgentsListResponse(createSubjectMapping(message.processId, message.url).toMap)
      sender !! mappingResponse
    }

    case rs: RegisterSubjects => {
      graph = ProcessGraph(graph.subjects ++ rs.subjects)
      addAgentsMapping(rs.agentsMapping)
    }

    case x => log.warning("ProcessInstanceActor did not handle: " + x)
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
    val maybeAgent = subject match {
      case extSub: ExternalSubject => {
        val externalSubject = externalSubjectAgent(extSub)
        log.debug("Creating new external subject container for subject: {} - {}", subject.id, externalSubject)
        Some(externalSubject)
      }
      case _ => {
        None
      }
    }

    val subjectContainer = new SubjectContainer(
      subject,
      processID,
      id,
      processInstanceManger,
      log,
      blockingHandlerActor,
      maybeAgent,
      () => runningSubjectCounter += 1,
      () => runningSubjectCounter -= 1)
    log.info("New subject Container created: {}", subjectContainer)
    subjectContainer
  }

  private def externalSubjectAgent(subject: ExternalSubject): Agent = {
    log.info("externalSubjectAgent: " + subject)
    log.info("externalSubjectType: " + subject.externalType)
    // If an agent for this subject exists, use it.
    agentsMap.get(subject.id) match {
      case Some(agent) => agent
      case None => {
        if (subject.externalType == Some("external")) {
          addExternalAgent(subject)
          externalSubjectAgent(subject)
        }
        else {
          log.error("Agent {} not available! Current Mapping: {}", subject.id, agentsMap)
          throw new Exception(s"Agent ${subject.id} not available. Mapping available: $agentsMap")
        }
      }
    }
  }

  private def addExternalAgent(subject: ExternalSubject) = {
    val ownAddress = AgentAddress(ip = SystemProperties.akkaRemoteHostname
      , port = SystemProperties.akkaRemotePort)
    subject.relatedProcessId match {
      case Some(relProcessId) => {
        val agent = new Agent(relProcessId,ownAddress, subject.id)
        agentsMap = agentsMap + (subject.id -> agent)
        log.debug("Added agent for external subject: {}", subject.id)
      }
      case None => throw new Exception(s"ExternalSubject without related process: ${subject.id}")
    }
  }

  private def addAgentsMapping(mapping: AgentsMap): Unit = {
    val mutableAgentsMap: MutableMap[SubjectID, Agent] = MutableMap() ++ this.agentsMap

    for ((subject, agent) <- mapping) {
      mutableAgentsMap(subject) = agentsMap.getOrElse(subject, agent)
    }

    this.agentsMap = mutableAgentsMap.toMap
  }



  private def createSubjectMapping(processId: ProcessID, url: String): Map[SubjectID, Agent] = {
    log.debug("create subject mapping for {}@{}", processId, url)

    // Own address is just the akka port map
    val ownAddress = AgentAddress(ip = SystemProperties.akkaRemoteHostname
      , port = SystemProperties.akkaRemotePort)
    // to the current agents map, add every subject from the current process that communitcates with
    // an interface with our own agents information.
    // This is important because every PE we communicate with
    // a) Should be able to identify the initial PE the message was sent from, especially
    //    if it the message flow is like this: OwnPE -> A -> B -> OwnPE
    // b) Should also get mapping information we got previously from other PEs
    getInterfacePartnerSubjects.foldLeft(agentsMap) { (mapping: AgentsMap, subject: SubjectLike) =>
      val agent = Agent(processId = processId
        , address = ownAddress
        , subjectId = subject.id)
      mapping + (subject.id -> agent)
    }
  }

  private def getInterfacePartnerSubjects: Seq[SubjectLike] = {
    // TODO: this function has not been changed after additionalSubjects was merged with graphs.
    //  That means, with the new version, the returned sequence might contain SubjectLikes which
    //  would not have been contained in the previous version.
    graph.subjects.map(_._2).filter(!_.external).toSeq
  }
}
