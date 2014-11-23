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
      val gizmos = Map(
        "kerosene-powered cheese grater" -> 39.99,
        "Spheroboom" -> 999.90,
        "Fing-Longer" -> 29.99
      )
      val reduced =
        for ((k, v) <- gizmos) yield (k, .9 * v)
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      import java.util.Calendar
      val days = scala.collection.mutable.LinkedHashMap(
        "Monday" -> Calendar.MONDAY,
        "Tuesday" -> Calendar.TUESDAY,
        "Wednesday" -> Calendar.WEDNESDAY,
        "Thursday" -> Calendar.THURSDAY,
        "Friday" -> Calendar.FRIDAY,
        "Saturday" -> Calendar.SATURDAY,
        "Sunday" -> Calendar.SUNDAY
      )
      println("the weekdays are:")
      for ((k, _) <- days) println(k)
    }
  }

  new Task("Task 7") {
    def solution() = {
      import scala.collection.JavaConverters._
      val props = System.getProperties().asScala
      val max_ = props.keys.map(_.length).max
      for ((k, v) <- props) {
        val pad = max_ - k.length + 10
        println(k + " "*pad + "| " + v)
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]): (Int, Int) = {
        (values.min, values.max)
      }
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int): (Int, Int, Int) = {
        val (le, gt) = values.partition(_ <= v)
        val (eq, lt) = le.partition(_ == v)
        (lt.length, eq.length, gt.length)
      }
    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
