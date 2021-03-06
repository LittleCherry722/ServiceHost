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

package de.tkip.sbpm.application.change

import de.tkip.sbpm.instrumentation.InstrumentedActor
import de.tkip.sbpm.persistence.query.Processes._
import de.tkip.sbpm.model._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import akka.pattern.pipe

case class GetProcessChange(timeStamp: Long)

case class GetActionChange(timeStamp: Long)

case class GetProcessInstanceChange(timeStamp: Long)

case class GetMessageChange(timeStamp: Long, userID: Int)

class ChangeActor extends InstrumentedActor {

  val processChangeEntries = new ArrayBuffer[ProcessChangeData]()

  val actionChangeEntries = new ArrayBuffer[ActionChangeData]()

  val processInstanceChangeEntries = new ArrayBuffer[ProcessInstanceChangeData]()

  val messageChangeEntries = new ArrayBuffer[MessageChangeData]()

  implicit val ec = context.dispatcher

  def wrappedReceive = {

    case q: ProcessChangeData => {
      println("add process change data : " + q.toString())
      addProcessChangeData(q)
    }

    case GetProcessChange(t) => {
      Future { getProcessData(t) } pipeTo sender
    }

    case q: ActionChangeData => {
      println("add action change data : " + q.toString())
      addActionChangeData(q)
    }

    case q: ProcessInstanceChangeData => {
      println("add processInstance change data: "+ q.toString())
      addProcessInstanceChangeData(q)
    }

    case GetActionChange(t) => {
      Future { getActionData(t) } pipeTo sender
    }

    case GetProcessInstanceChange(t) => {
      Future { getProcessInstanceData(t) } pipeTo sender
    }

    case q: MessageChangeData => {
      println("add message change data: "+ q.toString())
      addMessageChangeData(q)
    }

    case GetMessageChange(t, id) => {
      Future { getMessageData(t, id) } pipeTo sender
    }

  }

  private def addProcessChangeData(p: ProcessChangeData) = {
    processChangeEntries += p
  }

  private def addActionChangeData(a: ActionChangeData) = {
    actionChangeEntries += a
  }

  private def addProcessInstanceChangeData(p: ProcessInstanceChangeData) = {
    processInstanceChangeEntries += p
  }

  private def addMessageChangeData(m: MessageChangeData) = {
    messageChangeEntries += m
  }

  private def getProcessData(t: Long) = {

    val tempInsert = new ArrayBuffer[ProcessRelatedChangeData]()
    val tempUpdate = new ArrayBuffer[ProcessRelatedChangeData]()
    val tempDelete = new ArrayBuffer[ProcessRelatedDeleteData]()
    val lastUpdate = new java.util.Date().getTime() - t * 1000

    for (i <- 0 until processChangeEntries.length) {
      processChangeEntries(i) match {
        case ProcessChange(p, info, date) => {
          if (date.getTime() > lastUpdate) {
            if (info == "insert")
              tempInsert += ProcessRelatedChangeData(p.id.get,p.interfaceId,p.name,p.isCase,p.startAble.get,p.activeGraphId)
            if (info == "update")
              tempUpdate += ProcessRelatedChangeData(p.id.get,p.interfaceId,p.name,p.isCase,p.startAble.get,p.activeGraphId)
          }
        }
        case ProcessDelete(id, date) => {
          if (date.getTime() > lastUpdate) {
            tempDelete += ProcessRelatedDeleteData(id)
          }
        }
      }
    }

    Some(ProcessRelatedChange(Some(tempInsert.toArray),Some(tempUpdate.toArray),Some(tempDelete.toArray)))


  }

  private def getProcessInstanceData(t: Long) = {

    val tempInsert = new ArrayBuffer[ProcessInstanceRelatedChangeData]()
    val tempUpdate = new ArrayBuffer[ProcessInstanceRelatedChangeData]()
    val tempDelete = new ArrayBuffer[ProcessInstanceRelatedDeleteData]()
    val lastUpdate = new java.util.Date().getTime() - t * 1000

    for (i <- 0 until processInstanceChangeEntries.length) {
      processInstanceChangeEntries(i) match {
        case ProcessInstanceChange(id, pid, pname, name, info, date) => {
          if (date.getTime() > lastUpdate) {
            if (info == "insert")
              tempInsert += ProcessInstanceRelatedChangeData(id,pid,pname,name)
            if (info == "update")
              tempUpdate += ProcessInstanceRelatedChangeData(id,pid,pname,name)
          }
        }
        case ProcessInstanceDelete(id, date) => {
          if (date.getTime() > lastUpdate) {
            tempDelete += ProcessInstanceRelatedDeleteData(id)
          }
        }
      }
    }

    Some(ProcessInstanceRelatedChange(Some(tempInsert.toArray),Some(tempUpdate.toArray),Some(tempDelete.toArray)))
  }

    private def getActionData(t: Long) = {

    val tempInsert = new ArrayBuffer[ActionRelatedChangeData]()
    val tempUpdate = new ArrayBuffer[ActionRelatedChangeData]()
    val tempDelete = new ArrayBuffer[ActionRelatedDeleteData]()
    val lastUpdate = new java.util.Date().getTime() - t * 1000

    for (i <- 0 until actionChangeEntries.length) {
      actionChangeEntries(i) match {
        case ActionChange(a, info, date) => {
          if (date.getTime() > lastUpdate) {
            if (info == "insert")
              tempInsert += ActionRelatedChangeData(a.id,a.userID,a.processInstanceID,a.subjectID,a.macroID,a.stateID,a.stateText,a.stateType,a.actionData)
            if (info == "updated")
              tempUpdate += ActionRelatedChangeData(a.id,a.userID,a.processInstanceID,a.subjectID,a.macroID,a.stateID,a.stateText,a.stateType,a.actionData)
          }
        }
        case ActionDelete(id, date) => {
          if (date.getTime() > lastUpdate) {
            tempDelete += ActionRelatedDeleteData(id)
          }
        }
      }
    }

    Some(ActionRelatedChange(Some(tempInsert.toArray),Some(tempUpdate.toArray),Some(tempDelete.toArray)))


  }

  private def getMessageData(t: Long, id: Int) = {

    val tempInsert = new ArrayBuffer[MessageRelatedChangeData]()
    val lastUpdate = new java.util.Date().getTime() - t * 1000

    for (i <- 0 until messageChangeEntries.length) {
      messageChangeEntries(i) match {
        case MessageChange(m, info, date) => {
          if (date.getTime() > lastUpdate) {
            if (info == "insert" && m.fromUser == id)
              tempInsert += MessageRelatedChangeData(m.id, m.fromUser, m.toUser, m.title, m.isRead, m.content, m.date)
          }
        }
      }
    }

    Some(MessageRelatedChange(Some(tempInsert.toArray)))


  }



}
