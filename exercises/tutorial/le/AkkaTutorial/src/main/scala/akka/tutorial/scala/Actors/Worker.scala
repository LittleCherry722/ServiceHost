package akka.tutorial.scala.Actors

import akka.actor.Actor
import akka.tutorial.scala.message.Work

class Worker extends Actor {
  def factorial(n: Int): Int = if (n == 0) 1 else n * factorial(n - 1)
  def invfactorial(n: Int): Double = {
     val fac = factorial(n)
     if(fac == 0){
       println("trouble with n="+n)
       0.0
     }
     else
       1.0/fac
    }
  implicit def pimp(i: Int) = new { def ! = factorial(i) }
  
  def receive = {
    case w : Work =>  sender ! (w.begin to (w.end-1)).map(invfactorial).sum
  }
}