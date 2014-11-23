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

      // your solution for task 1 here
      val gizmos = Map("Mercedes S-Class Coupe" -> 125000.00, "Bugatti Shoes" -> 249.95, "Cube Attention 26" -> 700.00)
      val gizmos_disount = for((k, v) <- gizmos) yield (k, v*0.9)

    }
  }

 

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      var week = scala.collection.mutable.LinkedHashMap[String, Int]()
      week += ("Monday" -> java.util.Calendar.MONDAY)
      week += ("Tuesday" -> java.util.CalendarTUESDAY)
      week += ("Wednesday" -> java.util.CalendarWEDNESDAY)
      week += ("Thursday" -> java.util.CalendarTHURSDAY)
      week += ("Friday" -> java.util.CalendarFRIDAY)
      week += ("Saturday" -> java.util.CalendarSATURDAY)
      week += ("Sunday" -> java.util.CalendarSUNDAY)

      for((day, javaDay) <- week){
        println(day + " -> " + javaDay)
      }
      
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
	 def minmax(values: Array[Int]): (Int, Int) = {
	   (values.min, values.max)
	 }
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here
      def lteqgt(values: Array[Int], v: Int) = {
        (values.count(_ < v), values.count(_ == v), values.count(_ > v))
	  }
    }
  }



}
