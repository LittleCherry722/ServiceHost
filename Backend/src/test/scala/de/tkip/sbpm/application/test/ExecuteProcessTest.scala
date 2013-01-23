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
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

object ExecuteProcessTest extends App {

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

  def testProcessAndSubjectCreation() {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(5 seconds)

    // Create the SubjectProvider for this user
    val future1 = subjectProviderManager ? CreateSubjectProvider()
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID
    val future12 = subjectProviderManager ? CreateSubjectProvider()
    val userID2: Int =
      Await.result(future12, timeout.duration).asInstanceOf[SubjectProviderCreated].userID
    val future13 = subjectProviderManager ? CreateSubjectProvider()
    val userID3: Int =
      Await.result(future13, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

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

    processManager ! ((processInstanceID, new AddSubject(userID2, "Employee")))

    Thread.sleep(1000)

    val future4 = subjectProviderManager ? GetAvailableActions(userID2, processInstanceID)
    val availableActions: Array[AvailableAction] =
      Await.result(future4, timeout.duration).asInstanceOf[AvailableActionsAnswer].availableActions

    println("AnswerMessage: " + availableActions(0) + " Possible actions: " + availableActions(0).actionData.mkString("/"))

    subjectProviderManager !
      ExecuteAction(
        userID2,
        processInstanceID,
        availableActions(0).subjectID,
        availableActions(0).stateID,
        availableActions(0).stateType,
        "Done")

    Thread.sleep(200)

    val future5 = subjectProviderManager ? GetAvailableActions(userID2, processInstanceID)
    val availableActions2: Array[AvailableAction] =
      Await.result(future5, timeout.duration).asInstanceOf[AvailableActionsAnswer].availableActions

    println("AnswerMessage: " + availableActions2(0) + " Possible actions: " + availableActions2(0).actionData.mkString("/"))

  }

  testProcessAndSubjectCreation()
}

