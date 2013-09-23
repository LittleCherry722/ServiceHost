import scala.util.logging.Logged

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
			def values(fun: (Int) => Int, low: Int, high: Int) = {
				(low to high).map(x => (x, fun(x)))
			}
			println(values(x => x * x, -5, 5))
		}
	}

	new Task("Task 2") {
		def solution() = {
			println(Vector(1, 4, 6, 2, 4, 6, 4, 5, 8, 3, 1).reduceLeft(scala.math.max(_, _)))
		}
	}

	new Task("Task 3") {
		def solution() = {
			def factorial(a: Int) =  {
				(1 to a).reduceLeft(_*_)
			}
			println(factorial(5))
		}
	}

	new Task("Task 4") {
		def solution() = {
			def factorial(a: Int) =  {
				(1 to a).foldLeft(1)(_*_)
			}
			println(factorial(0))
		}
	}

	new Task("Task 5") {
		def solution() = {
			def largest(fun: (Int) => Int, inputs: Seq[Int]): Int = {
				inputs.map(fun).max
			}
			println(largest(x => 10 * x - x * x, 1 to 10))
		}
	}

	new Task("Task 10") {
		def solution() = {
			def unless(cond: Boolean)(block: => Unit) {
				if (!cond) block
			}
			unless(false) {
				println("Hello World!")
			}
		}
	}
}