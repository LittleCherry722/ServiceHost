package de.tkip.servicehost.serviceactor.stubgen

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.vasec.VasecJsonProtocol._
import de.tkip.vasec._
import spray.json._
import scala.collection.mutable.Map


class GasStationServiceServiceActor extends TemplateServiceServiceActor {

  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-gasstation"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-gasstation"

  override protected def states: List[State] = super.states.map(state => state match {
    case internalaction(id, exitType, targets, targetIds, text, variableid) => fetchGasStations(id, "exitcondition", targets, targetIds, "fetch gas stations", "")
    case _ => state
  })

  private val gasStations: Seq[(Set[String], VSinglePoint)] = Seq(
    (Set("gasoline", "natural gasoline"), VSinglePoint(7.5, 9.0)),
    (Set("gasoline", "electric"), VSinglePoint(9.5, 0.5))
  )

  case class fetchGasStations(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      //      var data = gasStations
      //
      //      if (!Array("", "[]", "[empty message]").contains(messageContent)) {
      //        val configKey = "gasstation"
      //        val config = messageContent.parseJson.asInstanceOf[JsObject]
      //
      //        if (config.fields.contains(configKey)) {
      //          val fuel = config.fields(configKey).asInstanceOf[JsObject].fields("fuel").convertTo[String]
      //
      //          data = data.filter(x => x._1.contains(fuel))
      //        }
      //        else {
      //          log.warning("config does not contain key \"" + configKey + "\"! messageContent = " + messageContent)
      //        }
      //      }
      //      else {
      //        log.warning("no config object given! messageContent = " + messageContent)
      //      }
      //
      //      val g = VPOIGroup(1, data.map(x => x._2))
      //
      //      pois = g :: Nil
      actor.changeState()
    }
  }

}
