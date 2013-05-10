
import scala.collection.JavaConversions.propertiesAsScalaMap
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

      // your solution for task 1 here
      ch4_1()

    }

    def ch4_1() {
      val map = scala.collection.mutable.Map("Apple" -> 100, "Banana" -> 120, "Orange" -> 110)
      val newmap = for ((k, v) <- map) yield (k, v * 0.9)
      for ((k, v) <- newmap) {
        println(k + " -- " + v)
      }
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      val map = scala.collection.mutable.LinkedHashMap("Monday" -> java.util.Calendar.MONDAY,
        "Tuesday" -> java.util.Calendar.TUESDAY,
        "Wednesday" -> java.util.Calendar.WEDNESDAY,
        "Thursday" -> java.util.Calendar.THURSDAY,
        "Friday" -> java.util.Calendar.FRIDAY,
        "Saturday" -> java.util.Calendar.SATURDAY,
        "Sunday" -> java.util.Calendar.SUNDAY)

      for ((k, v) <- map) {
        println(k + " -- " + v)
      }

    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      val props: scala.collection.Map[String, String] = System.getProperties()

      var max = 0

      for (elem <- props.keySet) {
        if (elem.length >= max)
          max = elem.length
      }

      for ((k, v) <- props) {
        println(k + " " + " " * (max - k.length) + " | " + v);
      }

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
      println(ch4_8(Array(1,2,3,4)))

    }
    
    def ch4_8(x: Array[Int]) = {
    (x.min, x.max)
  }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here
      println(lteqgt(Array(1,2,3,4,5,5,5,6,7,8,9,10),5))

    }
    
    def lteqgt(values: Array[Int], v: Int) = {
    var less = 0
    var equal = 0
    var larger = 0
    for ( n <- values ){
      if ( n < v )
        less += 1
      if ( n == v )
        equal += 1
      if ( n > v )
        larger += 1
    }
    (less, equal, larger)
  }
  }

}