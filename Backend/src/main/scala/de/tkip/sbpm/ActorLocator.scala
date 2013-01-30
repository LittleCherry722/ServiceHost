package de.tkip.sbpm

import akka.actor.ActorContext

object ActorLocator {
  val persistenceActorName = "persistence"
  val contextResolverActorName = "context-resolver"
  val processManagerActorName = "process-manager"
  val subjectProviderManagerActorName = "subject-provider-manager"
  val frontendInterfaceActorName = "frontend-interface"

  def actor(name: String)(implicit ctx: ActorContext) = ctx.actorFor("/user/" + name)

  def persistenceActor(implicit ctx: ActorContext) = actor(persistenceActorName)
  def contextResolverActor(implicit ctx: ActorContext) = actor(contextResolverActorName)
  def processManagerActor(implicit ctx: ActorContext) = actor(processManagerActorName)
  def subjectProviderManagerActor(implicit ctx: ActorContext) = actor(subjectProviderManagerActorName)
}
