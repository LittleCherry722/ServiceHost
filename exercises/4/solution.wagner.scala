import java.util.LinkedHashMap
import collection.JavaConversions._
import java.util.Collection

object Solution extends App {
  Tasks.execute()
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
      val p = Map("cessna" -> 750000, "iMac" -> 1900, "tesla coil" -> 9294094)
      val p2 = for ((k, v) <- p) yield (k, v * .9)
      println(p2.mkString(", "))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lm = collection.mutable.LinkedHashMap(
        "Monday" -> java.util.Calendar.MONDAY,
        "Tuesday" -> java.util.Calendar.TUESDAY,
        "Wednesday" -> java.util.Calendar.WEDNESDAY)
      println(lm.mkString(", "))

    }
  }

  new Task("Task 7") {
    def solution() = {
      val m = asScalaMap(System.getProperties())
      val longestKey = m.keySet.reduceLeft(
        (a, b) => if (a.length > b.length) a else b)
      for ((k, v) <- m) println(
        k + " " * (longestKey.length - k.length) + " | " + v)
    }
  }

  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]) = (values.min, values.max)
      println("minmax: " + minmax(Array(1, 3, 5, 6, 3, 2, 66, 7, 6, 4)))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int) = (
        values.filter(_ < v).length,
        values.filter(_ == v).length,
        values.filter(_ > v).length)
      println("lteqgt(...,7): " + lteqgt(Array(5, 3, 2, 2, 6, 66, 11, 4), 7))
    }
  }

}
