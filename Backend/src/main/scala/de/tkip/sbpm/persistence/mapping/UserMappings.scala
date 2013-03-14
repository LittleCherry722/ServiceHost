package de.tkip.sbpm.persistence.mapping
import de.tkip.sbpm.{ model => domainModel }
import PrimitiveMappings._

object UserMappings {
  def convert(i: UserIdentity, u: User): domainModel.UserIdentity =
    domainModel.UserIdentity(
      PrimitiveMappings.convert(u, Persistence.user, Domain.user),
      i.provider,
      i.eMail,
      i.password)
}