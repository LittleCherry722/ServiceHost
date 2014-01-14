
package chapter4

import scala.collection.mutable.LinkedHashMap
import scala.collection.JavaConversions.propertiesAsScalaMap
import java.util.Calendar._

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
      val map = Map("Item1" -> 10, "Item2" -> 5, "Item3" -> 7, "Item4" -> 20, "Item5" -> 15)
      val newmap = for ((k, v) <- map) yield (k, v * 0.9)
      print(map)
      print(newmap)
    }
  }

  new Task("Task 6") {
    def solution() = {
      val weekdays = LinkedHashMap("Monday" -> MONDAY, "Tuesday" -> TUESDAY, "Wednesday" -> WEDNESDAY, "Thursday" -> THURSDAY, "Friday" -> FRIDAY, "Saturday" -> SATURDAY, "Sunday" -> SUNDAY)
      weekdays.keySet.foreach(println)
    }
  }
  new Task("Task 7") {
    def solution() = {
      val probs: scala.collection.Map[String, String] = System.getProperties()
      val keys = Array("java.runtime.name", "sun.boot.library.path", "java.vm.version", "java.vm.vendor", "java.vendor.url", "path.seperator", "java.vm.name")
      val javaprobs =
        for (key <- keys)
          yield (key, probs.getOrElse(key, ":"))
      val max = keys.max.length
      for ((k, v) <- javaprobs)
        println(k + (" " * (max - k.length)) + "\t\uff5c " + v)
    }
  }
  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]): Tuple2[Int, Int] = {
        (values.min, values.max)
      }
      //TEST
      val a = Array(2, 3, 4, 5, 6, 7, 8, 1)
      println("Array: " + a.mkString(" "))
      println("Result: " + minmax(a))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int): Tuple3[Int, Int, Int] = {
        var (l, e, g) = (0, 0, 0)
        values.foreach(x => (if (x == v) e += x else if (x > v) g += x else l += x))
        (l, e, g)
      }
      //TEST
      val a = Array.range(1, 11)
      println("Array: " + a.mkString(" "))
      println("Result: " + lteqgt(a, 5))
    }
  }
}
