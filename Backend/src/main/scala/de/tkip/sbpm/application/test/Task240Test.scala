package de.tkip.sbpm.application.test

import akka.actor._
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._

/**
 * kleiner Test der SubjectManagerProvider und ProcessManager instanziert,
 * dann Subjekte und Verhalten hinzufügt und
 * eine Statusabfrage sendet, die StateAusführung jedes Subjects erzwingt
 *
 * Beispiel wurde aus dem alten Kernel übernommen und an die neue Struktur
 * angepasst
 */
object Task240Test extends App {

  println("Starting....")

  implicit val timeout = Timeout(5 seconds)

  println("instantiating...")
  val system = ActorSystem("TextualEpassIos")
  val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
  val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

  // Blocking ask to create the subjectProvider
  val future1 = subjectProviderManager ? CreateSubjectProvider()
  val userID: Int =
    Await.result(future1, timeout.duration).asInstanceOf[SubjectProviderCreated].userID

  println("UserID: " + userID)

  // Blocking ask to create the process
  val future = subjectProviderManager ? CreateProcess(userID)
  val processID: Int =
    Await.result(future, timeout.duration).asInstanceOf[ProcessCreated].processID

  // employee
  val employeeName = "Employee"
  val employeeStates = Array(
    new ActState("empl", "Fill out Application", Array(Transition("Done", "Do"))),
    new SendState("empl.br1", Array(Transition("BT Application", "Superior"))),
    new ReceiveState("empl.br1.br1", Array(Transition("Approval", "Superior"), Transition("Denial", "Superior", "End of the old one"))),
    new ActState("empl.br1.br1.br1", "Make business trip", Array(Transition("Done", "Do", "End of the old one"))),
    new EndState("End of the old one"))

  // Superior  
  val superiorName = "Superior"
  val superiorStates = Array(
    new ReceiveState("sup", Array(Transition("BT Application", "Employee"))),
    new ActState("sup.br1", "Check Application", Array(Transition("Approval", "Do"), Transition("Denial", "Do"))),
    new SendState("sup.br1.br1", Array(Transition("Approval", "Employee", "The End"))),
    new SendState("sup.br1.br2", Array(Transition("Denial", "Employee", "The End"))),
    new EndState("The End"))

  // add subjects
  println("add testsubjects")
  processManager ! AddSubject(userID, processID, employeeName)
  processManager ! AddSubject(userID, processID, superiorName)

  // add behaviorStates
  println("add behaviorStates")
  for (state <- employeeStates)
    subjectProviderManager ! AddState(userID, processID, employeeName, state)
  for (state <- superiorStates)
    subjectProviderManager ! AddState(userID, processID, superiorName, state)

  // execute states
  println("execute states")
  subjectProviderManager ! ExecuteRequest(userID, processID)
}
