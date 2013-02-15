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

  def actor(name: String)(implicit ctx: ActorRefFactory) = ctx.actorFor("/user/" + name)

  def persistenceActor(implicit ctx: ActorRefFactory) = actor(persistenceActorName)
  def contextResolverActor(implicit ctx: ActorRefFactory) = actor(contextResolverActorName)
  def processManagerActor(implicit ctx: ActorRefFactory) = actor(processManagerActorName)
  def subjectProviderManagerActor(implicit ctx: ActorRefFactory) = actor(subjectProviderManagerActorName)
  def sessionActor(implicit ctx: ActorRefFactory) = actor(sessionActorName)
  def userPassAuthActor(implicit ctx: ActorRefFactory) = actor(userPassAuthActorName)
  def googlAuthActor(implicit ctx: ActorRefFactory) = actor(googleAuthActorName)
}
