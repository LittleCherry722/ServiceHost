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
import scala.collection.mutable.Buffer
import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.Future._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.ProcessGraph
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.Process
import de.tkip.sbpm.model.ProcessInstance
import de.tkip.sbpm.model.Graph
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.persistence._
import akka.event.Logging
import scala.collection.mutable.SortedSet
import scala.collection.mutable.Set
import scala.collection.mutable.LinkedList
import scala.collection.mutable.Map
import ExecutionContext.Implicits.global
import akka.actor.Status.Failure
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.application.subject.misc._
import de.tkip.sbpm.model.SubjectLike

// represents the history of the instance
case class History(
  var processName: String,
  instanceId: ProcessInstanceID,
  var processStarted: Option[Date] = None, // None if not started yet
  var processEnded: Option[Date] = None, // None if not started or still running
  entries: Buffer[history.Entry] = ArrayBuffer[history.Entry]()) // recorded state transitions in the history

/**
 * instantiates SubjectActor's and manages their interactions
 */
class ProcessInstanceActor(request: CreateProcessInstance) extends Actor {
  private val logger = Logging(context.system, this)
  // This case class is to add Subjects to this ProcessInstance
  private case class AddSubject(userID: UserID, subjectID: SubjectID)

  implicit val timeout = Timeout(4 seconds)

  // this fields are set in the preStart, dont change them afterwards!!!
  private var id: ProcessInstanceID = _
  private val processID = request.processID
  private var processName: String = _
  private var persistenceGraph: Graph = _
  private var graph: ProcessGraph = _

  // whether the process instance is terminated or not
  private var runningSubjectCounter = 0
  private def isTerminated = runningSubjectCounter == 0
  // this map stores all Subject(Container) with their IDs 
  private val subjectMap = collection.mutable.Map[SubjectID, SubjectContainer]()

  private val processInstanceManger =
    request.manager.getOrElse(context.actorOf(
      Props(new ProcessInstanceManagerActor(request.userID, id, self))))

  // recorded transitions in the subjects of this instance
  // every subject actor has to report its transitions by sending
  // history.Entry messages to this actor
  private val executionHistory = History(processName, id, Some(new Date()))
  // provider actor for debug payload used in history's debug data
  private lazy val debugMessagePayloadProvider = context.actorOf(Props[DebugHistoryMessagePayloadActor])

  // this actor handles the blocking for answer to the user
  private val blockingHandlerActor = context.actorOf(Props[BlockingActor])

  override def preStart() {
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

      executionHistory.processName = processName

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
    case _: SendProcessInstanceCreated => {
      trySendProcessInstanceCreated()
    }

    case st: SubjectTerminated => {
      subjectMap(st.subjectID).handleSubjectTerminated(st)

      logger.debug("process instance [" + id + "]: subject terminated " + st.subjectID)
      if (isTerminated) {
        // log end time in history
        executionHistory.processEnded = Some(new Date())
      }
    }

    case sm: SubjectToSubjectMessage if (graph.subjects.contains(sm.to)) => {
      val to = sm.to
      // Send the message to the container, it will deal with it
      subjectMap.getOrElseUpdate(to, createSubjectContainer(graph.subjects(to))).send(sm)
    }

    // add an entry to the history
    // (should be called by subject actors when a transition occurs)
    case he: history.Entry => {
      executionHistory.entries += he
    }

    case he: history.NewEntry => {
      he.processInstanceId = id
      he.processName = processName
      context.parent.forward(he)
    }

    // return current process instance history
    case msg: GetHistory => {
      sender ! {
        if (msg.isInstanceOf[Debug]) {
          HistoryAnswer(msg, HistoryTestData.generate(processName, id)(debugMessagePayloadProvider))
        } else {
          HistoryAnswer(msg, executionHistory)
        }
      }
    }

    case message: GetSubjectAddr => {
        subjectMap
          .getOrElseUpdate(
            message.subjectId,
            createSubjectContainer(graph.subjects((message.subjectId))))
          .send(sender, message)
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
      context.parent.forward(answer)
    }

    case message: ReadProcessInstance => {
      createReadProcessInstanceAnswer(message)
    }
  }

  private var sendProcessInstanceCreated = true
  private def createProcessInstanceData(actions: Array[AvailableAction]) =
    ProcessInstanceData(id, processID, persistenceGraph, false, executionHistory, actions)
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
    new SubjectContainer(
      subject,
      processID,
      id,
      processInstanceManger,
      logger,
      blockingHandlerActor,
      () => runningSubjectCounter += 1,
      () => runningSubjectCounter -= 1)
  }
}
