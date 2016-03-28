package de.tkip.sbpm.application.subject

import de.tkip.sbpm.instrumentation.InstrumentedActor

class ExternalSubjectActor extends InstrumentedActor {
  //TODO 1 actor instance zum forwarden
  def wrappedReceive = {
    case _ =>
  }

}
