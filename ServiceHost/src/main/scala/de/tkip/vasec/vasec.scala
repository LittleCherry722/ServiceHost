package de.tkip.vasec

import spray.json._
import DefaultJsonProtocol._


trait VPoint {
  def x: Double
  def y: Double
}

trait VROI {
  def metricFactor: Double // TODO: enum / discrete interval [1,2,3,4,5]

  def intersectLength(a: VPoint, b: VPoint): Double
  def getBoundary: Seq[VPoint] = Nil // TODO: just an idea, not yet used
}

case class VSinglePoint(x: Double, y: Double) extends VPoint

case class VStartEnd(start: VSinglePoint, end: VSinglePoint)

case class VRoute(points: Seq[VSinglePoint], metric: Double)


case class VPOIGroup(num: Int, points: Seq[VSinglePoint])
case class VROIGroup(num: Int, points: Seq[VROI])


case class VCircle(x: Double, y: Double, r: Double, metricFactor: Double = 1.0) extends VROI {
  def intersectLength(a: VPoint, b: VPoint): Double = 0.0 // TODO
}


object VasecJsonProtocol extends DefaultJsonProtocol {
  implicit val vSinglePointFormat = jsonFormat2(VSinglePoint)
  implicit val vStartEndFormat = jsonFormat2(VStartEnd)
  implicit val vRouteFormat = jsonFormat2(VRoute)

  implicit val vCircleFormat = jsonFormat4(VCircle)

  implicit object vROIFormat extends RootJsonFormat[VROI] {
    def write(obj: VROI) = obj match {
      case x: VCircle => {
        val y = x.toJson.asJsObject
        y.copy(fields = y.fields ++ Map("type" -> JsString("circle")))
      }
    }
    def read(json: JsValue) = {
      json.asJsObject.fields("type") match {
        case JsString("circle") => json.convertTo[VCircle]
        case x                  => throw new DeserializationException("known VROI type expected; type = " + x)
      }
    }
  }

  implicit val vPOIGroupFormat = jsonFormat2(VPOIGroup)
  implicit val vROIGroupFormat = jsonFormat2(VROIGroup)
}
