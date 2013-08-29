package de.tkip.sbpm.application.change

import akka.actor.Actor
import de.tkip.sbpm.persistence.query.Processes._
import de.tkip.sbpm.model._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import akka.pattern.pipe

case class GetProcessChange(timeStamp: Long)

case class GetActionChange(timeStamp: Long)

class ChangeActor extends Actor {

  val processChangeEntries = new ArrayBuffer[ProcessChangeData]()
  
  val actionChangeEntries = new ArrayBuffer[ActionChangeData]()

  implicit val ec = context.dispatcher

  def receive = {

    case q: ProcessChangeData => {
      println("add process change data : " + q.toString())
      addProcessChangeData(q)
    }

    case GetProcessChange(t) => {
      Future { getProcessChange(t) } pipeTo sender
    }
    
    case q: ActionChangeData => {
      println("add action change data : " + q.toString())
      addActionChangeData(q)
    }
    
    case GetActionChange(t) => {
      Future { getActionChange(t) } pipeTo sender
    }

  }

  private def addProcessChangeData(p: ProcessChangeData) = {
    processChangeEntries += p
  }
  
  private def addActionChangeData(a: ActionChangeData) = {
    actionChangeEntries += a
  }

  private def getProcessChange(t: Long) = {
    val resultHead = """"process":"""
    val insertHead = """"inserted":"""
    val updateHead = """"updated":"""
    val deleteHead = """"deleted":"""
    var result = new ArrayBuffer[String]()
    var tempInsert = new ArrayBuffer[String]()
    var tempUpdate = new ArrayBuffer[String]()
    var tempDelete = new ArrayBuffer[String]()
    for (i <- 0 until processChangeEntries.length) {
      processChangeEntries(i) match {
        case ProcessChange(p, info, date) => {
          if (date.getTime() > t * 1000) {
            if (info == "insert")
              tempInsert += """{ "id": """ + p.id.get + """, "name": """" + p.name + """"}"""
            if (info == "update")
              tempUpdate += """{ "id": """ + p.id.get + """, "name": """" + p.name + """"}"""
          }
        }
        case ProcessDelete(id, date) => {
          if (date.getTime() > t * 1000) {
            tempDelete += """{"id" :""" + id + """}"""
          }
        }
      }
    }

      if (tempInsert.length > 0) {
        result += insertHead + tempInsert.mkString("[", ",", "]")
      }
      if (tempUpdate.length > 0) {
        result += updateHead + tempInsert.mkString("[", ",", "]")
      }
      if (tempDelete.length > 0) {
        result += deleteHead + tempInsert.mkString("[", ",", "]")
      }
    if (result.length > 0)
      resultHead + result.mkString("{",",","}")
    else ""

  }
  
  private def getActionChange(t: Long) = {
    val resultHead = """"action":"""
    val insertHead = """"inserted":"""
    val updateHead = """"updated":"""
    val deleteHead = """"deleted":"""
    var result = new ArrayBuffer[String]()
    var tempInsert = new ArrayBuffer[String]()
    var tempUpdate = new ArrayBuffer[String]()
    var tempDelete = new ArrayBuffer[String]()
    for (i <- 0 until actionChangeEntries.length) {
      actionChangeEntries(i) match {
        case ActionChange(a, info, date) => {
          if (date.getTime() > t * 1000) {
            if (info == "insert")
              tempInsert += """{ "id": """ + a.id.get + """, "data": """" + a.data + """"}"""
            if (info == "update")
              tempUpdate += """{ "id": """ + a.id.get + """, "data": """" + a.data + """"}"""
            if (info == "delete")
              tempDelete += """{ "id": """ + a.id.get + """, "data": """" + a.data + """"}"""
          }
        }
      }
    }

      if (tempInsert.length > 0) {
        result += insertHead + tempInsert.mkString("[", ",", "]")
      }
      if (tempUpdate.length > 0) {
        result += updateHead + tempInsert.mkString("[", ",", "]")
      }
      if (tempDelete.length > 0) {
        result += deleteHead + tempInsert.mkString("[", ",", "]")
      }
    if (result.length > 0)
      resultHead + result.mkString("{",",","}")
    else ""

  }

}