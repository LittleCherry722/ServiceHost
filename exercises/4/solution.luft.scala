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
	new Task("Task 1") {
		def solution() = {
			val prices = Map("a" -> 10, "b" -> 20)
			val discount = for ((k, v) <- prices) yield (k, v * .9)
			println(discount)
		}
	}

	new Task("Task 6") {
		def solution() = {
			val week = scala.collection.mutable.LinkedHashMap(
				"Monday" -> java.util.Calendar.MONDAY,
				"Tuesday" -> java.util.Calendar.TUESDAY,
				"Wednesday" -> java.util.Calendar.WEDNESDAY,
				"Thursday" -> java.util.Calendar.THURSDAY,
				"Friday" -> java.util.Calendar.FRIDAY,
				"Saturday" -> java.util.Calendar.SATURDAY,
				"Sunday" -> java.util.Calendar.SUNDAY
			)
			for ((k, v) <- week) {
				println(k + " [" + v + "]")
			}
		}
	}

	new Task("Task 7") {
		def solution() = {
			import collection.JavaConversions._
			val ident = System.getProperties().map(_._1.length()).max
			for ((k, v) <- System.getProperties()) {
				println(k + " " * (ident - k.length) + " = " + v)
			}
		}
	}

	new Task("Task 8") {
		def solution() = {
			def minmax(values: Array[Int]) = {
				(values.min, values.max)
			}
			println(minmax(Array(1, -2, 3, -4, 5)))
		}
	}

	new Task("Task 9") {
		def solution() = {
			def lteqgt(values: Array[Int], v: Int) = {
				(
					values.filter(_ < v).size,
					values.filter(_ == v).size,
					values.filter(_ > v).size
				)
			}
			println(lteqgt(Array(1, -2, 3, -4, 5), 3))
		}
	}
}