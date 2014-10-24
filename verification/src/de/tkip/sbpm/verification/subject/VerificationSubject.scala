package de.tkip.sbpm.verification.subject

import de.tkip.sbpm.newmodel.Subject
import de.tkip.sbpm.newmodel.Channel
import de.tkip.sbpm.newmodel.InstantInterface

private[verification] trait VerificationSubject {

  def channel: Channel
  def activeStates: Set[ExtendedState]
}

private[verification] case class VerificationInstantInterface(subject: InstantInterface,
                                                              channel: Channel) extends VerificationSubject {

  def activeStates = Set()

  override def toString = s"${subject.id}"
}