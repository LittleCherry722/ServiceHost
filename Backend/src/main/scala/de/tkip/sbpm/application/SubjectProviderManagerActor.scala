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

package de.tkip.sbpm.application

import de.tkip.sbpm.instrumentation.InstrumentedActor
import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.subject._
import de.tkip.sbpm.application.miscellaneous.AnswerAbleMessage
import de.tkip.sbpm.ActorLocator
import akka.event.Logging
import java.util.UUID

class SubjectProviderManagerActor extends InstrumentedActor {

  implicit val config = context.system.settings.config

  private lazy val processManagerActor = ActorLocator.processManagerActor
  private val subjectProviderMap =
    collection.mutable.Map[UserID, SubjectProviderRef]()

  def wrappedReceive = {
    // create a new subject provider and send the ID to the requester.
    // additionally send it to the subjectprovider who forwards
    // the message to the processmanager so he can register the new subjectprovider
    case csp @ CreateSubjectProvider(userID) =>
      createNewSubjectProvider(userID)
      if (subjectProviderMap.contains(userID)) {
        sender !! SubjectProviderCreated(csp, userID)
      }

    // general matching:
    // first match the answers
    // then SubjectProviderMessages
    case answer: AnswerMessage =>
      if (answer.sender != null) {
        answer.sender !! answer
      }

    // TODO werden noch zu forwards aber zum routing testen erstmal tells
    case message: SubjectProviderMessage =>
      if (subjectProviderMap.contains(message.userID)) {
        subjectProviderMap(message.userID) ! withSender(message)
      } else {
        // TODO dynamisch erstellen?
        log.info("creating new subject provider")
        createNewSubjectProvider(message.userID)
        subjectProviderMap(message.userID).forward(withSender(message))
      }

    case message: CreateProcessInstance =>
      processManagerActor ! message.withSender(sender)

    // TODO muss man zusammenfassen koennen
    case message: AnswerAbleMessage =>
      processManagerActor ! message.withSender(sender)

    case message: ControlMessage =>
      processManagerActor ! message

    case message: SubjectMessage =>
      processManagerActor ! message

    case s =>
      println("SubjectProviderManger not yet implemented: " + s)
  }

  /**
   * Sets the sender of the message if the message is AnswerAble
   * and returns the message
   */
  private def withSender(message: Any) = {
    message match {
      case answerAble: AnswerAbleMessage => answerAble.sender = sender
      case _                             =>
    }
    message
  }

  // creates a new subject provider and registers it with the given userID
  // (overrides the old entry)
  private def createNewSubjectProvider(userID: UserID) = {
    val subjectProvider =
      context.actorOf(Props(new SubjectProviderActor(userID)), "SubjectProviderActor____" + UUID.randomUUID().toString)
    subjectProviderMap += userID -> subjectProvider
    subjectProvider
  }

  // kills the subject provider with the given userID and unregisters it
  private def killSubjectProvider(userID: UserID) = {
    if (subjectProviderMap.contains(userID)) {
      context.stop(subjectProviderMap(userID))
      subjectProviderMap -= userID
    }
  }
}
