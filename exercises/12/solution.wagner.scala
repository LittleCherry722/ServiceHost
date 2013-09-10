object Solution extends App {
  Tasks.execute()
}

abstract class Task(val name: String) {
  Tasks.add(this)

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = tasks :+= t
  def execute() = tasks foreach { _.execute() }
  def execute(name: String) =
    (tasks filter { _.name==name }).head.execute()
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
    	def values(fun: (Int) => Int, low: Int, high: Int) =
    	  for (i <- low to high) yield (i, fun(i))
    	println(s"values(x => x * x, -5, +5) = ${ values(x => x * x, -5, +5) }")
    }
  }

  new Task("Task 2") {
    def solution() = {
    	def maxEl(a: Array[Int]) = a reduceLeft { _ max _ }
    	println("maxEl(Array(1,6,3,11,2,4)) = " + maxEl(Array(1,6,3,11,2,4)))
    }
  }

  new Task("Task 3") {
    def solution() = {
    	def factorial(n: Int) = (1 to n) reduceLeft { _ * _ }
    	println("factorial(9) = " + factorial(9))
    }
  }

  new Task("Task 4") {
    def solution() = {
    	def factorial(n: Int) = (1 /: (1 to n)) { _ * _ }
    	println("factorial(9) = " + factorial(9))
    	println("factorial(0) = " + factorial(0))
    }
  }

  new Task("Task 5") {
    def solution() = {
    	def largest(fun: (Int) => Int, inputs: Seq[Int]) =
    	  inputs.map(fun).max
    	println(largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 10") {
    def solution() = {
    	def unless(cond: Boolean)(fn: => Unit) { if (! cond) fn }
    	unless(5>10) {
    	  println("nope")
    	}
    }
  }

}
