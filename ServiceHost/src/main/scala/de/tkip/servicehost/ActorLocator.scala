package de.tkip.servicehost

import akka.actor.ActorRefFactory

object ActorLocator {
  val referenceXMLActorName = "reference-xml-actor"
  val servicehostActorName = "subject-provider-manager"
  val serviceActorManagerName = "service-actor-manager"
    
  def actor(name: String)(implicit ctx: ActorRefFactory) = ctx.actorFor("/user/" + name)

  def referenceXMLActor(implicit ctx: ActorRefFactory) = actor(referenceXMLActorName)
  def serviceHost(implicit ctx: ActorRefFactory) = actor(servicehostActorName)
  def serviceActorManager(implicit ctx: ActorRefFactory) = actor(serviceActorManagerName)
}