package de.tkip.sbpm.newmodel

import ProcessModelTypes._

// The Channel is the ID of a Subject Instance
case class Channel(subjectId: SubjectId,
                   agentId: AgentId)