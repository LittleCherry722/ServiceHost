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

package de.tkip.sbpm.bir

import de.tkip.sbpm.instrumentation.InstrumentedActor
//import com.google.sbpm.bir.tools._
//import com.google.sbpm.bir.samples._
//import spray.json.lenses.JsonLenses._
import spray.json._
import DefaultJsonProtocol._

object GoogleBIRActor{
  case class CreateActor(actorId: String)
  case class CreateBI(name: String, subjectName: String, content: String)
}

class GoogleBIRActor extends InstrumentedActor {

  import GoogleBIRActor._
  implicit val ec = context.dispatcher

  def wrappedReceive = {
    // the id should be the google id of the user
    case CreateActor(id) => createActor(id)
    case CreateBI(n,sn,c) => saveBI(n,sn,c)
  }

  def createActor(id: String) = {
//    new KeyGen().run(id, "api.key");
  }

  def saveBI(name: String, subjectName: String, content: String) = {
    val source = """{"process":[{"id":"Staples","name":"Staples","type":"single","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"","relatedProcess":null,"externalType":"external","role":"","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":7,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":5,"text":"","start":false,"end":false,"type":"receive","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":6,"text":"Bestellung aus Lager holen","start":false,"end":false,"type":"action","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"Verfügbarkeit prüfen","start":false,"end":false,"type":"action","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":3,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":4,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":2,"text":"m3","type":"cancelcondition","target":{"id":"Großunternehmen","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":4,"text":"Verfügbar","type":"cancelcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":2,"end":6,"text":"Nicht Verfügbar","type":"cancelcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":3,"end":1,"text":"m2","type":"cancelcondition","target":{"id":"Großunternehmen","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":5,"end":3,"text":"m2","type":"cancelcondition","target":{"id":"Zulieferer","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":4,"end":5,"text":"m1","type":"cancelcondition","target":{"id":"Zulieferer","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":6,"end":4,"text":"","type":"cancelcondition","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1},{"id":"Zulieferer","name":"Lieferant","type":"external","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"Zulieferer","relatedProcess":3,"externalType":"interface","role":"PROVIDER","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":3,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"receive","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":false,"type":"send","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":1,"text":"m1","type":"cancelcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m2","type":"cancelcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1},{"id":"Großunternehmen","name":"Kunde","type":"external","deactivated":false,"startSubject":false,"inputPool":100,"relatedSubject":"Großunternehmen","relatedProcess":1,"externalType":"interface","role":"","comment":"","variables":{},"variableCounter":1,"macros":[{"id":"##main##","name":"internal behavior","nodeCounter":3,"nodes":[{"id":0,"text":"","start":true,"end":false,"type":"send","deactivated":false,"majorStartNode":true,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"","conversation":"","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":1,"text":"","start":false,"end":false,"type":"receive","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}},{"id":2,"text":"","start":false,"end":true,"type":"end","deactivated":false,"majorStartNode":false,"conversation":"","variable":"","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":null},"macro":"","varMan":{"var1":"","var2":"","operation":"and","storevar":""}}],"edges":[{"start":0,"end":1,"text":"m3","type":"cancelcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]},{"start":1,"end":2,"text":"m2","type":"cancelcondition","target":{"id":"Staples","min":-1,"max":-1,"createNew":false,"variable":""},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":"","correlationId":"","comment":"","transportMethod":["internal"]}]}],"macroCounter":1}],"conversations":{},"conversationCounter":1,"messages":{"m1":"Abholauftrag","m2":"Lieferdatum","m3":"Bestellung"},"messageCounter":4,"nodeCounter":1}"""
    val json = content.asJson
//    val query = "process" / filter("name".is[String]( _ == subjectName)) / "role"
//    val processNames = json.extract[String](query)
//    val role = processNames.head
//    new CreateBi().run(name, subjectName, role)
  }

}
