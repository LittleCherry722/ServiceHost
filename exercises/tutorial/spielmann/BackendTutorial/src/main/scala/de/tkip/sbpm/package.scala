package de.tkip

package object sbpm {

  type SubjectID = Int
  type StateID = Int
  type StateType = String
  
  val Receive = "receive"
  val Send = "send"
  val Act = "act"
}