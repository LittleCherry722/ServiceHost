package de.tkip.akkatutorial

sealed abstract class Messages
case class Calculate() extends Messages
case class Work(start: Int, end: Int) extends Messages
case class Result(val e: Double) extends Messages
case class eApproximation(r: Double) extends Messages