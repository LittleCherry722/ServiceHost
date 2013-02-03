package de.tkip.sbpm.rest

/**
 * this trait extends spray-json to support JsObject and JsArray as root objects.
 * 
 * By now this has to be done by this code. In future spray releases it should work by itself.
 * 
 * See issue here: https://github.com/spray/spray-json/issues/31 
 */
trait SprayJsonSupport extends spray.httpx.SprayJsonSupport {
  import spray.json._

  implicit object JsObjectWriter extends RootJsonFormat[JsObject] {
    def write(jsObject: JsObject) = jsObject
    def read(value: JsValue) = value.asJsObject
  }

  implicit object JsArrayWriter extends RootJsonFormat[JsArray] {
    def write(jsArray: JsArray) = jsArray
    def read(value: JsValue) = value.asInstanceOf[JsArray]
  }
}

object SprayJsonSupport extends SprayJsonSupport