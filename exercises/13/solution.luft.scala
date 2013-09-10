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
			import scala.collection.mutable._
			def indexes(str: String): Map[Char, Set[Int]] = {
				val result = Map[Char, Set[Int]]()
				for (i <- 0 until str.length()) {
					if (result.contains(str(i))) {
						result(str(i)) += i
					} else {
						result(str(i)) = Set[Int](i)
					}
				}
				result
			}
			println(indexes("Mississippi"))
		}
	}

	new Task("Task 2") {
		def solution() = {
			//dont know
			//println(indexes("Mississippi"))
		}
	}

	new Task("Task 3") {
		def solution() = {
			import scala.collection.mutable.LinkedList
			def removeZeros(list: LinkedList[Int]): LinkedList[Int] = {
				list.filter(_ != 0)
			}
			println(removeZeros(LinkedList(1, 2, 0, 4, 5)))
		}
	}

	new Task("Task 4") {
		def solution() = {
			def doit(a: Array[String], b: Map[String, Int]) = {
				a.map(b.get(_)).flatMap(a=>a)
			}
			println(doit(Array("Tom", "Fred", "Harry"), Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)).mkString(", "))

		}
	}
}