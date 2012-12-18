import akka.actor._

import de.tkip.sbpm.application._
import de.tkip.sbpm.application.miscellaneous._

class Task240Test {

}

/**
 * kleiner Test der SubjectManagerProvider und ProcessManager instanziert, dann Subjekte und Verhalten hinzufügt und
 * eine Statusabfrage sendet, die StateAusführung jedes Subjects erzwingt
 * 
 * Beispiel wurde aus dem alten Kernel übernommen und an die neue Struktur angepasst
 */
object Task240Test extends App {

  println("Starting....")

  println("instantiating...")
  val system = ActorSystem("TextualEpassIos")
  val processManager = system.actorOf(Props(new ProcessManagerActor("BT_Application")), name = "BT_Application")
  val subjectProviderManager = system.actorOf(Props(new SubjectProviderManagerActor(processManager)))

  // instantiate subjectProvider and processes
  val userID = 10
  val processID = 100
  subjectProviderManager ! CreateSubjectProvider(userID)
  processManager ! CreateProcess(processID)

  // employee
  val employeeName = "employee"
  val employeeStates = Array(
    new ActState("empl", "Fill out Application", Array(Transition("Done", "Do"))),
    new SendState("empl.br1", Array(Transition("BT Application", "Superior"))),
    new ReceiveState("empl.br1.br1", Array(Transition("Approval", "Superior"), Transition("Denial", "Superior", "End of the old one"))),
    new ActState("empl.br1.br1.br1", "Make business trip", Array(Transition("Done", "Do", "End of the old one"))),
    new EndState("End of the old one"))

  // Superior  
  val superiorName = "superior"
  val superiorStates = Array(
    new ReceiveState("sup", Array(Transition("BT Application", "Employee"))),
    new ActState("sup.br1", "Check Application", Array(Transition("Approval", "Do"), Transition("Denial", "Do"))),
    new SendState("sup.br1.br1", Array(Transition("Approval", "Employee", "The End"))),
    new SendState("sup.br1.br2", Array(Transition("Denial", "Employee", "The End"))),
    new EndState("The End"))

  // add subjects
  println("add testsubjects")
  processManager ! AddSubject(processID, employeeName)
  processManager ! AddSubject(processID, superiorName)

  // add behaviorStates
  println("add behaviourStates")
  for (state <- employeeStates)
    subjectProviderManager ! AddState(userID, processID, employeeName, state)
  for (state <- superiorStates)
    subjectProviderManager ! AddState(userID, processID, superiorName, state)

  // execute states
  println("execute states")
  subjectProviderManager ! StatusRequest(userID, processID)
}