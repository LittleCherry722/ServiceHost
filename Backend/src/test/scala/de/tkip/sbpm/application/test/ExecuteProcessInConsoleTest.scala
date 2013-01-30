package de.tkip.sbpm.application.test

//import org.junit._
//import org.junit.Assert._
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
import de.tkip.sbpm.persistence.TestPersistenceActor
import ActorLocator._
import akka.actor.ActorContext
import akka.actor.ActorContext

/**
 * Creates all actors
 */
object createTestRunSystem {
  def apply(): ActorSystem = {
    val system = ActorSystem()

    system.actorOf(Props[TestPersistenceActor], ActorLocator.persistenceActorName)
    system.actorOf(Props[ContextResolverActor], ActorLocator.contextResolverActorName)
    system.actorOf(Props[ProcessManagerActor], ActorLocator.processManagerActorName)
    system.actorOf(Props[SubjectProviderManagerActor], ActorLocator.subjectProviderManagerActorName)

    system
  }
}

object ExecuteProcessInConsoleTest {

  val processGraph =
    ProcessGraph(
      Array[Subject](
        Subject("Superior",
          Array[State](
            State(0, "start", StartStateType, Array[Transition](StartTransition(1))),
            State(1, "receive", ReceiveStateType, Array[Transition](Transition("BT Application", "Employee", 2))),
            State(2, "act", ActStateType, Array[Transition](ActTransition("Approval", 3), ActTransition("Denial", 4))),
            State(3, "send approval", SendStateType, Array[Transition](Transition("Approval", "Employee", 5))),
            State(4, "send denial", SendStateType, Array[Transition](Transition("Denial", "Employee", 5))),
            State(5, "end superior", EndStateType, Array[Transition]()))),
        Subject("Employee",
          Array[State](
            State(0, "Start", StartStateType, Array[Transition](StartTransition(1))),
            State(1, "Fill out Application", ActStateType, Array[Transition](ActTransition("Done", 2))),
            State(2, "Send Application", SendStateType, Array[Transition](Transition("BT Application", "Superior", 3))),
            State(3, "Receive", ReceiveStateType, Array[Transition](Transition("Approval", "Superior", 4), Transition("Denial", "Superior", 5))),
            State(4, "Make business trip", ActStateType, Array[Transition](ActTransition("Done", 5))),
            State(5, "End employee", EndStateType, Array[Transition]())))))

  val processModel = ProcessModel(1, "Urlaub", processGraph)

  /**
   * This class simulates the frontentinterfaceactor and runs by the console
   */
  class FrontendSimulatorActor() extends Actor {

    private lazy val subjectProviderManagerActor =
      ActorLocator.subjectProviderManagerActor

    def receive = {

      case a: ExecuteActionAnswer =>
        println("FE - action executed: " + a)
        Thread.sleep(100)
        subjectProviderManagerActor ! GetAvailableActions(a.request.userID, a.request.processInstanceID)

      case AvailableActionsAnswer(request, available) =>
        if (available.isEmpty) {
          println("FE: No actions are available anymore")
        }
        for (action <- available) {
          executeAction(action)
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
          while (!avail.actionData.contains(action)) {
            action = readLine("Invalid Input\nExecute one Action of " + avail.actionData.mkString("[", ", ", "]:"))
          }
          subjectProviderManagerActor ! ExecuteAction(avail, action)
        case SendStateType =>
          val message = readLine("Please insert message: ")
          subjectProviderManagerActor ! ExecuteAction(avail, message)
        case ReceiveWaitingStateType => {
          readLine("I am waiting for a message...")
          // always ask again if there is a new action for this subject
          //          subjectProviderManager ! GetAvailableActions(avail.userID, avail.processInstanceID, avail.subjectID)
        }
        case ReceiveStateType =>
          val ack = readLine("Got message " + avail.actionData.mkString(",") + ", ok?")
          subjectProviderManagerActor ! ExecuteAction(avail, "")
        case EndStateType =>
          println("Subject terminated: " + avail.subjectID)
      }
    }
  }

  def testProcessAndSubjectCreationWithKonsole() {
    val parseJson = true
    var graph: ProcessGraph = null
    if (parseJson) {
      graph = parseGraph(MyJSONTestGraph.processGraph)
    } else {
      graph = processGraph
    }

    val system = createTestRunSystem()
    val console = system.actorOf(Props(new FrontendSimulatorActor()))

    implicit val timeout = Timeout(5 seconds)

    // Create the SubjectProvider for this user
    val future1 = console ? CreateSubjectProvider()
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID
    println("User Created id: " + userID)

    val processID = 2
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
