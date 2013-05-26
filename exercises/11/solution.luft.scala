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
			val answer = "both operators (+ and ->) have same precedence, so both statements get evaluated left to right"
		}
	}

	new Task("Task 4") {
		def solution() = {
			class Money(val dollar: Int, val cent: Int) extends scala.math.Ordered[Money] {
				def compare(that: Money): Int = {
					(dollar * 100 + cent) - (that.dollar * 100 + cent)
				}
				def +(that: Money): Money = {
					new Money(this.dollar + that.dollar + (this.cent + that.cent) / 100, (this.cent + that.cent) % 100)
				}
				def ==(that: Money): Boolean = {
					this.compare(that) == 0;
				}
				override def toString(): String = {
					this.dollar + " dollar and " + this.cent + " cent"
				}
			}
			assert(new Money(1, 75) + new Money(0, 50) == new Money(2, 25))
			assert(new Money(1, 75) > new Money(0, 50));
			val answer = "* and / operators dont make sense, because money^2 is not a usable unit"
		}
	}

	new Task("Task 7") {
		def solution() = {
			// don't know what to do...
		}
	}
}