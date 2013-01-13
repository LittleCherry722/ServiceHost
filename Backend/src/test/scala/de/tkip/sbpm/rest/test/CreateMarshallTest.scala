package de.tkip.sbpm.rest.test

import org.junit.Test

import de.tkip.sbpm.model._
import de.tkip.sbpm.rest.JsonProtocol._
import spray.json._

class CreateMarshallTest  {
  
  @Test
  def test1 {
    println("Los")
	val json = User(Some(1), "SurfNazi", true, 160).toJson
	val color = json.convertTo[User]
    
	println(json)
	println(color)
	
  }

}

