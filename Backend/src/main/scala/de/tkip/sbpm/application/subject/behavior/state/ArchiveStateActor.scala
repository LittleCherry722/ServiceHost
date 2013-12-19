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

package de.tkip.sbpm.application.subject.behavior.state

import scala.Array.canBuildFrom
import akka.actor.Status.Failure
import java.io.File
import akka.actor.actorRef2Scala
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes.exitCondLabel
import de.tkip.sbpm.application.subject.behavior.Transition
import de.tkip.sbpm.application.subject.misc.ActionData
import de.tkip.sbpm.application.subject.misc.ActionExecuted
import de.tkip.sbpm.application.subject.misc.ExecuteAction
import de.tkip.sbpm.application.miscellaneous.AutoArchive
import de.tkip.sbpm.application.miscellaneous.AutoArchive
import de.tkip.sbpm.application.miscellaneous.ArchiveMessage
import java.io.PrintWriter
import de.tkip.sbpm.application.miscellaneous.AutoArchive
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import de.tkip.sbpm.model.Subject
import de.tkip.sbpm.model.Subject

protected case class ArchiveStateActor(data: StateData)
  extends BehaviorStateActor(data) {
  private final val archivePath = "./log/"
    
  changeState(exitTransition.successorID, data, null)
  
  
  
  
  protected def stateReceive = {
     case autoArchive : AutoArchive =>{
       //TODO do not address hardcoded... iterate over all and write them
      val msg =  data.internalStatus.variables.get("archiveMsg").get.messages(0).messageContent
      val format=new SimpleDateFormat("yyyy_MM_dd HH_mm_ss")
      val date=format.format(new Date);
      val f = new File(archivePath+"archive"+"_"+date+".log")
     
      println(f.getAbsolutePath())
      val writer = new PrintWriter(f)
      writer.write(msg)
      writer.close()
      
    }
  }
  private def exitTransition = exitTransitions(0)
  override protected def getAvailableAction: Array[ActionData] =
    exitTransitions.map((t: Transition) => ActionData(t.messageType, true, exitCondLabel))

}