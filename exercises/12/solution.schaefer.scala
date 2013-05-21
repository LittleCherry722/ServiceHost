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
      println(values(x => x * x, -5, 5))
    }

    def values(fun: (Int) => Int, low: Int, high: Int) = {
      (low to high).map(i => (i, fun(i)))
    }
  }

  new Task("Task 2") {
    def solution() = {
      println(Array(1, 7, 42, 12, 2) reduceLeft (_ max _))
    }
  }

  new Task("Task 3") {
    def solution() = {
      for (i <- 0 to 5) println(factorial(i))
    }

    def factorial(value: Int): Int = {
      require(value > -1)
      if (value == 0) 1
      else {
        (value to 1 by -1) reduceLeft (_ * _)
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      for (i <- 0 to 5) println(factorial(i))
    }

    def factorial(value: Int): Int = {
      require(value > -1)
      (value to 1 by -1).foldLeft(1)(_ * _)
    }
  }

  new Task("Task 5") {
    def solution() = {
      println(largest(x => 10 * x - x * x, 1 to 10))
    }

    def largest(fun: (Int) => Int, inputs: Seq[Int]): Int = {
      inputs.map(fun).max
    }
  }

  new Task("Task 10") {
    def solution() = {
      unless(2 > 1) { println("x") }
      unless(2 < 1) { println("this") }
    }

    def unless(cond: Boolean)(code: => Any) {
      if (!cond) code
    }
  }

}
