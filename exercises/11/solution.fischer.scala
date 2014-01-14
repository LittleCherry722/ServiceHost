
package chapter11

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length

object Solution extends App {

  // execute all tasks
  Tasks.execute()

  // execute only a single one
  //TaskManager.execute("Task 1")
}

abstract class Task(val name: String) {
  Tasks add this

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()

  def add(t: Task) = { tasks :+= t }

  def execute() = { tasks.foreach((t: Task) => { t.execute() }) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      println("(3 + 4) -> 5 and 3 -> (4 + 5)")
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Money(val dollars: Int = 0, val cents: Int = 0) {
        def +(other: Money): Money = {
          val d = this.dollars + other.dollars
          val c = this.cents + other.cents
          if (c > 99) new Money(d + 1, c - 100) else new Money(d, c)
        }

        def -(other: Money): Money = {
          val d = this.dollars - other.dollars
          val c = this.cents - other.cents
          if (c < 0) new Money(d - 1, c + 100) else new Money(d, c)
        }

        def ==(other: Money): Boolean = this.dollars == other.dollars && this.cents == other.cents

        def <(other: Money): Boolean = {
          (this.dollars < other.dollars) || (this.dollars == other.dollars && this.cents < other.cents)
        }

        override def toString: String = "Money(" + this.dollars + ", " + this.cents + ")"
      }

      val m1 = new Money(1, 75)
      val m2 = new Money(0, 50)
      val m3 = new Money(2, 25)
      println(m1.toString + " + " + m2.toString + " == " + m3.toString + " => " + (m1 + m2 == m3))
    }
  }
  new Task("Task 7") {
    def solution() = {

      class Bit(val on: Boolean = false) {
        val value = if (on) 1 else 0
        override def toString: String = String.valueOf(value)
      }
      class BitSequence(bits: Array[Bit]) {
        def update(k: Int, b: Bit) = bits(k) = b
        def apply(k: Int) = bits(k)
        override def toString: String = bits.mkString("")
      }

      val bits = new Array[Bit](64)
      for (i <- 0 until 64) bits(i) = new Bit(false)
      val seq = new BitSequence(bits)

      println("Before update: \t" + seq.toString)
      seq(63) = new Bit(true);
      println("After update: \t" + seq.toString)
      
      println("Get Bit at Position 63: " + seq(63))
    }
  }
}
