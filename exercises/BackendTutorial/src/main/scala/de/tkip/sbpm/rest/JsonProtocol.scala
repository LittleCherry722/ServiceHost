package de.tkip.sbpm.rest

import spray.json.DefaultJsonProtocol
import de.tkip.sbpm.application._
import spray.json.RootJsonFormat
import akka.actor.ActorRef
import spray.json.JsValue

object JsonProtocol extends DefaultJsonProtocol {

  implicit val readSubjectFormat = jsonFormat1(ReadSubject)
  implicit val subjectAnswerFormat = jsonFormat3(SubjectAnswer)
  implicit val executeActionFormat = jsonFormat2(ExecuteAction)

}
