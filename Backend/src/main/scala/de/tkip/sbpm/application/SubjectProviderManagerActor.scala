package de.tkip.sbpm.application

import akka.actor._
import miscellaneous._
import miscellaneous.ProcessAttributes._

class SubjectProviderManagerActor(val processManagerRef: ProcessManagerRef)
  extends Actor {
  private var subjectCount = 0
  private val subjectProviderMap =
    collection.mutable.Map[UserID, SubjectProviderRef]()

  def receive = {
    // create a new subject provider and send the ID to the requester.
    // additionally send it to the subjectprovider who forwards 
    // the message to the processmanager so he can register the new subjectprovider
    case csp: CreateSubjectProvider =>
      createNewSubjectProvider(subjectCount) ! SubjectProviderCreated(csp, subjectCount)
      sender ! SubjectProviderCreated(csp, subjectCount)
      subjectCount += 1

    case gpr: ExecuteRequest =>
      forwardControlMessageToProvider(gpr.userID, gpr)

    case as: AddState =>
      forwardControlMessageToProvider(as.userID, as)

    case cp: CreateProcess =>
      cp.sender = sender
      forwardControlMessageToProvider(cp.userID, cp)

    case pc: ProcessCreated =>
      if (pc.cp.sender != null)
        pc.cp.sender ! pc

    case kill: KillProcess =>
      processManagerRef ! kill

    case _ => "not yet implemented"
  }

  // forward control message to subjectProvider that is mapped to a specific userID
  private def forwardControlMessageToProvider(userID: UserID,
    controlMessage: ControlMessage) {
    if (subjectProviderMap.contains(userID)) {
      subjectProviderMap(userID) ! controlMessage
    }
  }

  // creates a new subject provider and registers it with the given userID 
  // (overrides the old entry)
  def createNewSubjectProvider(userID: UserID) = {
    val subjectProvider =
      context.actorOf(Props(new SubjectProviderActor(userID, processManagerRef)))
    subjectProviderMap += userID -> subjectProvider
    subjectProvider
  }

  // kills the subject provider with the given userID and unregisters it
  def killSubjectProvider(userID: UserID) = {
    if (subjectProviderMap.contains(userID)) {
      context.stop(subjectProviderMap(userID))
      subjectProviderMap -= userID
    }
  }

}