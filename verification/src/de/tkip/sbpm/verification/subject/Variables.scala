package de.tkip.sbpm.verification.subject

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.ProcessModelTypes._

object Variables {
  def empty: Variables = Variables(Map())
}

case class Variables(messageVar: Map[VarName, MessageList]) {

  def getMessageList(name: VarName): MessageList = messageVar(name)
  def getVar(name: VarName) = getMessageList(name)

  def add(name: VarName, messageList: MessageList): Variables =
    Variables(messageVar + (name -> messageList))

  def mkShortString = {
    val messagesStr =
      messageVar.map(t => "\"%s\":%s".format(t._1, t._2.messages.length))
    if (messagesStr.isEmpty) ""
    else messagesStr.mkString("ML: {", ", ", "}, ")
  }

  def mkString = {
    val outputStrings =
      messageVar.map(t => "\"%s\" -> %s".format(t._1, t._2))

    outputStrings.mkString("{", ", ", "}")
  }
}