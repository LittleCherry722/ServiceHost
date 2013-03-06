package de.tkip.sbpm.application.subject

import scala.collection.mutable.{ ArrayBuffer, Map => MutableMap }
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._

class SubjectVariable {

}

/**
 * This class holds the InternalStatus of an Internalbehavior, defined by:
 * - Variables
 */
protected case class InternalStatus() {
  // set this variable to store if a subject started message has been sent
  // (needed for loop, which passes the start state several times)
  var subjectStartedSent = false

  // This map stores all active variables 
  val variables = MutableMap[String, Variable]()
}

protected case class Variable(id: String) {
  private val _messages = ArrayBuffer[SubjectToSubjectMessage]()

  def addMessage(message: SubjectToSubjectMessage) {
    _messages += message
  }
  
  def messages = _messages.toArray

  override def toString() = {
    "{%s <- %s}".format(id, _messages.mkString("[", ", ", "]"))
  }
}
