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

     val prices = Map("Head" -> 100, "Barton" -> 80, "Salomon" -> 60)
     val discount = for ((k, v) <- prices) yield (k, v * 0.9)
  
     println(prices)
     println(discount)

    }
  }

  

  new Task("Task 6") {
    def solution() = {

      import scala.collection.mutable.LinkedHashMap
      val daysOfTheWeek = LinkedHashMap(
    	  "Monday" -> java.util.Calendar.MONDAY,
    	  "Tuesday" -> java.util.Calendar.TUESDAY,
    	  "Wednesday" -> java.util.Calendar.WEDNESDAY,
    	  "Thursday" -> java.util.Calendar.THURSDAY,
    	  "Friday" -> java.util.Calendar.FRIDAY,
    	  "Saturday" -> java.util.Calendar.SATURDAY,
    	  "Sunday" -> java.util.Calendar.SUNDAY
      )
    
      for((k, v) <- daysOfTheWeek) println(k +" "+ v)

    }
  }

  new Task("Task 7") {
    def solution() = {

      import scala.collection.JavaConversions.propertiesAsScalaMap

      val props: scala.collection.Map[String, String] = System.getProperties()

      for ((k, v) <- props) println(k +" | "+ v)

    }
  }

  new Task("Task 8") {
    def solution() = {

      def minmax(values: Array[Int]): Tuple2[Int, Int] = {
    	 (values.min, values.max)
      }

      val a = Array(1, 5, 10, 5, 6, -2)

      println(minmax(a))

    }
  }

  new Task("Task 9") {
    def solution() = {

      def lteqgt(values: Array[Int], v: Int): Tuple3[Int, Int, Int] = {
	      (values.count(_ < v), values.count(_ == v), values.count(_ > v))
      }

      val a = Array(-1, 0, 1, 3, 4, 4, 5, 6, 7)

      println(lteqgt(a, 4))

    }
  }

  

}
