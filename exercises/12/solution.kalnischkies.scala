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
      def values(fun: (Int) => Int, low: Int, high: Int) = low.to(high).map(x => (x, fun(x)))
      println(values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val s = Array(1, 5, 4, 2, 9, 3, 7)
      println(s.mkString("(", ",", ")") + " with max-element = " + s.reduceLeft(math.max))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def factorial(x: Int) = if (x == 0) 1 else 1.to(x).reduceLeft(_ * _)
      println("0! = " + factorial(0))
      println("5! = 5 x 4 x 3 x 2 x 1 = " + factorial(5))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def factorial(x: Int) = 1.to(x).foldLeft(1)( _ * _)
      println("0! = " + factorial(0))
      println("5! = 5 x 4 x 3 x 2 x 1 = " + factorial(5))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = inputs.map(fun).max
      println("Largest value: " + largest(x => 10 * x - x*x, 1 to 10))
    }
  }

  new Task("Task 10") {
    def solution() = {
      def unless(condition: Boolean)(block: => Unit) { if (!condition) { block } }
      unless(false) { println("We don't need call-by-name for condition as the evaluation doesn't change") }
      unless(!true) { println("We need currying though to have a 'nice' if-like syntax") }
    }
  }
}
