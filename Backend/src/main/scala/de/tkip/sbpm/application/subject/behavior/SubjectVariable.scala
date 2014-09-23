/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.application.subject.behavior

import scala.collection.mutable.{ ArrayBuffer, Map => MutableMap }
import scala.collection.mutable.{Map => MutableMap}

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage

class SubjectVariable {

}

/*protected*/ case class Variable(id: String) {
  private val _messages = ArrayBuffer[SubjectToSubjectMessage]()

  def addMessage(message: SubjectToSubjectMessage) {
    _messages += message
  }
  
  def messages = _messages.toArray

  override def toString() = {
    "{%s <- %s}".format(id, _messages.mkString("[", ", ", "]"))
  }
}
