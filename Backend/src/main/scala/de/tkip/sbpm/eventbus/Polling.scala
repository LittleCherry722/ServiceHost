package de.tkip.sbpm.eventbus

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import akka.actor.Cancellable
import scala.concurrent.Future

object Polling {
  val system = akka.actor.ActorSystem("system", ConfigFactory.load("polling"))
  implicit val timeout = Timeout(10 seconds)
  import system.dispatcher

  /*
   *   can use this function like:
   *   val callback = (r: Future[Any]) => {
   *     val result = Await.result(r, 10 seconds).asInstanceOf[ReplyForTrafficJam]
   *     println(result.result + " test test")
   *   }  
   *   val cancellable = Polling.startPolling("/trafficJams",5, callback)
   *   
   *   stop polling like:
   *   cancellable.cancel
   */
  def startPolling(msgType: String, timeInterval: Int, callback: Future[Any] => Unit): Cancellable = {
    println("Polling started!")
    val selection = system.actorSelection("akka.tcp://PollingApp@127.0.0.1:6666/user/receive")
    val msg = msgType match {
      case "/trafficJams" => AskForTrafficJam()
      case _              => "somethingelse"
    }
    val cancellable = system.scheduler.schedule(0 seconds, timeInterval seconds) {
      val future = selection ? msg     
      callback(future)
    }
    return cancellable
  }
}