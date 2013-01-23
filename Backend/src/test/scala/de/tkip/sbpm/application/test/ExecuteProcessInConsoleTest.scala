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

object ExecuteProcessInConsoleTest {

  val processGraph =
    ProcessGraph(
      Array[Subject](
        Subject("Superior",
          Array[State](
            State("sup", "start", StartStateType, Array[Transition](StartTransition("sup.br1"))),
            State("sup.br1", "receive", ReceiveStateType, Array[Transition](Transition("BT Application", "Employee"))),
            State("sup.br1.br1", "act", ActStateType, Array[Transition](ActTransition("Approval"), ActTransition("Denial"))),
            State("sup.br1.br1.br1", "send approval", SendStateType, Array[Transition](Transition("Approval", "Employee", "The End"))),
            State("sup.br1.br1.br2", "send denial", SendStateType, Array[Transition](Transition("Denial", "Employee", "The End"))),
            State("The End", "end superior", EndStateType, Array[Transition]()))),
        Subject("Employee",
          Array[State](
            State("empl", "start", StartStateType, Array[Transition](StartTransition("empl.br1"))),
            State("empl.br1", "Fill out Application", ActStateType, Array[Transition](ActTransition("Done"))),
            State("empl.br1.br1", "Send Application", SendStateType, Array[Transition](Transition("BT Application", "Superior"))),
            State("empl.br1.br1.br1", "Receive", ReceiveStateType, Array[Transition](Transition("Approval", "Superior"), Transition("Denial", "Superior", "End"))),
            State("empl.br1.br1.br1.br1", "Make business trip", ActStateType, Array[Transition](ActTransition("Done", "End"))),
            State("End", "end state", EndStateType, Array[Transition]())))))

  val processModel = ProcessModel(1, "Urlaub", processGraph)

  /**
   * This class simulates the frontentinterfaceactor and runs by the console
   */
  private class FrontendSimulatorActor(subjectProviderManager: SubjectProviderManagerRef) extends Actor {

    def receive = {

      case a: ExecuteActionAnswer =>
        println("FE - action executed: " + a)
        Thread.sleep(100)
        subjectProviderManager ! GetAvailableActions(a.request.userID, a.request.processInstanceID)

      case AvailableActionsAnswer(request, available) =>
        for (action <- available) {
          executeAction(action)
        }

      case a: AvailableAction =>
        executeAction(a)

      case s =>
        println("FE received: " + s)
    }

    private def executeAction(avail: AvailableAction) {
      print(avail.processInstanceID + "/" + avail.subjectID + "/" + avail.stateID + " - ")
      avail.stateType match {
        case ActStateType =>
          var action = readLine("Execute one Action of " + avail.actionData.mkString("[", ", ", "]:"))
          while (!avail.actionData.contains(action)) {
            action = readLine("Invalid Input\nExecute one Action of " + avail.actionData.mkString("[", ", ", "]:"))
          }

          subjectProviderManager ! ExecuteAction(avail, action)
        case SendStateType =>
          val message = readLine("Please insert message: ")
          subjectProviderManager ! ExecuteAction(avail, message)
        case ReceiveWaitingStateType => {
          readLine("I am waiting for a message")
          //          subjectProviderManager ! GetAvailableActions(avail.userID, avail.processInstanceID)
        }
        case ReceiveStateType =>
          val ack = readLine("Got message " + avail.actionData.mkString(",") + ", ok?")
          subjectProviderManager ! ExecuteAction(avail, "")
        case EndStateType =>
          println("Subject terminated: " + avail.subjectID)
      }
    }
  }

  def testProcessAndSubjectCreationWithKonsole() {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))
    val console = system.actorOf(Props(new FrontendSimulatorActor(subjectProviderManager)))

    implicit val timeout = Timeout(5 seconds)

    // Create the SubjectProvider for this user
    val future1 = subjectProviderManager ? CreateSubjectProvider()
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

    println("User Created id: " + userID)

    // Create a Process using the ProcessModel
    val future2 = subjectProviderManager ? CreateProcess(userID, "my process", processGraph)
    val processID: Int =
      Await.result(future2, timeout.duration).asInstanceOf[ProcessCreated].processID

    println("Process(Model) Created id: " + processID)

    // Execute the ProcessInstance
    val future3 = subjectProviderManager ? CreateProcessInstance(processID)
    val processInstanceID: Int =
      Await.result(future3, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    println("ProcessInstance Executed id: " + processInstanceID)

    processManager ! ((processInstanceID, AddSubject(0, "Employee")))

    Thread.sleep(1500)

    subjectProviderManager.!(
      GetAvailableActions(userID, processInstanceID))(console)

  }

  def main(s: Array[String]) {
    testProcessAndSubjectCreationWithKonsole()
  }
}


