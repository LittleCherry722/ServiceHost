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
  /* The preceding problem wasn’t very object-oriented. Provide a general super-
  class UnitConversion and define objects InchesToCentimeters, GallonsToLiters, and
  MilesToKilometers that extend it. */
	abstract class UnitConversion() {
		def convert(value: Double): Double
	}

	object InchesToCentimeters extends UnitConversion() {
		override def convert(value: Double) = 2.54 * value
	}

	object GallonsToLiters extends UnitConversion() {
		override def convert(value: Double) = 3.78541178 * value
	}

	object MilesToKilometers extends UnitConversion() {
		override def convert(value: Double) = 1.609344 * value
	}
	
		println(InchesToCentimeters.convert(1))
		println(GallonsToLiters.convert(1))
		println(MilesToKilometers.convert(1))
    }
  }

  new Task("Task 2") {
    def solution() = {
  /* Define a Point class with a companion object so that you can construct Point
  instances as Point(3, 4), without using new. */
	class Point(val x: Int, val y: Int) { }

	object Point {
		def apply(x: Int, y: Int) = new Point(x, y)
	}
	
	val n = Point(7, 3)
	println("n.x = " + n.x + " n.y = " + n.y)
    }
  }

  new Task("Task 3") {
    def solution() = {
  /* Write a Scala application, using the App trait, that prints the command-line
  arguments in reverse order, separated by spaces. For example, scala Reverse
  Hello World should print World Hello. */
	object Reverse extends App {
	if (args.length > 0)
		println(args.reverse.mkString(" "))
	else
		println("")
	}
	
    }
  }

  new Task("Task 4") {
    def solution() = {
  /* Write an enumeration describing the four playing card suits so that the toString
  method returns ♣, ♦, ♥, or ♠. */
	object Card extends Enumeration {
		val clubs = Value("\u2618")
		val diamonds = Value("\u2666")
		val hearts = Value("\u2665")
		val spades = Value("\u2660")
	}
	
	println(Card.clubs)
	println(Card.diamonds)
	println(Card.hearts)
	println(Card.spades)
    }
  }

}
