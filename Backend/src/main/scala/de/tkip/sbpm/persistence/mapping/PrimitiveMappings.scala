package de.tkip.sbpm.persistence.mapping

import shapeless._
import de.tkip.sbpm.{ model => domainModel }

object PrimitiveMappings {
  object Persistence {
    val user = Iso.hlist(User.apply _, User.unapply _)
    val group = Iso.hlist(Group.apply _, Group.unapply _)
    val role = Iso.hlist(Role.apply _, Role.unapply _)
    val processInstance = Iso.hlist(ProcessInstance.apply _, ProcessInstance.unapply _)
    val groupRole = Iso.hlist(GroupRole.apply _, GroupRole.unapply _)
    val groupUser = Iso.hlist(GroupUser.apply _, GroupUser.unapply _)
    val message = Iso.hlist(Message.apply _, Message.unapply _)
    val configuration = Iso.hlist(Configuration.apply _, Configuration.unapply _)
  }

  object Domain {
    val user = Iso.hlist(domainModel.User.apply _, domainModel.User.unapply _)
    val group = Iso.hlist(domainModel.Group.apply _, domainModel.Group.unapply _)
    val role = Iso.hlist(domainModel.Role.apply _, domainModel.Role.unapply _)
    val processInstance = Iso.hlist(domainModel.ProcessInstance.apply _, domainModel.ProcessInstance.unapply _)
    val groupRole = Iso.hlist(domainModel.GroupRole.apply _, domainModel.GroupRole.unapply _)
    val groupUser = Iso.hlist(domainModel.GroupUser.apply _, domainModel.GroupUser.unapply _)
    val message = Iso.hlist(domainModel.Message.apply _, domainModel.Message.unapply _)
    val configuration = Iso.hlist(domainModel.Configuration.apply _, domainModel.Configuration.unapply _)
  }

  def convert[A, B, L <: HList](a: A, fromIso: Iso[A, L], toIso: Iso[B, L]): B =
    toIso.from(fromIso.to(a))

  def convert[A, B, L <: HList](a: Option[A], fromIso: Iso[A, L], toIso: Iso[B, L]): Option[B] =
    a match {
      case None => None
      case Some(aa) => Some(convert(aa, fromIso, toIso))
    }

}