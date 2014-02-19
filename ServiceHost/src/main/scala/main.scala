import akka.actor._
import scalaj.http.Http
import Messages.RegisterServiceMessage
import Messages.ExecuteServiceMessage

/*

Momentan funktioniert es nur so: Starte Instanz von Prozess Großunternehmen. Führe aus bis send. Message kommt hier an.

 */


object main extends App {
  println("main started")
  val repoUrl = "http://localhost:8181/repo"
  val system = ActorSystem("sbpm")

  system.actorOf(Props[ServiceHostActor], "subject-provider-manager")
  registerInterface()


  /**
   * Registers the interface at the interface repository by sending the graph data and some additional information as
   * post request to the repository url
   *
   * Internal Subject name: Lokal (Kunde)
   * External Subject name: Extern (Zulieferer)
   * msg from Lokal -> Extern: input
   * msg from Extern -> Lokal: output
   */
  def registerInterface(): Unit = {
    println("registerInterface")
    val source = scala.io.Source.fromFile("./src/main/resources/interface.json")
    val jsonString = source.mkString
    source.close()

    val result = Http.postData(repoUrl, jsonString)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .responseCode

    if (200 == result) {
      println("Registered interface at local repository")
    } else {
      println("Some error occurred; " + result)
    }
  }
}


class ServiceHostActor extends Actor {
  def receive: Actor.Receive = {
    case register: RegisterServiceMessage => {
      println("received RegisterServiceMessage: " + register)
      // TODO implement
    }
    case execute: ExecuteServiceMessage => {
      println("received ExecuteServiceMessage: " + execute)
      // TODO implement
    }
    case something => {
      println("received something: "+something)
    }
  }
}