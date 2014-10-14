package de.tkip.sbpm.repository

import spray.json._
import de.tkip.sbpm.application.ProcessInstanceActor.{ Agent, AgentAddress }

/**
 * Created by arne on 30.03.14.
 */
object RepositoryJsonProtocol extends DefaultJsonProtocol {
  implicit val agentAddressJsonProtocol = jsonFormat2(AgentAddress)
  implicit val agentJsonProtocol = jsonFormat3(Agent)
}
