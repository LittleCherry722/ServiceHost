
package chapter3

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

  new Task("Task 2") {
    def solution() = {
      //found at stackoverflow: http://stackoverflow.com/questions/10158405/swapping-array-values-with-for-and-yield-scala
      def swap(a: Array[Int]) = {
        a.grouped(2)
          .flatMap {
            case Array(x, y) => Array(y, x)
            case Array(x) => Array(x)
          }.toArray
      }

      //Test
      var x = Array.range(1, 10)
      println("Before: " + x.mkString(", "))
      println("After: " + swap(x).mkString(", "))

    }
  }

  new Task("Task 3") {
    def solution() = {

      def swapreturn(a: Array[Int]): Array[Int] = {
        val x = ArrayBuffer[Int]()
        for (i <- 0 to (a.length - 1, 2)) yield if (i != (a.length - 1)) x += (a(i + 1), a(i)) else x += a(i)
        x.toArray
      }

      //Test
      var x = Array.range(1, 10)
      println("Before: " + x.mkString(", "))
      println("After: " + swapreturn(x).mkString(", "))
    }
  }
  new Task("Task 4") {
    def solution() = {
      def seperate(a: Array[Int]): Array[Int] = {
        val positives = ArrayBuffer[Int]()
        val negatives = ArrayBuffer[Int]()
        for (i <- 0 to a.length - 1) yield if (a(i) >= 0) positives += a(i) else negatives += a(i)
        (positives ++= negatives).toArray
      }

      //Test
      val x = Array(-2, 1, -3, 0, 2, 3, -4)
      println("Before: " + x.mkString(", "))
      println("After: " + seperate(x).mkString(", "))
    }
  }
  new Task("Task 7") {
    def solution() = {
      def rmvdublicates(a: Array[Int]): Array[Int] = {
        a.distinct
      }
      
      //Test
      val x = Array(-2, 1, -3, 0, 2, 3, -4, -4, 1, 7, 7, 0)
      println(rmvdublicates(x).mkString(" "))
    }
  }

  new Task("Task 8") {	
    def solution() = {
      def transform(a: Array[Int]): Array[Int] = {
        val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
        val b = a.toBuffer
    	indexes.toArray.drop(1).reverse.foreach(e => b.remove(e))
    	b.toArray
      }
      
      //Test
      val x = Array(-2, 1, -3, 0, 2, 3, -4)
      println("Before: " + x.mkString(", "))
      println("After: " + transform(x).mkString(", "))
    }	
  }
}
