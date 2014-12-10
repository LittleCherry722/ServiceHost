package de.tkip.servicehost.serviceactor.stubgen

import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.servicehost.ServiceAttributes._
import de.tkip.servicehost.serviceactor.ServiceActor
import de.tkip.vasec.VasecJsonProtocol._
import de.tkip.vasec._
import spray.json._
import scala.collection.mutable.Map


class KioskServiceServiceActor extends TemplateServiceServiceActor {

  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-kiosk"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-kiosk"

  override protected def states: List[State] = super.states.map(state => state match {
    case internalaction(id, exitType, targets, targetIds, text, variableId) => fetchKiosks(id, "exitcondition", targets, targetIds, "fetch kiosks", "")
    case _ => state
  })

  private val kiosks: Seq[(Set[String], VSinglePoint)] = Seq(
    (Set("food", "drinks", "books", "magazines", "newspapers", "tobaccos"), VSinglePoint(2.0, 9.0)),
    (Set("food", "drinks", "books", "magazines", "newspapers", "tobaccos"), VSinglePoint(7.5, 9.0)),
    (Set("magazines", "newspapers", "tobaccos"), VSinglePoint(3.0, 3.0))
  )

  case class fetchKiosks(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String, override val variableId: String) extends State("action", id, exitType, targets, targetIds, text, variableId) {

    def process()(implicit actor: ServiceActor) {
      //      var data = kiosks
      //
      //      if (!Array("", "[]", "[empty message]").contains(messageContent)) {
      //        val configKey = "kiosk"
      //        val config = messageContent.parseJson.asInstanceOf[JsObject]
      //
      //        if (config.fields.contains(configKey)) {
      //          val articles = config.fields(configKey).asInstanceOf[JsObject].fields("articles").convertTo[Set[String]]
      //
      //          data = data.filter(x => articles.subsetOf(x._1))
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
      println("ok ok ok ok ok ok ok ok ok ok ok ok")

      actor.changeState()
    }
  }

}
