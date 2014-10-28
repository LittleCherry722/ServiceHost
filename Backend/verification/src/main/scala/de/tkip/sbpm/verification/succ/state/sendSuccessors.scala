package de.tkip.sbpm.verification.succ.state

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.verification.lts.{LtsState, SendLabel}
import de.tkip.sbpm.verification.subject._
import de.tkip.sbpm.verification.succ._

/**
 * Creates the Successors for SendStates
 */
object sendSuccessors extends StateSuccessorFunction {

  def apply(global: GlobalFunctions,
            ltsState: LtsState,
            subject: SubjectStatus,
            state: ExtendedState): Set[Successor] = {

    val successors: Set[Successor] =
      for {
        transition <- state.communicationTransitions
        params: CommunicationParams = transition.exitParams.asInstanceOf[CommunicationParams]

        subjectMap = ltsState.normalSubjectMap
        channel = subject.channel

        channelVar = params.channelVar
        messageType = params.messageType

        messageContent: MessageContent = params.contentVarName.map(
          subject.variables.getMessageList
        ).getOrElse(NoContent)

      } yield {

        val otherModel = global.model.subjectOrInterFace(params.subject)
        otherModel match {
          case s: Subject => {

            global.model.subject(params.subject).multi match {
              // Send to MultiSubject
              case true => {
                val targetChannels = (state.data, channelVar) match {
                  case (Some(SendStateData(remaining)), _) => {
                    remaining.map(Channel(params.subject, _))
                  }
                  case (_, Some(name)) => {
                    subject.variables.getMessageList(name).channels
                  }
                  case _ => {
                    throw new Exception("A send to a MultiSubject musst be a Send to Var")
                  }
                }
                // check if all target Subjects exists
                if (!targetChannels.forall(subjectMap.contains(_)))
                  LtsSuccessor(null)
                else {
                  val targetSubjects = targetChannels.map(subjectMap(_))
                  // Send the Message to the TargetSubjects, where it is possible
                  val (sendPossible, remaining) =
                    targetSubjects.partition(_.hasInputPoolSpace(messageType, channel))

                  // send the message where it is Possible
                  val newTargetSubjects =
                    sendPossible.map(_.putMessage(messageContent, messageType, channel))

                  // store the remaining Subjects in a var
                  val newSendStateData = SendStateData(remaining.map(_.channel.agentId))

                  // if the storeVar is defined, add the targetSubject into a Variable
                  val subjectWithVar =
                    if (params.storeVar.isDefined) {
                      val messageList =
                        MessageList(
                          targetSubjects.map(s => Message(messageContent, s.channel)) toList)
                      subject.addVar(params.storeVar.get, messageList)
                    } else subject

                  val newSubj =
                    if (remaining.size == 0) {
                      subjectWithVar.fireTransition(state, transition)
                    } else {
                      subjectWithVar.setStateData(state, newSendStateData)
                    }

                  val nums = targetChannels.map(_.agentId)
                  val label = SendLabel(subject.id, subject.channel.agentId, messageType, params.subject, nums)
                  ltsState.successor(newTargetSubjects + newSubj, transition).copy(label = label)
                }
              }

              // Send to SingleSubject
              case false => {
                val targetChannel: Channel = channelVar match {
                  case Some(name) => {
                    val targetChannels = subject.variables.getMessageList(name).channels

                    val filtered = targetChannels.filter(_.subjectId == params.subject)
                    // muss be length == 1 ?
                    assert(filtered.size == 1, "The Variable musst contain the SingeSubject?")
                    filtered.head
                  }
                  case None => Channel(params.subject, 0)
                }

                val targetSubject =
                  subjectMap.getOrElse(targetChannel, global.createSubject(targetChannel))

                // if it has Space send, the Message
                if (targetSubject.hasInputPoolSpace(messageType, channel)) {
                  // Send the message
                  val newTargetSubject =
                    targetSubject.putMessage(messageContent, messageType, channel)
                  // fire the send Transition
                  val successorSubj =
                    subject.fireTransition(state, transition)

                  // if the storeVar is defined, add the targetSubject into a Variable
                  val newSubjWithVar =
                    if (params.storeVar.isDefined) {
                      val messageList =
                        MessageList(List(Message(messageContent, targetSubject.channel)))
                      successorSubj.addVar(params.storeVar.get, messageList)
                    } else successorSubj

                  val label = SendLabel(subject.id, subject.channel.agentId, messageType, targetSubject.id, Set(0))
                  ltsState.successor(Set(newSubjWithVar, newTargetSubject), transition)
                    .copy(label = label)
                } else {
                  LtsSuccessor(null)
                }
              }
            }
          }

          case i: InstantInterface => {
            val m = params.max match {case Number(n) => n; case _ => 0}
            val nums = (0 until m).toSet
            val newSubject = subject.fireTransition(state, transition)
            val label = SendLabel(subject.id, subject.channel.agentId, messageType, params.subject, nums)
            ltsState.successor(newSubject, transition).copy(label = label)
          }
        }
      }
    successors.filter { case s: LtsSuccessor => s.state != null; case _ => true }
  }
}