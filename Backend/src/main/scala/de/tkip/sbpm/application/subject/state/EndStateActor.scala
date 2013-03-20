package de.tkip.sbpm.application.subject.state


import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.Array.canBuildFrom
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import de.tkip.sbpm.application.miscellaneous._
import de.tkip.sbpm.application.miscellaneous.ProcessAttributes._
import de.tkip.sbpm.application.history.{
  Transition => HistoryTransition,
  Message => HistoryMessage,
  State => HistoryState
}
import de.tkip.sbpm.ActorLocator
import de.tkip.sbpm.application.SubjectInformation
import de.tkip.sbpm.application.RequestUserID
import de.tkip.sbpm.model._
import de.tkip.sbpm.model.StateType._
import de.tkip.sbpm.application.miscellaneous.MarshallingAttributes._
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer
import de.tkip.sbpm.application.subject.StateData
import de.tkip.sbpm.application.subject.ExecuteAction
import de.tkip.sbpm.application.subject.BehaviorStateActor
import de.tkip.sbpm.application.subject.ActionExecuted
import de.tkip.sbpm.application.subject.ActionData
import de.tkip.sbpm.application.subject.SubjectTerminated

protected case class EndStateActor(data: StateData)
  extends BehaviorStateActor(data) {

  // Inform the processinstance that this subject has terminated
  internalBehaviorActor ! SubjectTerminated(userID, subjectID)

  // nothing to receive for this state
  protected def stateReceive = FSM.NullFunction

  override def postStop() {
    logger.debug("End@" + userID + ", " + subjectID + "stops...")
  }

  override protected def getAvailableAction: Array[ActionData] =
    Array()
}