package de.tkip.sbpm.application.subject

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes.MessageID

object MessageIDProvider {
  private var currentID: Int = 0

  def nextMessageID(): MessageID = synchronized {
    currentID += 1
    currentID
  }
}
