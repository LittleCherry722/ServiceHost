package de.tkip.sbpm.application.test

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.subject._
import spray.json._
import de.tkip.sbpm.rest.test.MyJSONTestGraph
import de.tkip.sbpm.ActorLocator
import ActorLocator._
import akka.actor.ActorContext
import akka.actor.ActorContext
import de.tkip.sbpm.persistence.PersistenceActor

/**
 * Creates all actors
 */
object createTestRunSystem {
  def apply(testPersistence: Boolean = true): (ActorSystem, ActorRef) = {
    val system = ActorSystem()

    system.actorOf(Props[PersistenceActor], ActorLocator.persistenceActorName)
    system.actorOf(Props[ContextResolverActor], ActorLocator.contextResolverActorName)
    system.actorOf(Props[ProcessManagerActor], ActorLocator.processManagerActorName)
    val subjectProviderManager = system.actorOf(Props[SubjectProviderManagerActor], ActorLocator.subjectProviderManagerActorName)

    (system, subjectProviderManager)
  }
}

object printHistory {
  def apply(history: History): String = {
    val sb: StringBuilder = new StringBuilder()

    sb ++= "History for process: %s (Instance: %d)"
      .format(history.processName, history.instanceId)
    sb ++= "\nStarttime: " + history.processStarted
    for (entry <- history.entries) {
      sb ++= "\n\t%s from: %s to: %s @%s, with message:"
        .format(entry.subject, entry.fromState, entry.toState, entry.timestamp)
      val messageOption = entry.message
      if (messageOption == None) {
        sb ++= "\n\t\tNo Message"
      } else {
        val message = messageOption.get
        sb ++= "\n\t\t [%d] from: %s to: %s channel: %s with content: %s"
          .format(message.id, message.from, message.to, message.messageType, message.data)
      }
    }
    sb ++= "\nEndtime: " + history.processEnded

    sb.toString
  }
}

object ExecuteProcessInConsoleTest {
  def createExecuteAction(available: AvailableAction, actionInput: ActionData): ExecuteAction =
      mixExecuteActionWithRouting(
        ExecuteAction(
          available.userID,
          available.processInstanceID,
          available.subjectID,
          available.stateID,
          available.stateType,
          actionInput))

  /**
   * This class simulates the frontentinterfaceactor and runs by the console
   */
  class FrontendSimulatorActor() extends Actor {

    private lazy val subjectProviderManagerActor =
      ActorLocator.subjectProviderManagerActor

    def receive = {

      case a: ExecuteActionAnswer => {
        println("FE - action executed: " + a)
        Thread.sleep(100)
        subjectProviderManagerActor ! GetAvailableActions(a.execute.userID, a.execute.processInstanceID)
      }

      case AvailableActionsAnswer(request, available) => {
        if (available.isEmpty) {
          println("FE - No actions are available anymore...")
          println("FE - Asking for history...")
          subjectProviderManagerActor ! GetHistory(request.userID, request.processInstanceID)
        }
        for (action <- available) {
          executeAction(action)
        }
      }

      case HistoryAnswer(_, history) => {
        println("\nExecutionHistory:\n")
        println(printHistory(history))
      }

      case a: AvailableAction =>
        executeAction(a)

      case message: AnswerAbleControlMessage =>
        subjectProviderManagerActor.forward(message)

      case s =>
        println("FE received: " + s)
    }

    

    private def executeAction(avail: AvailableAction) {
      print(avail.processInstanceID + "/" + avail.subjectID + "/" + avail.stateID + " - ")
      StateType.fromStringtoStateType(avail.stateType) match {
        case ActStateType =>
          var action = readLine("Execute one Action of " + avail.actionData.mkString("{", ", ", "}:"))
          //          while (!avail.actionData.contains(action)) {
          //            action = readLine("Invalid Input\nExecute one Action of " + avail.actionData.mkString("[", ", ", "]:"))
          //          }
          subjectProviderManagerActor ! createExecuteAction(avail, avail.actionData.find(_.text == action).get)
        case SendStateType =>
          val message = readLine("Please insert message: ")
          subjectProviderManagerActor ! createExecuteAction(avail, avail.actionData(0))
        case ReceiveStateType =>
          val ack = readLine("Got message " + avail.actionData.mkString(",") + ", ok?")
          subjectProviderManagerActor ! createExecuteAction(avail, avail.actionData(0))
        case EndStateType =>
          println("Subject terminated: " + avail.subjectID)
      }
    }
  }

  def testProcessAndSubjectCreationWithKonsole() {
    implicit val timeout = Timeout(5 seconds)

    val (system, _) = createTestRunSystem(false)
    val console = system.actorOf(Props(new FrontendSimulatorActor()))

    // Create the SubjectProvider for this user
    val future1 = console ? CreateSubjectProvider(1)
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID
    println("User Created id: " + userID)

    val processID = 1
    // Execute the ProcessInstance
    val future3 = console ? CreateProcessInstance(userID, processID)
    val processInstanceID: Int =
      Await.result(future3, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    println("ProcessInstance Executed id: " + processInstanceID)

    // increase the sleeping time, if it does not work
    Thread.sleep(2000)
    println("Start First Request.")

    console.!(GetAvailableActions(userID, processInstanceID))(console)

  }

  def main(s: Array[String]) {
    testProcessAndSubjectCreationWithKonsole()
  }
}
