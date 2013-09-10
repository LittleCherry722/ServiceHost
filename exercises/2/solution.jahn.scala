object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
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
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      println(signum(3))
      println(signum(-5))
      println(signum(0))
    }

    def signum(i: Int) = {
      if (i > 0) 1
      else if (i < 0) -1
      else 0
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("the value of an empty block expression is () from type Unit")
    }
  }

  new Task("Task 4") {
    def solution() = {
      for (i <- (0 to 10).reverse) println(i)
    }
  }

  new Task("Task 5") {
    def solution() = {
      countdow(3)
    }

    def countdow(n: Int) {
      for (i <- (0 to n).reverse) println(i)
    }
  }

  new Task("Task 10") {
    def solution() = {
      println("3^4 = " + pow(3, 4))
      println("4^5 = " + pow(4, 5))
      println("42^0 = " + pow(42, 0))
      println("27^-3 = " + pow(27, -3))
    }

    def pow(x: Double, n: Int): Double = n match {
      case n if even(n) && n > 0 => {
        val y = pow(x, n / 2)
        y * y
      }
      case n if odd(n) && n > 0 => x * pow(x, n - 1)
      case n if n == 0 => 1
      case n if n < 0 => 1 / pow(x, -n)
    }

    def even(i: Int) = i % 2 == 0
    def odd(i: Int) = !even(i)
  }

}
