package de.tkip.vasec

import akka.event.LoggingAdapter

import spray.json._
import DefaultJsonProtocol._


sealed trait VPoint {
  def x: Double
  def y: Double
}

sealed trait VROI {
  def metricFactor: Int

  def intersectLength(a: VPoint, b: VPoint): Double
  def getBoundary: Seq[VSinglePoint]

  final def getMetricFactor(): Double = metricFactor match {
    // 1 is invalid, intersecting Routes must be excluded
    case 2 => 5.0
    case 3 => 2.0
    case 4 => 0.8
    case 5 => 0.5
    case 6 => 0.2
    case x => {
      println("getMetricFactor called for invalid metricFactor = " + x)
      0.0
    }
  }
}

case class VSinglePoint(x: Double, y: Double) extends VPoint

case class VStartEnd(start: VSinglePoint, end: VSinglePoint)

case class VRoute(points: Seq[VSinglePoint], metric: Double)


case class VPOIGroup(num: Int, points: Seq[VSinglePoint])


case class VCircle(x: Double, y: Double, r: Double, metricFactor: Int) extends VROI {
  def intersectLength(A: VPoint, B: VPoint): Double = geometric.intersectLength(A, B, this)
  def getBoundary: Seq[VSinglePoint] = VSinglePoint(x, y) :: Nil // TODO
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
}

