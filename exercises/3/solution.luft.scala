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
			def loop(a: Array[Int]): Array[Int] = {
				for (i <- 0 until a.length - 1 by 2) {
					val tmp = a(i)
					a(i) = a(i + 1)
					a(i + 1) = tmp
				}
				a
			}
			println(loop(Array(1, 2, 3, 4, 5, 6)).mkString(", "))
		}
	}

	new Task("Task 3") {
		def solution() = {
			def loop(a: Array[Int]): Array[Int] = {
				(for (i <- 0 until a.length) yield {
					var index = if (i % 2 == 0) i + 1 else i - 1
					if (index >= a.length) index = a.length - 1
					a(index)
				}).toArray
			}
			println(loop(Array(1, 2, 3, 4, 5)).mkString(", "))
		}
	}

	new Task("Task 4") {
		def solution() = {
			def func(a: Array[Int]): Array[Int] = {
				a.filter(_ > 0) ++ a.filter(_ <= 0)
			}
			println(func(Array(5, -1, 4, -88, 11, 23)).mkString(", "))
		}
	}

	new Task("Task 7") {
		def solution() = {
			def func(a: Array[Int]): Array[Int] = {
				a.distinct
			}
			println(func(Array(1, 2, 3, 4, 2, 3, 4, 1, 2, 2, 2)).mkString(", "))
		}
	}

	new Task("Task 8") {
		def solution() = {
			def func(a: Array[Int]): Array[Int] = {
				val b = a.toBuffer
				val indexes = for (i <- 0 until b.length if b(i) < 0) yield i
				for (i <- indexes.drop(1).reverse) b.remove(i)
				b.toArray
			}
			println(func(Array(1, 2, -1, -2, 3, -4, 5)).mkString(", "))
		}
	}
}