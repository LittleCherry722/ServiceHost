package de.tkip.sbpm.model


case class GraphSubject(id: String,
  name: String,
  subjectType: String,
  isDisabled: Boolean,
  isStartSubject: Option[Boolean],
  inputPool: Short,
  relatedSubjectId: Option[String],
  relatedInterfaceId: Option[Int],
  isImplementation: Option[Boolean],
  externalType: Option[String],
  role: Option[String],
  url: Option[String],
  implementations: Option[List[InterfaceImplementation]],
  comment: Option[String],
  variables: Map[String, GraphVariable],
  macros: Map[String, GraphMacro])