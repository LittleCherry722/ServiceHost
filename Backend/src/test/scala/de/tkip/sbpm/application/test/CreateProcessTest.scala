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

object CreateProcessTest extends App {

  val processModel =
    ProcessModel(
      4,
      "Urlaub",
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

  def testProcessCreation() {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(5 seconds)
    val processInstanceActor = system.actorOf(Props(new ProcessInstanceActor(1, processModel)))

    processInstanceActor ! AddSubject(1, 2, "Superior")
    processInstanceActor ! AddSubject(1, 2, "Employee")

    println("send executerequest")
    processInstanceActor ! ExecuteRequest(1, 2)
    println("done")

    Thread.sleep(12000)
  }

  def testProcessAndSubjectCreation() {

    val system = ActorSystem("TextualEpassIos")
    val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
    val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

    implicit val timeout = Timeout(5 seconds)

    // Create the SubjectProvider for this user
    val future1 = subjectProviderManager ? CreateSubjectProvider()
    val userID: Int =
      Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

    println("User Created id: " + userID)

    // Create a Process using the ProcessModel
    val future2 = subjectProviderManager ? CreateProcess(userID, "my process", processModel)
    val processID: Int =
      Await.result(future2, timeout.duration).asInstanceOf[ProcessCreated].processID

    println("Process(Model) Created id: " + processID)

    // Execute the ProcessInstance
    val future3 = subjectProviderManager ? CreateProcessInstance(processID)
    val processInstanceID: Int =
      Await.result(future3, timeout.duration).asInstanceOf[ProcessInstanceCreated].processInstanceID

    println("ProcessInstance Executed id: " + processInstanceID)

    processManager ! ((processInstanceID, AddSubject(1, 2, "Employee")))

  }

  testProcessAndSubjectCreation()
}


