package de.tkip.sbpm.newmodel

sealed trait MessageContentType
case class VariableContent(variableName: String, content: OrdinaryMessageContentType)
  extends MessageContentType
trait OrdinaryMessageContentType extends MessageContentType

// Some Example Types
case object NoContentType extends OrdinaryMessageContentType with MessageContent
case object TextContentType extends OrdinaryMessageContentType
case object FileContentType extends OrdinaryMessageContentType