import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import java.util.Calendar

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
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
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }

  def printArray(arr: Array[Int]) {
    for (i <- 0 to arr.length - 1) {
      print(arr(i) + " ")
    }
    println()
  }
}

/* insert your solutions below */

object Tasks extends Tasks {
  new Task("Task 1") {
    //4.1
    def solution() = {

      val myGizmos = Map("Gizmo 1" -> 24.95, "Gizmo 2" -> 9.99, "Gizmo 3" -> 10.00)
      val discountGizmos = myGizmos.mapValues(cost => cost * 0.9)

      println(myGizmos.mkString(" - "))
      println(discountGizmos.mkString(" - "))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val weekdays = collection.mutable.LinkedHashMap(
        "Monday" -> Calendar.MONDAY,
        "Tuesday" -> Calendar.TUESDAY,
        "Wednesday" -> Calendar.WEDNESDAY,
        "Thursday" -> Calendar.THURSDAY,
        "Friday" -> Calendar.FRIDAY,
        "Saturday" -> Calendar.SATURDAY,
        "Sunday" -> Calendar.SUNDAY)

      println(weekdays.mkString(" - "))

    }
  }

  new Task("Task 7") {
    import collection.JavaConversions._

    def solution() = {

      val properties = System.getProperties()
      val keys = properties.keySet()
      val maxLength = keys.map(key => (key.asInstanceOf[String]).length).max

      for ((key, value) <- properties) {
        println(key + " " * (maxLength - key.length()) + " | " + value)
      }
      println()
    }
  }

  new Task("Task 8") {
    //4.8
    def solution() = {
      val array = Array(2, 6, 3, 69, -3, 5)
      print(minmax(array))
    }

    def minmax(values: Array[Int]): Tuple2[Int, Int] = {
      val min = values.min
      val max = values.max
      (min, max)
    }
  }

  new Task("Task 9") {
    //4.9
    def solution() = {
      val array = Array(3, 1, 5, -6, 0, 12, -15)
      print(lteqgt(array, 11))    		  
    }

    def lteqgt(values: Array[Int], v: Int): Tuple3[Int, Int, Int] = {
      val lt = values.filter(_ < v).size
      val eq = values.filter(_ == v).size
      val gt = values.filter(_ > v).size

      (lt, eq, gt)
    }
  }
}
