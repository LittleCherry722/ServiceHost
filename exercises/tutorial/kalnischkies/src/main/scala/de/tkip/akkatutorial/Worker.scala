package de.tkip.akkatutorial

import akka.actor._

class Worker extends Actor {
  // see my Exercise 12 in Scala for the Impatient
  def fac(x: Int) = 1.to(x).foldLeft(1)( _ * _)

  private def get_result(start: Int, end: Int) : Double =
    (for (x <- start.until(end)) yield 1.0 / fac(x)).sum
  
  def receive = {
    case Work(start: Int, end: Int) => sender ! new Result(get_result(start, end))
  }
}