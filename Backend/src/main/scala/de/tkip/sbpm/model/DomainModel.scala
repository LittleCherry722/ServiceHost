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
case class User(var id: Option[Int], name: String, isActive: Boolean = true, inputPoolSize: Int = 8)
case class UserIdentity(user: User, provider: String, eMail: String, password: Option[String])
case class Role(var id: Option[Int], name: String, isActive: Boolean = true)
case class Group(var id: Option[Int], name: String, isActive: Boolean = true)
case class Credentials(provider: String, oldEMail: String, eMail: Option[String], oldPassword: String, password: Option[String])

// Model for Modeling/Execution
case class Graph(var id: Option[Int], graph: String, date: java.sql.Timestamp, var processId: Int = -1)
case class ProcessInstance(var id: Option[Int], processId: Int, graphId: Int, involvedUsers: String, data: String)
case class Process(var id: Option[Int], name: String, var graphId: Int = -1, var isCase: Boolean = false, startSubjects: String = "[]")
case class Relation(userId: Int, groupId: Int, responsibleId: Int, processId: Int)
case class Message(var id: Option[Int], from: Int, to: Int, instanceId: Int, isRead: Boolean, data: String, date: java.sql.Timestamp)
case class Action(id: Option[Int], data: String) // TODO extend this case class to fit the requirements

// Model for DB Relations 
case class GroupRole(groupId: Int, roleId: Int, isActive: Boolean = true)
case class GroupUser(groupId: Int, userId: Int, isActive: Boolean = true)
