package de.tkip.servicehost.serviceactor.stubgen

import akka.actor.Actor
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import de.tkip.servicehost.Messages._
import de.tkip.sbpm.application.subject.misc.GetProxyActor
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import de.tkip.sbpm.application.subject.misc.SubjectToSubjectMessage
import java.util.Date
import de.tkip.sbpm.application.subject.misc.Stored
import de.tkip.servicehost.ActorLocator
import de.tkip.servicehost.ServiceAttributes._
import scala.collection.immutable.Map
import scala.collection.mutable.Queue
import de.tkip.sbpm.application.subject.misc.Rejected


import de.tkip.vasec._
import de.tkip.vasec.VasecJsonProtocol._
import spray.json._

import java.io.File

import java.awt.image.BufferedImage

import javax.imageio
import javax.imageio.ImageIO


class TrafficServiceActor extends TemplateServiceActor {

  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-traffic"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-traffic"
  
  override protected def states: List[State] = super.states.map(state => state match {
    case internalaction(id, exitType, targets, targetIds, text) => loadTrafficData(id,"exitcondition",targets,targetIds,"fetch gas stations")
    case _ => state
  })

  private val trafficData: Seq[VROI] = Seq(
      VCircle(2.0,8.0,1.0,2)
    )

  case class loadTrafficData(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      rois = trafficData

      actor.changeState()
    }
  }

}
