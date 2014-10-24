package de.tkip.sbpm.newmodel

object ProcessModelTypes {
  type ProcessId = Int
  type SubjectId = String
  type MessageType = String
  type StateId = Int
  type AgentId = Int
  type VarName = String
}

object StateTypes {
  type StateType = String
  val Act = "action"
  val Receive = "receive"
  val Observer = "observer"
  val Send = "send"
  val End = "end" // the endstate if the subject or the endstate of a macro 
  val Split = "msplit"
  val SplitGuard = "gsplit" // der Guard bei optionalen split transitions
  val Join = "mjoin"
  val Function = "function state"
}

object Operation {
  type Operation = String
  // concats two variables
  val Concatenation = "union"
  // extracts the content of a message to store it into a variable
  val ExtractMessageContent = "extract"
  // select a sublist of the current list
  // might be user or automatic filter selection
  val Selection = "select"

  // set difference
  val Difference = "difference"
}
