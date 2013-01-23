package de.tkip.sbpm.application.history

// message to report a transition in the internal behavior
// to the corresponding subject actor
case class Transition(from: State, to: State, message: Message)