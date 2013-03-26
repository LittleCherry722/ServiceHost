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
  val contextResolverActorName = "context-resolver"
  val processManagerActorName = "process-manager"
  val subjectProviderManagerActorName = "subject-provider-manager"
  val frontendInterfaceActorName = "frontend-interface"
  val sessionActorName = "session"
  val basicAuthActorName = "basic-auth"
  val oAuth2ActorName = "o-auth"
  val userPassAuthActorName = "user-pass-auth"
  val googleAuthActorName = "google-auth"
  val googleDriveActorName = "google-drive"
  val googleUserInformationActorName = "google-user-info"

  def actor(name: String)(implicit ctx: ActorRefFactory) = ctx.actorFor("/user/" + name)

  def persistenceActor(implicit ctx: ActorRefFactory) = actor(persistenceActorName)
  def contextResolverActor(implicit ctx: ActorRefFactory) = actor(contextResolverActorName)
  def processManagerActor(implicit ctx: ActorRefFactory) = actor(processManagerActorName)
  def subjectProviderManagerActor(implicit ctx: ActorRefFactory) = actor(subjectProviderManagerActorName)
  def sessionActor(implicit ctx: ActorRefFactory) = actor(sessionActorName)
  def userPassAuthActor(implicit ctx: ActorRefFactory) = actor(userPassAuthActorName)
  def googleAuthActor(implicit ctx: ActorRefFactory) = actor(googleAuthActorName)
  def googleDriveActor(implicit ctx: ActorRefFactory) = actor(googleDriveActorName)
  def googleUserInformationActor(implicit ctx: ActorRefFactory) = actor(googleUserInformationActorName)
}
