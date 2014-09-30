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

package de.tkip.sbpm.model

import spray.httpx.SprayJsonSupport
import spray.json._
import scala.collection.immutable.Map

/**
 * Provides conversion from Graph domain model
 * to JSON and vice versa.
 */
object GraphJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

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
  implicit val nodeFormat = jsonFormat(GraphNode,
    "id",
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
    "chooseAgentSubject",
    "macro",
    "blackboxname",
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
        n.id -> n
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  /**
   * Format for edge's target object.
   */
  implicit val edgeTargetFormat = jsonFormat(GraphEdgeTarget,
    "id", "exchangeOriginId", "exchangeTargetId", "min", "max", "createNew", "variable")

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
   * Convert macros map (domain model) to macro array (JSON) and vice versa.
   */
  implicit object macrosFormat extends RootJsonFormat[Map[String, GraphMacro]] {
    def write(ms: Map[String, GraphMacro]) =
      JsArray(ms.values.map(_.toJson).toSeq: _*)
    def read(v: JsValue) = v match {
      case a: JsArray => a.elements.map { e =>
        val m = e.convertTo[GraphMacro]
        m.id -> m
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  implicit val addressFormat = jsonFormat3(Address)
  implicit val interfaceImplementationFormat = jsonFormat3(InterfaceImplementation)

  implicit val mergedSubjectsFormat = jsonFormat2(MergedSubject)

  /**
   * Format for a subject object.
   * Counter values are calculated on the fly when converting to JSON
   * and ignored while converting from JSON to domain model.
   */
  implicit object subjectFormat extends RootJsonFormat[GraphSubject] {
    def write(s: GraphSubject) = JsObject(
      "id" -> s.id.toJson,
      "name" -> s.name.toJson,
      "type" -> s.subjectType.toJson,
      "mergedSubjects" -> s.mergedSubjects.toJson,
      "deactivated" -> s.isDisabled.toJson,
      "startSubject" -> s.isStartSubject.toJson,
      "inputPool" -> s.inputPool.toJson,
      "blackboxname" -> s.blackboxname.toJson,
      "relatedSubject" -> s.relatedSubjectId.toJson,
      "relatedInterface" -> s.relatedInterfaceId.toJson,
      "isImplementation" -> s.isImplementation.toJson,
      "externalType" -> s.externalType.toJson,
      "role" -> s.role.toJson,
      "implementations" -> s.implementations.toJson,
      "comment" -> s.comment.toJson,
      "variables" -> s.variables.toJson,
      // extract counter value from variable ids
      "variableCounter" -> counter(s.variables),
      "macros" -> s.macros.values.toJson,
      // extract counter value fro∂m macro ids
      "macroCounter" -> counter(s.macros))
    def read(v: JsValue) = v.asJsObject.convertTo[GraphSubject](jsonFormat(GraphSubject,
      "id",
      "name",
      "type",
      "mergedSubjects",
      "deactivated",
      "startSubject",
      "inputPool",
      "blackboxname",
      "relatedSubject",
      "relatedInterface",
      "isImplementation",
      "externalType",
      "role",
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
  implicit object graphJsonFormat extends RootJsonFormat[Graph] {
    def write(g: Graph) = JsObject(
        "process" -> g.subjects.values.toJson,
        "conversations" -> g.conversations.toJson,
        "conversationCounter" -> counter(g.conversations),
        "messages" -> g.messages.toJson,
        "messageCounter" -> counter(g.messages),
        "nodeCounter" -> counter(g.subjects))
    def read(v: JsValue) = v.asJsObject.getFields("id",
      "processId",
      "definition",
      "routings") match {
        case Seq(id: JsValue,
          processId: JsValue,
          definition: JsObject,
          routings: JsArray) =>
          Graph(
            id            = None,
            conversations = definition.fields("conversations").convertTo[Map[String, GraphConversation]],
            messages      = definition.fields("messages").convertTo[Map[String, GraphMessage]],
            subjects      = definition.fields("process").convertTo[Seq[GraphSubject]].map(s => s.id -> s).toMap)
        case Seq(definition: JsObject,
          routings: JsArray) => Graph(
          id            = None,
          conversations = definition.fields("conversations").convertTo[Map[String, GraphConversation]],
          messages      = definition.fields("messages").convertTo[Map[String, GraphMessage]],
          subjects      = definition.fields("process").convertTo[Seq[GraphSubject]].map(s => s.id -> s).toMap)
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
    JsNumber(ids.map(extractCounterValue).foldLeft(0)(Math.max) + 1)

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
      case _             => None
    }
  }
}
