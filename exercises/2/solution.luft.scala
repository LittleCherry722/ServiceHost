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
			def signum(a: Int) = {
				if (a < 0) {
					-1
				} else {
					if (a == 0) {
						0
					} else {
						1
					}
				}
			}
			println(signum(42))
			println(signum(-4))
			println(signum(0))
		}
	}

	new Task("Task 2") {
		def solution() = {
			println("Value of {}: " + {})
			println("Type of {}: Unit")
		}
	}

	new Task("Task 4") {
		def solution() = {
			//			for (int i = 10; i >= 0; i--) System.out.println(i);
			for (i <- 10 to 0 by -1) println(i)
		}
	}

	new Task("Task 5") {
		def solution() = {
			def countdown(n: Int) {
				for (i <- n to 0 by -1) println(i)
			}
			countdown(3)
		}
	}

	new Task("Task 10") {
		def solution() = {
			def exp(x: Double, n: Int): Double = {
				if (n == 0) {
					1
				} else {
					if (n > 0) {
						if (n % 2 == 0) {
							exp(x, n / 2) * exp(x, n / 2)
						} else {
							x * exp(x, n - 1)
						}
					} else {
						1 / exp(x, -n)
					}
				}
			}
			println(exp(0.5, -7))
		}
	}
}
