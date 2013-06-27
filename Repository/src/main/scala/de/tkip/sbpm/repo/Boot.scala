package de.tkip.sbpm.repo

import spray.routing.SimpleRoutingApp
import akka.actor.Props
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.util.Timeout
import spray.http.{StatusCodes, HttpResponse}


object Boot extends App with SimpleRoutingApp {

  val repoActor = system.actorOf(Props[RepoActor])

  implicit val timeout = Timeout(5 seconds)


  startServer(interface = "localhost", port = 8181) {
    pathPrefix("repo") {
      get {
        path(IntNumber) {
          id =>
            val future = repoActor ? GetEntry(id)
            val result = Await.result(future, timeout.duration).asInstanceOf[Option[String]]

            result match {
              case Some(s) => complete(s)
              case None => complete(HttpResponse(status = StatusCodes.NotFound))
            }
        } ~ path("") {
          ctx =>
            val future = repoActor ? GetEntries
            val result = Await.result(future, timeout.duration).asInstanceOf[String]
            ctx.complete(result)
        }
      } ~
        post {
          entity(as[String]) {
            entry =>
              val future = repoActor ? CreateEntry(entry)
              val result = Await.result(future, timeout.duration).asInstanceOf[Option[String]]

              result match {
                case Some(s) => complete(s)
                case None => complete(HttpResponse(status = StatusCodes.InternalServerError))
              }
          }
        }
    }
  }
}
