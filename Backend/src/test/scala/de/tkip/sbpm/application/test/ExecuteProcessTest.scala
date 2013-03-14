package de.tkip.sbpm.application.test

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import ExecuteProcessInConsoleTest.createExecuteAction
import akka.testkit.TestActorRef
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.ActorLocator
import org.scalatest.FunSuite

class ExecuteProcessTest extends FunSuite {
  implicit val timeout = Timeout(5 seconds)

  abstract class TestExectionActor(userID: UserID, processInstanceID: ProcessInstanceID) extends Actor {

    case class TestActionMatching(stateID: StateID, stateType: StateType, actionData: Array[String])
    private lazy val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor

    val subjectID: SubjectID

    val executionData: Array[(TestActionMatching, String)] // AvailableAction, actionInput

    def receive = {
      case _ =>
    }

    protected def assertAction(matching: TestActionMatching, action: AvailableAction) {

      if (matching == null) {
        return
      }

      val stateType = matching.stateType.toString()

      action match {
        case AvailableAction(userID, processInstanceID, subjectID, matching.stateID, stateName, stateType, data) => {
          if (matching.actionData.sameElements(data)) {
            println(">>>>>>>>> Action does match")
          } else {
            println("<<<<<<<<< ACTION DOES NOT MATCH")
            noError = false
          }
        }
        case _ => {
          println("<<<<>>>>> ACTION DOES NOT MATCH")
          noError = false
        }
      }

    }

    protected def askForActions(): Array[AvailableAction] = {
      while (true) {
        Thread.sleep(50)
        val future = subjectProviderManagerActor ? GetAvailableActions(userID, processInstanceID)
        val actions = Await.result(future, timeout.duration).asInstanceOf[AvailableActionsAnswer].availableActions
        if (!actions.isEmpty) {
          return actions
        }
      }
      null
    }

    protected def askForAction(): AvailableAction = {
      var action: AvailableAction = null

      while (action == null) {
        Thread.sleep(50)
        val future = subjectProviderManagerActor ? GetAvailableActions(userID, processInstanceID, subjectID)
        val actions = Await.result(future, timeout.duration).asInstanceOf[AvailableActionsAnswer].availableActions
        if (!actions.isEmpty) {
          action = actions(0)
        }
      }
      action
    }

    protected def executeAction(action: ExecuteAction) {
      val future = subjectProviderManagerActor ? action
      Await.ready(future, timeout.duration)
    }
  }

  //  class Manager(userID: UserID, processInstanceID: ProcessInstanceID) extends TestExectionActor(userID, processInstanceID) {
  //
  //  }

  class Purchaser(userID: UserID, processInstanceID: ProcessInstanceID) extends TestExectionActor(userID, processInstanceID) {

    val subjectID: SubjectID = "Subj1"

    val executionData =
      Array[(TestActionMatching, String)](
        (TestActionMatching(0, ActStateType, Array("Done")), "Done"),
        (TestActionMatching(1, SendStateType, Array()), "The Message content"),
        (TestActionMatching(2, ActStateType, Array("Await Denial", "Await Accept")), "Await Denial"))

    private lazy val subjectProviderManagerActor = ActorLocator.subjectProviderManagerActor

    for ((matching, actionInput) <- executionData) {
      val action = askForAction()
      assertAction(matching, action)
      println("action: " + action.stateType + action.actionData.mkString(" data: ", ", ", ">"))
      executeAction(createExecuteAction(action, null)) //TODO nicht null sondern sinnvoll
    }
    val action = askForAction()
    println("next action: " + action.stateType + action.actionData.mkString(" data: ", ", ", ">"))

    //    var action = askForAction()
    //
    //    println("action: " + action.stateType + action.actionData.mkString(" data: ", ", ", ">"))
    //    println(AvailableAction(userID, processInstanceID, "Subj1", 0, ActStateType.toString(), Array("Done")) == action)
    //    val a = ActStateType.toString()
    //    action match {
    //      case AvailableAction(userID, processInstanceID, "Subj1", 0, a, Array("Done")) => println("-.----y")
    //      case _ => println("fffffffffffffff2")
    //    }
    //    println(action)
    //    println(AvailableAction(userID, processInstanceID, "Subj1", 0, ActStateType.toString(), Array("Done")))
    //    executeAction(ExecuteAction(action, "Done"))
    //    action = askForAction()
    //    println("action: " + action.stateType + action.actionData.mkString(" data: ", ", ", ">"))
    //    executeAction(ExecuteAction(action, "Buy somethin from store."))

  }

  var noError: Boolean = true

  test("test process execution approval path") {
    noError = true
    val (system, subjectProviderManagerActor) = createTestRunSystem()

    var future = subjectProviderManagerActor ? CreateSubjectProvider(1)
    val userID = Await.result(future, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

    future = subjectProviderManagerActor ? CreateProcessInstance(userID, 2)
    val processInstanceID = Await.result(future, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    val purchaser = system.actorOf(Props(new Purchaser(userID, processInstanceID)))

    Thread.sleep(2000)
    println("Error: ---" + noError)
    assert(noError)
  }
}
