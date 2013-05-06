import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.LinkedHashMap
import java.util.Calendar

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      val gizmos = Map("product A" -> 9.90, "product B" -> 49.90, "product C" -> 100.00)
      val gizmos2 = gizmos.mapValues(price => price * 1.10)
      println(gizmos.mkString(","))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val days = new LinkedHashMap[String, Int]()
      days += "Monday" -> Calendar.MONDAY
      days += "Tuesday" -> Calendar.TUESDAY
      days += "Wednesday" -> Calendar.WEDNESDAY
      days += "Thursday" -> Calendar.THURSDAY
      days += "Friday" -> Calendar.FRIDAY
      days += "Saturday" -> Calendar.SATURDAY
      days += "Sunday" -> Calendar.SUNDAY

      println(days.mkString("\n"))
    }
  }

  new Task("Task 7") {
    import collection.JavaConversions._

    def solution() = {
      val properties = System.getProperties()
      val keyLength = for ((key, value) <- properties) yield key.length
      val maxKeyLength = keyLength.max

      for ((key, value) <- properties) {
        val padding = maxKeyLength - key.length
        println(key + " " * padding + " | " + value)
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
      println(minmax(a))
    }

    def minmax(values: Array[Int]) = (values.min, values.max)
  }

  new Task("Task 9") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
      println(lteqgt(a, 7))
    }

    def lteqgt(values: Array[Int], v: Int) = {
      val lt = values.filter(_ < v).size
      val eq = values.filter(_ == v).size
      val gt = values.filter(_ > v).size

      (lt, eq, gt)
    }
  }
}
