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

  new Task("Task 2") {
    def solution() = {
      class UnitConversion {
        val k = 0.0;
        def convert(value: Double) = { k * value }
      }
      class InchesToCentimeters extends UnitConversion {
        override val k = 2.54
      }
      class GallonsToLiters extends UnitConversion {
        override val k = 3.78
      }
      class MilesToKilometers extends UnitConversion {
        override val k = 1.6
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(val x: Double, val y: Double) {
        override def toString() = { "[" + x + ";" + y + "]" }
      }
      object Point {
        def apply(x: Double, y: Double) {
          new Point(x, y);
        }
      }
      val point34 = Point(3, 4)
      println(point34)
    }
  }

  new Task("Task 5") {
    def solution() = {
      object Reverse extends App {
        if (args.length > 0) {
          val rargs = args.reverse
          println(rargs.mkString(" "))
        }
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      object Card extends Enumeration {
        val Spade = Value("\u2660") ;
        val Heart = Value("\u2665") ;
        val Diamond = Value("\u2666");
        val Club = Value("\u2663");
      }
      for(c <- Card.values)
    	  println(c);
    }
  }

}
