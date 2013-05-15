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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      val pairs = values(x => x * x, -5, 5)
      println(pairs)
    }

    def values(fun: (Int) => Int, low: Int, high: Int) = (low to high).map(x => (x, fun(x)))
  }

  new Task("Task 2") {
    def solution() = {
      val numbers = List(1, 6, 4, 9, 7, 2, 3)
      val max = numbers.reduceLeft((x, y) => if (x > y) x else y)
      println(max)
    }
  }

  new Task("Task 3") {
    def solution() = {
      (0 to 4).foreach(i => println(i + "! = " + factorial(i)))
    }

    def factorial(n: Int) = {
      require(n >= 0)

      if (n == 0)
        1
      else
        (1 to n).reduceLeft(_ * _)
    }
  }

  new Task("Task 4") {
    def solution() = {
      (0 to 4).foreach(i => println(i + "! = " + factorial(i)))
    }

    def factorial(n: Int) = {
      require(n >= 0)
      (1 to n).foldLeft(1)(_ * _)
    }
  }

  new Task("Task 5") {
    def solution() = {
      val inputs = List(1, 2, 3, 4, 5, -6, -7, -8)
      println(largest(Math.abs, inputs))
    }

    def largest(fun: (Int) => Int, inputs: Seq[Int]) = inputs.map(fun).max
  }

  new Task("Task 10") {
    def solution() = {
      unless(0 > 1) {
        println("in unless")
      }
    }

    def unless(condition: Boolean)(block: => Any): Any = if (!condition) block else ()
  }

}
