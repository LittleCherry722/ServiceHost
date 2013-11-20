import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap

object Solution extends App {
	for(a<-args.reverse){
	  print(a)
	  print(" ")
	}
	println()

  // execute all tasks
  Tasks.execute()

}

abstract class UnitConversion() {
  def convert(in: Double): Double
}

object InchesToCentimeters extends UnitConversion {
  override def convert(in: Double): Double = {
    in / 2.5
  }
}
object GallonsToLiters extends UnitConversion {
  override def convert(in: Double): Double = {
    in / 4
  }
}

object MilesToKilometers extends UnitConversion {
  override def convert(in: Double): Double = {
    in / 1.5
  }
}

class Point(var x: Double, var y: Double) {

}
object Point {
  def apply(x: Double, y: Double) =
    new Point(x, y)
}


object Cards extends Enumeration {
	val Spade, Diamond, Club, Heart = Value
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
      println(InchesToCentimeters.convert(100));
      println(GallonsToLiters.convert(100));
      println(MilesToKilometers.convert(100));
    }
  }

  new Task("Task 4") {
    def solution() = {
      val p = Point(1, 2)

    }
  }


  new Task("Task 8") {
    def solution() = {
      println(Cards.Spade)
      println(Cards.Club)
    }
  }

}
