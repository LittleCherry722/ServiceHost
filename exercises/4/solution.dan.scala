import scala.collection.JavaConverters._

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
      val gizmos = scala.collection.mutable.Map("item1" -> 10.0, "item2" -> 8.0, "item3" -> 9.0)
      println(gizmos)
      for ((id, price) <- gizmos)
        gizmos(id) = price * 0.9;
      println(gizmos)
    }
  }

  new Task("Task 6") {
    def solution() = {
    	val weekDays = scala.collection.mutable.LinkedHashMap[String, Int]();
    	weekDays += ("Monday" ->java.util.Calendar.MONDAY);
    	weekDays += ("Tuesday" ->java.util.Calendar.TUESDAY);
    	weekDays += ("Wednesday" ->java.util.Calendar.WEDNESDAY);
    	weekDays += ("Thursday" ->java.util.Calendar.THURSDAY);
    	weekDays += ("Friday" ->java.util.Calendar.FRIDAY);
    	weekDays += ("Saturday" ->java.util.Calendar.SATURDAY);
    	weekDays += ("Sunday" ->java.util.Calendar.SUNDAY);
    	
    	for((k,v) <- weekDays)
    	  println(k+" - "+v);
    }
  }

  new Task("Task 7") {
    def solution() = {
      val properties = System.getProperties().asScala
      val keys = properties.keySet;
      val maxLength = keys.maxBy(_.length());
      for ((k, v) <- properties) {
        val nSpace = (maxLength.length() - k.length());
        val space = if (nSpace > 0) String.format("%" + (maxLength.length() - k.length()) + "s", " ") else "";
        println(k + space + "| " + v);
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]) = {
        (values.min, values.max)
      }
      val arr = Array(1, 7, 2, 9)
      println(arr.mkString(" "))
      print("minmax:"+minmax(arr))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int) ={
    	  (values.filter(_ < v),
    	   values.filter(_ == v),
    	   values.filter(_ > v))
      }
      val arr = Array(1, 7, 4, 3, 5, 6, 4, 2, 9)
      println(arr.mkString(" "))
      val res = lteqgt(arr,4)
      print("lteqgt(arr,4): ["+ res._1.mkString(" ")+"; "+res._2.mkString(" ")+"; "+res._3.mkString(" ")+"]")
    }
  }
}
