
package chapter12

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
      def values (fun: (Int) => Int, low: Int, high: Int) = {
        val range = low to high
        for {e <- range } yield (e, fun(e))
      }      
      println(values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val range = 1 to 10
      def returnBigger(x: Int, y: Int): Int = if (x > y) x else y
      println(range.mkString(" "))
      println(range.reverse.reduceLeft(returnBigger(_, _)))      
    }
  }
  new Task("Task 3") {
    def solution() = {
      def factorial(x: Int): Int = {
        if (x < 1) return x
        val range = 1 to x
        range.reduceLeft(_ * _)
      }
      println("Factorial(5) = " + factorial(5))
    }
  }
  new Task("Task 4") {
    def solution() = {
      def factorial(x: Int): Int = {
//        if (x < 1) return x
        val range = 1 to x
        range.foldLeft(1)(_ * _)
      }
      println("Factorial(5) = " + factorial(5))
    }
  }
  
  new Task("Task 5") {
    def solution() = {
     
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
        def returnBigger(x: Int, y: Int): Int = if (x > y) x else y
        (inputs.map(fun)).reduceLeft(returnBigger(_, _))
      }
      
      println(largest(x => 10*x - x*x, 1 to 10))
    }
  }
  new Task("Task 10") {
    def solution() = {
      
      def unless (condition: => Boolean)(block: => Unit) {
        if(!condition) block
      }
      for (i <- 1 to 5) {
        unless(i == 3){ 
          println("Unless i = 3 print i from 1 to 5: i = " + i)
        }
      }
      println("Would work without currying as well, but then you would have to write the execution block into the parantheses with the condition")
    }
  }
}
