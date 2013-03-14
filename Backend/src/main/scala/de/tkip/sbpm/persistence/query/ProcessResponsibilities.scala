package de.tkip.sbpm.persistence.query

import de.tkip.sbpm.model.ProcessResponsibility

object ProcessResponsibilities {
  trait Query extends BaseQuery

  object Read {
    def apply() = All
    case object All extends Query
    case class ById(userId: Int, groupId: Int, processId: Int) extends Query
  }

  object Save {
    def apply(procResp: ProcessResponsibility*) = Entity(procResp: _*)
    case class Entity(procResp: ProcessResponsibility*) extends Query
  }

  object Delete {
    def apply(procResp: ProcessResponsibility) = ById(procResp.userId, procResp.roleId, procResp.processId)
    case class ById(userId: Int, groupId: Int, processId: Int) extends Query
  }
}
