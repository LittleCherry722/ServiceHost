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
    def values(fun: (Int) => Int, low: Int, high: Int) = {
      for (i <- low to high) yield (i, fun(i))
    }

    def solution() = {
      val res = values(x => x * x, -5, 5)
      println("res: "+res);
    }
  }

  new Task("Task 2") {
    def solution() = {

      println("with a.reduceLeft(_ max _)")

    }
  }

  new Task("Task 3") {
    def factorial(n: Int) = {
      if (n == 0) 1 else (1 to n).reduceLeft(_ * _)
    }
    def solution() = {
      println("factorial(0): "+factorial(0))
      println("factorial(1): "+factorial(1))
      println("factorial(3): "+factorial(3))
      println("factorial(5): "+factorial(5))
    }
  }

  new Task("Task 4") {
    def factorial(n: Int) = {
      (1 to n).foldLeft(1)(_ * _)
    }
    def solution() = {
      println("factorial(0): "+factorial(0))
      println("factorial(1): "+factorial(1))
      println("factorial(3): "+factorial(3))
      println("factorial(5): "+factorial(5))
    }
  }

  new Task("Task 5") {
    def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
      inputs.reduceLeft(_ max fun(_))
    }
    def solution() = {
      println("largest(10x-x^2, 1 to 10): "+largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 10") {
    def unless(cond: Boolean)(fun: => Any) = {
      if (!cond) fun
    }
    def solution() = {
      unless (false) {
        println("unless false");
      }
      
      unless (true) {
        println("unless true");
      }

      val a1 = if (true) { 5 }
      println("if_true: "+a1)
      val a2 = if (false) { 5 }
      println("if_false: "+a2)

      val b1 = unless (true) { 5 }
      println("unless_true: "+b1)
      val b2 = unless (false) { 5 }
      println("unless_false: "+b2)
    }
  }

}
