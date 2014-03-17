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

package de.tkip.sbpm

import akka.actor.ActorRefFactory

object ActorLocator {
  val persistenceActorName = "persistence"
  val logPersistenceActorName = "logPersistence"
  val contextResolverActorName = "context-resolver"
  val processManagerActorName = "process-manager"
  val subjectProviderManagerActorName = "subject-provider-manager"
  val frontendInterfaceActorName = "frontend-interface"
  val sessionActorName = "session"
  val basicAuthActorName = "basic-auth"
  val oAuth2ActorName = "o-auth"
  val userPassAuthActorName = "user-pass-auth"
  val googleDriveActorName = "google-drive"
  val googleCalendarActorName = "google-calendar"
  val googleBIRActorName = "google-BIR"
  val changeActorName = "change"
  val eventBusRemotePublishActorName = "eventbus-remote-publish"

  def actor(name: String)(implicit ctx: ActorRefFactory) = ctx.actorFor("/user/" + name)

  def persistenceActor(implicit ctx: ActorRefFactory) = actor(persistenceActorName)
  def logPersistenceActor(implicit ctx: ActorRefFactory) = actor(logPersistenceActorName)
  def contextResolverActor(implicit ctx: ActorRefFactory) = actor(contextResolverActorName)
  def processManagerActor(implicit ctx: ActorRefFactory) = actor(processManagerActorName)
  def subjectProviderManagerActor(implicit ctx: ActorRefFactory) = actor(subjectProviderManagerActorName)
  def sessionActor(implicit ctx: ActorRefFactory) = actor(sessionActorName)
  def userPassAuthActor(implicit ctx: ActorRefFactory) = actor(userPassAuthActorName)
  def googleDriveActor(implicit ctx: ActorRefFactory) = actor(googleDriveActorName)
  def googleCalendarActor(implicit ctx: ActorRefFactory) = actor(googleCalendarActorName)
  def googleBIRActor(implicit ctx: ActorRefFactory) = actor(googleBIRActorName)
  def changeActor(implicit ctx: ActorRefFactory) = actor(changeActorName)
  def eventBusRemotePublishActor(implicit ctx: ActorRefFactory) = actor(eventBusRemotePublishActorName)
}

