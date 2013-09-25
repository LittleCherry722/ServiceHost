package de.tkip.sbpm.application.subject.misc

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.ActionID

object ActionIDProvider {
  private var currentID: Int = 0

  def nextActionID(): ActionID = synchronized {
    currentID += 1
    currentID
  }
}