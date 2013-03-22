package de.tkip.sbpm.rest

import de.tkip.sbpm.model._
import spray.json._
import scala.collection.immutable.Map

object GraphJsonProtocol extends DefaultJsonProtocol {

  implicit override def optionFormat[T: JsonFormat] = new OptionFormat[T] {
    override def write(o: Option[T]) = super.write(o)
    override def read(v: JsValue) = v match {
      case JsString("") => None
      case x            => super.read(x)
    }
  }
  
  implicit def stringOptionFormat = new OptionFormat[String]

  implicit object TimestampFormat extends JsonFormat[java.sql.Timestamp] {
    def write(t: java.sql.Timestamp) = JsNumber(t.getTime)
    def read(v: JsValue) = v match {
      case JsNumber(t) => new java.sql.Timestamp(t.toLong)
      case _           => throw new DeserializationException("Timestamp expected.")
    }
  }

  implicit object ChannelsFormat extends RootJsonFormat[Map[String, GraphChannel]] {
    def write(map: Map[String, GraphChannel]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphChannel(id, name))
      case _                    => throw new DeserializationException("Channels map expected.")
    }
  }

  implicit object MessagesFormat extends RootJsonFormat[Map[String, GraphMessage]] {
    def write(map: Map[String, GraphMessage]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphMessage(id, name))
      case _                    => throw new DeserializationException("Messages map expected.")
    }
  }

  implicit object VariablesFormat extends RootJsonFormat[Map[String, GraphVariable]] {
    def write(map: Map[String, GraphVariable]) =
      JsObject(map.values.map(c => (c.id, JsString(c.name))).toSeq: _*)
    def read(v: JsValue) = v.asJsObject.fields.toMap.map {
      case (id, JsString(name)) => (id, GraphVariable(id, name))
      case _                    => throw new DeserializationException("Variables map expected.")
    }
  }

  implicit def optionRoleFormat(implicit roles: Map[String, Role] = Map()) = new JsonFormat[Option[Role]] {
    def write(o: Option[Role]) = o match {
      case None    => JsNull
      case Some(r) => JsString(r.name)
    }
    def read(v: JsValue) = v match {
      case JsString(name) if (roles.contains(name)) => Some(roles(name))
      case JsNull                                   => None
      case _                                        => throw new DeserializationException("Existing role name or null expected.")
    }
  }

  implicit object NodeOptionsFormat extends JsonFormat[GraphNodeOptions] {
    def write(o: GraphNodeOptions) = JsObject(
      "message" -> NoneAsterisk(o.messageId),
      "subject" -> NoneAsterisk(o.subjectId),
      "correlationId" -> o.correlationId.toJson,
      "channel" -> o.channelId.toJson,
      "state" -> o.nodeId.toJson)
    def read(v: JsValue) =
      v.asJsObject.getFields("message", "subject", "correlationId", "channel", "state") match {
        case Seq(msg: JsValue, subj: JsValue, corr: JsValue, ch: JsValue, st: JsValue) =>
          GraphNodeOptions(NoneAsterisk.unapply(msg),
            NoneAsterisk.unapply(subj),
            corr.convertTo[Option[String]],
            ch.convertTo[Option[String]],
            st.convertTo[Option[Short]])
        case _ => GraphNodeOptions()
      }
  }

  implicit val varManFormat = jsonFormat(GraphVarMan,
    "var1", "var2", "operation", "storevar")

  implicit val nodeFormat = jsonFormat(GraphNode, "id",
    "text",
    "start",
    "end",
    "type",
    "deactivated",
    "majorStartNode",
    "channel",
    "variable",
    "options",
    "macro",
    "varMan")

  implicit object NodesFormat extends RootJsonFormat[Map[Short, GraphNode]] {
    def write(map: Map[Short, GraphNode]) = JsArray(map.values.map(_.toJson).toSeq: _*)
    def read(v: JsValue) = v match {
      case a: JsArray => a.elements.map { v =>
        val n = v.convertTo[GraphNode]
        (n.id -> n)
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  implicit val edgeTargetFormat = jsonFormat(GraphEdgeTarget,
    "id", "min", "max", "createNew", "variable")

  implicit val edgeFormat = jsonFormat(GraphEdge, "start",
    "end",
    "text",
    "type",
    "target",
    "deactivated",
    "optional",
    "priority",
    "manualTimeout",
    "variable",
    "correlationId",
    "comment",
    "transportMethod")

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

  implicit val routingExpressionFormat = jsonFormat4(GraphRoutingExpression)
  implicit val routingFormat = jsonFormat3(GraphRouting)

  implicit object macrosFormat extends RootJsonFormat[Map[String, GraphMacro]] {
    def write(ms: Map[String, GraphMacro]) = JsArray(ms.values.map(_.toJson).toSeq: _*)
    def read(v: JsValue) = v match {
      case a: JsArray => a.elements.map { e =>
        val m = e.convertTo[GraphMacro]
        (m.id -> m)
      }.toMap
      case _ => throw new DeserializationException("Array expected.")
    }
  }

  implicit def subjectFormat(implicit roles: Map[String, Role] = Map()) = new RootJsonFormat[GraphSubject] {
    def write(s: GraphSubject) = JsObject(
      "id" -> s.id.toJson,
      "name" -> s.name.toJson,
      "type" -> s.subjectType.toJson,
      "deactivated" -> s.isDisabled.toJson,
      "startSubject" -> s.isStartSubject.toJson,
      "inputPool" -> s.inputPool.toJson,
      "relatedSubject" -> s.relatedSubjectId.toJson,
      "relatedProcess" -> s.relatedGraphId.toJson,
      "externalType" -> s.externalType.toJson,
      "role" -> s.role.toJson,
      "comment" -> s.comment.toJson,
      "variables" -> s.variables.toJson,
      "variableCounter" -> counter(s.variables),
      "macros" -> s.macros.values.toJson,
      "macroCounter" -> counter(s.macros))
    def read(v: JsValue) = v.asJsObject.convertTo[GraphSubject](jsonFormat(GraphSubject, "id", "name",
      "type",
      "deactivated",
      "startSubject",
      "inputPool",
      "relatedSubject",
      "relatedProcess",
      "externalType",
      "role",
      "comment",
      "variables",
      "macros"))
  }

  implicit def graphJsonFormat(implicit roles: Map[String, Role] = Map()) = new RootJsonFormat[Graph] {
    def write(g: Graph) = JsObject(
      "id" -> g.id.toJson,
      "processId" -> g.processId.toJson,
      "date" -> g.date.toJson,
      "definition" -> JsObject(
        "process" -> g.subjects.values.toJson,
        "channels" -> g.channels.toJson,
        "channelCounter" -> counter(g.channels),
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
            definition.fields("channels").convertTo[Map[String, GraphChannel]],
            definition.fields("messages").convertTo[Map[String, GraphMessage]],
            definition.fields("process").convertTo[Seq[GraphSubject]].map(s => (s.id -> s)).toMap,
            routings.convertTo[Seq[GraphRouting]])
        case x => throw new DeserializationException("Graph expected, but found: " + x)
      }
  }

  private def counter[A](map: Map[String, A]): JsNumber =
    counter(map.keys)

  private def counter(ids: Iterable[String]): JsNumber =
    JsNumber(ids.map(extractCounterValue).foldLeft(0)(Math.max(_, _)) + 1)
    
  private def counter(ids: Iterable[Short]): JsNumber =
    JsNumber(ids.foldLeft(0)(Math.max(_, _)) + 1)

  private def extractCounterValue(s: String) = {
    s.reverse.takeWhile(c => c >= 48 && c <= 57).reverse match {
      case "" => 0
      case n  => n.toInt
    }
  }

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