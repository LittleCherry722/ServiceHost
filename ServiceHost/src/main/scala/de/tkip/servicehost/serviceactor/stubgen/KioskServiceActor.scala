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


class KioskServiceActor extends TemplateServiceActor {

  override protected val serviceID: ServiceID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-kiosk"
  override protected val subjectID: SubjectID = "Subj2:ff9bacbf-bb0c-4316-9cd5-5328e1246557-kiosk"
  
  override protected def states: List[State] = super.states.map(state => state match {
    case internalaction(id, exitType, targets, targetIds, text) => fetchKiosks(id,"exitcondition",targets,targetIds,"fetch kiosks")
    case _ => state
  })

  private val kiosks: Seq[(Set[String],VSinglePoint)] = Seq(
      (Set("food", "drinks", "books", "magazines", "newspapers", "tobaccos"), VSinglePoint(2.0,9.0)),
      (Set("food", "drinks", "books", "magazines", "newspapers", "tobaccos"), VSinglePoint(7.5,9.0)),
      (Set("magazines", "newspapers", "tobaccos"),                            VSinglePoint(3.0,3.0))
    )

  case class fetchKiosks(override val id: Int, override val exitType: String, override val targets: Map[BranchID, Target], override val targetIds: Map[BranchID, Int], override val text: String) extends State("action", id, exitType, targets, targetIds, text) {

    val stateName = "" //TODO state name

    def process()(implicit actor: ServiceActor) {
      var data = kiosks
      
      if (!Array("", "[]", "[empty message]").contains(messageContent)) {
        val configKey = "kiosk"
        val config = messageContent.parseJson.asInstanceOf[JsObject]
  
        if (config.fields.contains(configKey)) {
          val articles = config.fields(configKey).asInstanceOf[JsObject].fields("articles").convertTo[Set[String]]

          data = data.filter(x => articles.subsetOf(x._1))
        }
        else {
          log.warning("config does not contain key \"" + configKey + "\"! messageContent = " + messageContent)
        }
      }
      else {
        log.warning("no config object given! messageContent = " + messageContent)
      }

      val g = VPOIGroup(1, data.map(x => x._2))

      pois = g :: Nil

      actor.changeState()
    }
  }

}
