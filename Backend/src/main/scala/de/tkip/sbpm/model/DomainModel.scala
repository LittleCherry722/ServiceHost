package de.tkip.sbpm.model

import spray.json.DefaultJsonProtocol
import spray.json.JsObject
import spray.json.RootJsonFormat
import spray.json.JsValue
import java.sql.Timestamp
import spray.json.DeserializationException
import spray.json.JsNumber

// Envelope for FE JSON answer
case class Envelope(data: Option[JsValue], code: String)

// Model for Administration
case class Configuration(key: String, label: String, value: String, dataType: String = "String")
case class User(id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8)
case class Role(id: Option[Int], name: String, isActive: Boolean = true)
case class Group(id: Option[Int], name: String, isActive: Boolean = true)

// Model for Modeling/Execution
case class Graph(id: Option[Int], graph: String, date: java.sql.Timestamp, processId: Int)
case class ProcessInstance(id: Option[Int], processId: Int, graphId: Int, involvedUsers: String, data: String)
case class Process(id: Option[Int], name: String, graphId: Int = -1, isProcess: Boolean = true, startSubjects: String = null)
case class Relation(userId: Int, groupId: Int, responsibleId: Int, processId: Int)
case class Message(id: Option[Int], from: Int, to: Int, instanceId: Int, isRead: Boolean, data: String, date: java.sql.Timestamp)
case class Action(id: Option[Int], data: String) // TODO extend this case class to fit the requirements

// Model for DB Relations 
case class GroupRole(groupId: Int, roleId: Int, isActive: Boolean = true)
case class GroupUser(groupId: Int, userId: Int, isActive: Boolean = true)
