package de.tkip.sbpm.application.miscellaneous

import de.tkip.sbpm.model.Role

trait RoleMapper {
  def hasRole(name: String): Boolean
  def getRole(name: String): Option[Role]
}

object RoleMapper {
  def createRoleMapper(roles: scala.collection.immutable.Map[String, Role]): RoleMapper = {
    object mapper extends RoleMapper {
      override def toString = roles.toString
      def hasRole(name: String): Boolean = roles.contains(name)
      def getRole(name: String): Option[Role] = roles.get(name)
    }
    mapper
  }

  //def emptyMapper: RoleMapper = {
    object emptyMapper extends RoleMapper {
      override def toString = "emptyMapper"
      def hasRole(name: String): Boolean = false
      def getRole(name: String): Option[Role] = None
    }
    //mapper
  //}

  //def noneMapper: RoleMapper = {
    object noneMapper extends RoleMapper {
      override def toString = "noneMapper"
      def hasRole(name: String): Boolean = true
      def getRole(name: String): Option[Role] = None
    }
    //mapper
  //}
}
