package de.tkip.servicehost.serviceactor.stubgen

import scala.collection.immutable.List;

trait State {
  
	def apply(startState: State, endState: State, args : List[Any])	
	
	def process()
	
}

object ReceiveState extends State
object SendState extends State
object ExitState extends State
