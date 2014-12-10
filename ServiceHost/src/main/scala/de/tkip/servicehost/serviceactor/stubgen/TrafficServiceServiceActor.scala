package de.tkip.servicehost.serviceactor.stubgen

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.vasec.VasecJsonProtocol._
import de.tkip.vasec._
import spray.json._
import scala.collection.mutable.Map


class TrafficServiceServiceActor extends TemplateServiceServiceActor {

  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-traffic"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-traffic"

  override protected def states: List[State] = super.states.map(state => state match {
    case internalaction(id, exitType, targets, targetIds, text, variableId) => loadTrafficData(id,"exitcondition",targets,targetIds,"fetch gas stations", "")
    case _ => state
  })
  private val trafficData: Seq[VROI] = Seq(
    VCircle(2.0,8.0,1.0,2)
  )

  case class loadTrafficData(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {


    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      rois = trafficData

      actor.changeState()
    }
  }
}
