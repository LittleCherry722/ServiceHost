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

      val gizmos = Map("item1" -> 5.0, "item2" -> 7.5, "item3" -> 100.0)
      val reduced = for((item, price) <- gizmos) yield (item, price*0.9)
      
      println("gizmos: " + gizmos.mkString(", "));
      println("reduced: " + reduced.mkString(", "));

    }
  }

  new Task("Task 6") {
    def solution() = {
      import java.util.Calendar._
      import scala.collection.mutable.LinkedHashMap
      
      var weekdays = LinkedHashMap[String, Int]()
      weekdays += ("Monday" -> MONDAY)
      weekdays += ("Tuesday" -> TUESDAY)
      weekdays += ("Wednesday" -> WEDNESDAY)
      weekdays += ("Thursday" -> THURSDAY)
      weekdays += ("Friday" -> FRIDAY)
      weekdays += ("Saturday" -> SATURDAY)
      weekdays += ("Sunday" -> SUNDAY)
      
      
      
      
      

      for((day, weekday) <- weekdays) println(day + " -> " + weekday)
    }
  }

  new Task("Task 7") {
    def solution() = {
      import scala.collection.JavaConversions.propertiesAsScalaMap
      val props: scala.collection.Map[String, String] = System.getProperties()
      
      val maxKeyLength: Int = (for((key, value) <- props) yield key.length).max
      
      for((key, value) <- props){
        println(key + (" " * (maxKeyLength - key.length)) + " | " + value)
      }
    }
  }

  new Task("Task 8") {
    def minmax(values: Array[Int]): (Int, Int) = {
      (values.min, values.max)
    }
    
    def solution() = {
      val arr1: Array[Int] = Array(1, 2, 3, 4, 5);
      val arr2: Array[Int] = Array(-1, -2, -3, -4, -5);
      val arr3: Array[Int] = Array(1, -2, 3, -4, 5);

      println("minmax " + arr1.mkString(", ") + ": " + minmax(arr1));
      println("minmax " + arr2.mkString(", ") + ": " + minmax(arr2));
      println("minmax " + arr3.mkString(", ") + ": " + minmax(arr3));
    }
  }

  new Task("Task 9") {
    def lteqgt(values: Array[Int], v: Int): (Int, Int, Int) = {
      val lt: Int = values.count(_ < v)
      val eq: Int = values.count(_ == v)
      val gt: Int = values.count(_ > v)
      
      (lt, eq, gt)
    }
    
    def solution() = {
      val arr1: Array[Int] = Array(1, 2, 3, 4, 5, 1);
      val arr2: Array[Int] = Array(-1, -2, -3, -4, -5);
      val arr3: Array[Int] = Array(1, -2, 3, -4, 5);

      println("lteqgt ((" + arr1.mkString(", ") + "), 1): " + lteqgt(arr1, 1));
      println("lteqgt ((" + arr2.mkString(", ") + "), 1): " + lteqgt(arr2, 1));
      println("lteqgt ((" + arr3.mkString(", ") + "), 1): " + lteqgt(arr3, 1));
    }
  }

}
