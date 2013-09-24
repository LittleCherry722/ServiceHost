object Solution extends App {
	Tasks.execute();
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
	def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

object Tasks extends Tasks {
	new Task("Task 2") {
		def solution() = {
			abstract class UnitConversion {
				def convert(a: Double): Double
			}
			object InchesToCentimeters extends UnitConversion {
				def convert(inches: Double) = inches / 2.54
			}
			object GallonsToLiters extends UnitConversion {
				def convert(gallons: Double) = gallons / 3.78541178
			}
			object MilesToKilometers extends UnitConversion {
				def convert(miles: Double) = miles / 1.609344
			}
		}
	}

	new Task("Task 4") {
		def solution() = {
			class Point(x: Double, y: Double)
			object Point {
				def apply(x: Double, y: Double) = new Point(x, y)
			}
		}
	}

	new Task("Task 5") {
		def solution() = {
			object argsInReverse extends App {
				args.reverse.map(println(_))
			}
		}
	}

	new Task("Task 6") {
		def solution() = {
			object suit extends Enumeration {
				val hearts = Value("\u2665")
				val diamonds = Value("\u2666")
				val clubs = Value("\u2663")
				val spades = Value("\u2660")
			}
			import suit._
			println("" + hearts + diamonds + clubs + spades)
		}
	}
}