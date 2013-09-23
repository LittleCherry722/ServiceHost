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

import spray.json.RootJsonFormat
import akka.actor.ActorRef
import spray.json.JsValue
import spray.json.DefaultJsonProtocol

case class CreateBIHeader(userID: String, name: String, subjectName: String, content: String)

object BIRJsonProtocol extends DefaultJsonProtocol{
  
  implicit val createBIHeaderFormat = jsonFormat4(CreateBIHeader)

}