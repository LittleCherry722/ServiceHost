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

package de.tkip.sbpm.rest

import de.tkip.sbpm.model._
import spray.json._
import scala.collection.immutable.Map
import java.util.UUID

/**
 * Provides conversion from Graph domain model
 * to JSON and vice versa.
 */
object GraphJsonProtocol extends DefaultJsonProtocol {

  /**
   * Override default option format because null values in
   * JSON are sometimes represented as empty String.
   */
  implicit override def optionFormat[T: JsonFormat] = new OptionFormat[T] {
    override def write(o: Option[T]) = super.write(o)
    override def read(v: JsValue) = v match {
      // treat empty string also as null
      case JsString("") => None
      case x            => super.read(x)
    }
  }

  /**
   * Reset behavior of Option for Option[String] to defaults, to
   * avoid changing empty string to None.
   */
  implicit val stringOptionFormat = new OptionFormat[String]

  implicit object TimestampFormat extends JsonFormat[java.sql.Timestamp] {
    def write(t: java.sql.Timestamp) = JsNumber(t.getTime)
    def read(v: JsValue) = v match {
      case JsNumber(t) => new java.sql.Timestamp(t.toLong)
      case _           => throw new DeserializationException("Timestamp expected.")
    }
  }

  /**
   * JSON format: { "conversationId": "conversationName", ... }
   */
  implicit object ConversationsFormat extends RootJsonFormat[Map[String, GraphConversation]] {
    def write(map: Map[String, GraphConversation]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphConversation(id, name))
      case _                    => throw new DeserializationException("Conversations map expected.")
    }
  }

  /**
   * JSON format: { "messageId": "messageName", ... }
   */
  implicit object MessagesFormat extends RootJsonFormat[Map[String, GraphMessage]] {
    def write(map: Map[String, GraphMessage]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphMessage(id, name))
      case _                    => throw new DeserializationException("Messages map expected.")
    }
  }

  /**
   * JSON format: { "variableId": "variableName", ... }
   */
  implicit object VariablesFormat extends RootJsonFormat[Map[String, GraphVariable]] {
    def write(map: Map[String, GraphVariable]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphVariable(id, name))
      case _                    => throw new DeserializationException("Variables map expected.")
    }
  }

  /**
   * Converts roles from domain model to JSON and vice versa.
   * "roles" map is needed to provide mapping between role name
   * and role id because in graph JSON roles are identified by name
   * not by id.
   */
  implicit def optionRoleFormat(implicit roles: Map[String, Role] = Map()) = new JsonFormat[Option[Role]] {
    def write(o: Option[Role]) = o match {
      case None    => JsString("noRole")
      // write only role name to JSON
      case Some(r) => JsString(r.name)
    }
    def read(v: JsValue) = v match {
      // convert role name back to role object if role is known 
      case JsString(name) if (roles.contains(name)) => Some(roles(name))
      case JsString("noRole")                       => None
      case JsString("")                             => None
      case JsNull                                   => None
      case _                                        => throw new DeserializationException("Existing role name or null expected. Unknown role: " + v + " Known roles: " + roles)
    }
  }

  /**
   * Format for node's options object.
   */
  implicit object NodeOptionsFormat extends JsonFormat[GraphNodeOptions] {
    def write(o: GraphNodeOptions) = JsObject(
      // convert message and subject id to * if they are None in database
      "message" -> NoneAsterisk(o.messageId),
      "subject" -> NoneAsterisk(o.subjectId),
      "correlationId" -> o.correlationId.toJson,
      "conversation" -> o.conversationId.toJson,
      "state" -> o.nodeId.toJson)
    def read(v: JsValue) =
      v.asJsObject.getFields("message", "subject", "correlationId", "conversation", "state") match {
        case Seq(msg: JsValue, subj: JsValue, corr: JsValue, ch: JsValue, st: JsValue) =>
          GraphNodeOptions(
            // convert message and subject id to None if they are * in the JSON
            NoneAsterisk.unapply(msg),
            NoneAsterisk.unapply(subj),
            corr.convertTo[Option[String]],
            ch.convertTo[Option[String]],
            st.convertTo[Option[Short]])
        case _ => GraphNodeOptions()
      }
  }

  /**
   * Format for node's varMan object.
   */
  implicit val varManFormat = jsonFormat(GraphVarMan,
    "var1", "var2", "operation", "storevar")

  /**
   * Format of a node object.
   */
  implicit val nodeFormat = jsonFormat(GraphNode, "id",
    "text",
    "start",
    "end",
    "type",
    "manualPositionOffsetX",
    "manualPositionOffsetY",
    "autoExecute",
    "deactivated",
    "majorStartNode",
    "conversation",
    "variable",
    "options",
    "macro",
    "varMan")

  /**
   * Convert nodes map (domain model) to nodes array (JSON) and vice versa.
   */
  implicit object NodesFormat extends RootJsonFormat[Map[Short, GraphNode]] {
    def write(map: Map[Short, GraphNode]) =
      JsArray(map.values.map(_.toJson).toSeq: _*)
    def read(v: JsValue) = v match {
      case a: JsArray => a.elements.map { v =>
        val n = v.convertTo[GraphNode]
        (n.id -> n)
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  /**
   * Format for edge's target object.
   */
  implicit val edgeTargetFormat = jsonFormat(GraphEdgeTarget,
    "id", "min", "max", "createNew", "variable")

  /**
   * Format of an edge object.
   */
  implicit val edgeFormat = jsonFormat(GraphEdge, "start",
    "end",
    "text",
    "type",
    "manualPositionOffsetLabelX",
    "manualPositionOffsetLabelY",
    "target",
    "deactivated",
    "optional",
    "priority",
    "manualTimeout",
    "variable",
    "correlationId",
    "comment",
    "transportMethod")

  /**
   * Format of a macro object.
   */
  implicit object MacroFormat extends RootJsonFormat[GraphMacro] {
    def write(m: GraphMacro) = JsObject(
      "id" -> m.id.toJson,
      "name" -> m.name.toJson,
      "nodeCounter" -> counter(m.nodes.keys),
      "nodes" -> m.nodes.toJson,
      "edges" -> m.edges.toJson)
    def read(v: JsValue) =
      v.asJsObject.convertTo[GraphMacro](jsonFormat4(GraphMacro))
  }

  /**
   * Format for a routing entry.
   */
  implicit object RoutingFormat extends RootJsonFormat[GraphRouting] {
    def write(r: GraphRouting) = JsObject(
      "id" -> r.id.toJson,
      "subject1Value" -> r.condition.subjectId.toJson,
      "is1Value" -> {
        if (r.condition.operator)
          "is"
        else
          "is not"
      }.toJson,
      "groupUser1Value" -> {
        if (r.condition.groupId.isDefined)
          "in group"
        else
          "user"
      }.toJson,
      "groupUser1ListValue" -> r.condition.groupId.getOrElse(r.condition.userId.getOrElse(-1)).toJson,
      "subject2Value" -> r.implication.subjectId.toJson,
      "is2Value" ->  {
        if (r.implication.operator)
          "is"
        else
          "is not"
      }.toJson,
      "groupUser2Value" -> {
        if (r.implication.groupId.isDefined)
          "in group"
        else
          "user"
      }.toJson,
      "groupUser2ListValue" -> r.implication.groupId.getOrElse(r.implication.userId.getOrElse(-1)).toJson)
    def read(v: JsValue) = v.asJsObject.getFields("id", "subject1Value", "is1Value", "groupUser1Value", "groupUser1ListValue", "subject2Value", "is2Value", "groupUser2Value", "groupUser2ListValue") match {
      case Seq(JsString(subject1Value), JsString(is1Value), JsString(groupUser1Value), JsNumber(groupUser1ListValue), JsString(subject2Value), JsString(is2Value), JsString(groupUser2Value), JsNumber(groupUser2ListValue)) =>
        GraphRouting(UUID.randomUUID().toString(),
          GraphRoutingExpression(subject1Value,
            is1Value.equals("is"),
            if (groupUser1Value.equals("in group")) Some(groupUser1ListValue.toInt) else None,
            if (!groupUser1Value.equals("in group")) Some(groupUser1ListValue.toInt) else None),
          GraphRoutingExpression(subject2Value,
            is2Value.equals("is"),
            if (groupUser2Value.equals("in group")) Some(groupUser2ListValue.toInt) else None,
            if (!groupUser2Value.equals("in group")) Some(groupUser2ListValue.toInt) else None))
      case Seq(JsString(id), JsString(subject1Value), JsString(is1Value), JsString(groupUser1Value), JsNumber(groupUser1ListValue), JsString(subject2Value), JsString(is2Value), JsString(groupUser2Value), JsNumber(groupUser2ListValue)) =>
        GraphRouting(id,
          GraphRoutingExpression(subject1Value,
            is1Value.equals("is"),
            if (groupUser1Value.equals("in group")) Some(groupUser1ListValue.toInt) else None,
            if (!groupUser1Value.equals("in group")) Some(groupUser1ListValue.toInt) else None),
          GraphRoutingExpression(subject2Value,
            is2Value.equals("is"),
            if (groupUser2Value.equals("in group")) Some(groupUser2ListValue.toInt) else None,
            if (!groupUser2Value.equals("in group")) Some(groupUser2ListValue.toInt) else None))
      case x => throw new DeserializationException("""Graph routing with "subject1Value", "is1Value", "groupUser1Value", "groupUser1ListValue", "subject2Value", "is2Value", "groupUser2Value", "groupUser2ListValue" expected, but found """ + x)
    }
  }

  /**
   * Convert macros map (domain model) to macro array (JSON) and vice versa.
   */
  implicit object macrosFormat extends RootJsonFormat[Map[String, GraphMacro]] {
    def write(ms: Map[String, GraphMacro]) =
      JsArray(ms.values.map(_.toJson).toSeq: _*)
    def read(v: JsValue) = v match {
      case a: JsArray => a.elements.map { e =>
        val m = e.convertTo[GraphMacro]
        (m.id -> m)
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  implicit val addressFormat = jsonFormat2(Address)
  implicit val interfaceImplementationFormat = jsonFormat4(InterfaceImplementation)

  /**
   * Format for a subject object.
   * Counter values are calculated on the fly when converting to JSON
   * and ignored while converting from JSON to domain model.
   */
  implicit def subjectFormat(implicit roles: Map[String, Role] = Map()) = new RootJsonFormat[GraphSubject] {
    def write(s: GraphSubject) = JsObject(
      "id" -> s.id.toJson,
      "name" -> s.name.toJson,
      "type" -> s.subjectType.toJson,
      "deactivated" -> s.isDisabled.toJson,
      "startSubject" -> s.isStartSubject.toJson,
      "inputPool" -> s.inputPool.toJson,
      "relatedSubject" -> s.relatedSubjectId.toJson,
      "relatedInterface" -> s.relatedInterfaceId.toJson,
      "isImplementation" -> s.isImplementation.toJson,
      "externalType" -> s.externalType.toJson,
      "role" -> s.role.toJson,
      "url" -> s.url.toJson,
      "implementations" -> s.implementations.toJson,
      "comment" -> s.comment.toJson,
      "variables" -> s.variables.toJson,
      // extract counter value from variable ids
      "variableCounter" -> counter(s.variables),
      "macros" -> s.macros.values.toJson,
      // extract counter value fro∂m macro ids
      "macroCounter" -> counter(s.macros))
    def read(v: JsValue) = v.asJsObject.convertTo[GraphSubject](jsonFormat(GraphSubject, "id", "name",
      "type",
      "deactivated",
      "startSubject",
      "inputPool",
      "relatedSubject",
      "relatedInterface",
      "isImplementation",
      "externalType",
      "role",
      "url",
      "implementations",
      "comment",
      "variables",
      "macros"))
  }

  /**
   * Format for the whole graph object.
   * "roles" map is needed to provide mapping between role name
   * and role id because in graph JSON roles are identified by name
   * not by id.
   */
  implicit def graphJsonFormat(implicit roles: Map[String, Role] = Map()) = new RootJsonFormat[Graph] {
    def write(g: Graph) = JsObject(
      "id" -> g.id.toJson,
      "processId" -> g.processId.toJson,
      "date" -> g.date.toJson,
      "definition" -> JsObject(
        "process" -> g.subjects.values.toJson,
        "conversations" -> g.conversations.toJson,
        "conversationCounter" -> counter(g.conversations),
        "messages" -> g.messages.toJson,
        "messageCounter" -> counter(g.messages),
        "nodeCounter" -> counter(g.subjects)),
      "routings" -> g.routings.toJson)
    def read(v: JsValue) = v.asJsObject.getFields("id",
      "processId",
      "date",
      "definition",
      "routings") match {
        case Seq(id: JsValue,
          processId: JsValue,
          date: JsValue,
          definition: JsObject,
          routings: JsArray) =>
          Graph(id.convertTo[Option[Int]],
            processId.convertTo[Option[Int]],
            date.convertTo[java.sql.Timestamp],
            definition.fields("conversations").convertTo[Map[String, GraphConversation]],
            definition.fields("messages").convertTo[Map[String, GraphMessage]],
            definition.fields("process").convertTo[Seq[GraphSubject]].map(s => (s.id -> s)).toMap,
            routings.convertTo[Seq[GraphRouting]])
        case Seq(definition: JsObject,
          routings: JsArray) => Graph(None,
          None,
          new java.sql.Timestamp(System.currentTimeMillis()),
          definition.fields("conversations").convertTo[Map[String, GraphConversation]],
          definition.fields("messages").convertTo[Map[String, GraphMessage]],
          definition.fields("process").convertTo[Seq[GraphSubject]].map(s => (s.id -> s)).toMap,
          routings.convertTo[Seq[GraphRouting]])
        case x => throw new DeserializationException("Graph expected, but found: " + x)
      }
  }

  /**
   * Extracts the current id counter value from an string id -> entity map.
   * The counter value is max(numericIdSuffix) + 1.
   */
  private def counter[A](map: Map[String, A]): JsNumber =
    counter(map.keys)

  /**
   * Extracts the current id counter value from a collection of string ids.
   * The counter value is max(numericIdSuffix) + 1.
   */
  private def counter(ids: Iterable[String]): JsNumber =
    JsNumber(ids.map(extractCounterValue).foldLeft(0)(Math.max(_, _)) + 1)

  /**
   * Extracts the current id counter value from a collection of numeric ids.
   * The counter value is max(id) + 1.
   */
  private def counter(ids: Iterable[Short]): JsNumber =
    JsNumber(ids.foldLeft(0)(Math.max(_, _)) + 1)

  /**
   * Extracts the numeric suffix of a string id.
   * Id are constructed of an entity identifier string
   * prefix and an unique numeric suffix.
   */
  private def extractCounterValue(s: String) = {
    // take all digits (ascii 48 - 57) from the end of
    // the string and convert to integer 
    s.reverse.takeWhile(c => c >= 48 && c <= 57).reverse match {
      case "" => -1
      case n  => n.toInt
    }
  }

  /**
   * Convert None to *, other values passed through.
   */
  private object NoneAsterisk {
    def apply(o: Option[String]) = JsString(o match {
      case None    => "*"
      case Some(s) => s
    })

    def unapply(v: JsValue) = v match {
      case JsNull        => None
      case JsString("*") => None
      case JsString("")  => None
      case JsString(s)   => Some(s)
    }
  }
}
