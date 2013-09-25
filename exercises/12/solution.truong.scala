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
      def values(fun: Int => Int, low: Int, height: Int) = {
        for (i <- low to height) yield (i, fun(i))
      }

      println(values(x => x * x, -5, 5).mkString(", "))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def arrayMax(a: Array[Double]) = {
        a.reduceLeft(Math.max(_, _))
      }

      val a = Array(1.0, 2, -4, 5, 6, -5)
      println(arrayMax(a))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def factorial(x: Int) = {
        if (x < 1) 1 else {
          val r = 1 to x
          r.reduce(_ * _)
        }
      }

      println(factorial(4))
      println(factorial(1))
      println(factorial(0))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def factorial(x: Int) = {
        val r = 1 to x
        r.foldLeft(1)(_ * _)
      }
      println(factorial(4))
      println(factorial(1))
      println(factorial(0))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: Int => Int, inputs: Seq[Int]) = {
        inputs.map(x => fun(x)).reduceLeft(Math.max(_, _))
      }

      println(largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here

    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      def unless(condition: Boolean)(fun1: => Any)(fun2: => Any) = if (condition) fun2 else fun1

      val x = unless(1 == 1) { 10 } { 15 }
      println(x)

    }
  }

}
