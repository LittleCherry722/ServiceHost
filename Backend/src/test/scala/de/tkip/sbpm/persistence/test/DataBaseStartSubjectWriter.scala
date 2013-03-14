package de.tkip.sbpm.persistence.test

import akka.actor._
import akka.pattern.ask
import de.tkip.sbpm.model.Process
import de.tkip.sbpm.persistence._
import de.tkip.sbpm.ActorLocator
import akka.util.Timeout
import scala.concurrent.Await
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.persistence.query._

object DataBaseStartSubjectWriter {
  val allStartSubjects: Array[(ProcessID, String)] =
    Array(
      (1, "[\"Employee\"]"),
      (2, "[\"Subj1\"]"),
      (3, "[\"Subj1\"]"))

  def main(s: Array[String]) {
    val system = ActorSystem()
    val persistenceActor = system.actorOf(Props[PersistenceActor], ActorLocator.persistenceActorName)

    val write = true
    //    val processID = 1
    //    val startSubjects = "[\"Employee\", \"Manager\"]"
    for ((processID, startSubjects) <- allStartSubjects) {
      persistenceActor ! Schema.Create

      implicit val timeout = Timeout(5000)

      val future = persistenceActor ? Processes.Read.ById(processID)
      val result = Await.result(future, timeout.duration).asInstanceOf[Option[Process]]
      if (result.isDefined) {
        println("Old Entry: " + result)
        val process = result.get
        // create the new entry
        val entry = Process(process.id, process.name, process.isCase)
        // write it into the database
        if (write) {
          persistenceActor ! Processes.Save(entry)
          Thread.sleep(500)
          println("New Entry: " + Await.result(persistenceActor ? Processes.Read.ById(processID), timeout.duration))
        }
      } else {
        System.err.println("Process " + processID + " does not exists")
      }
    }
  }

}