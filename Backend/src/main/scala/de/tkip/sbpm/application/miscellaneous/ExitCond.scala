package de.tkip.sbpm.application.miscellaneous

import ProcessAttributes._

// case class because of the apply method
case class ExitCond(val messageType: MessageType, val subjectName: SubjectName) 
