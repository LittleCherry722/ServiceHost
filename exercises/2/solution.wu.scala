



import scala.math._

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
      println(signum(2))
      println(signum(-5))
      println(signum(0))
    }

    def signum(input: Double) = {
      if (input > 0) 1
      else if (input == 0) 0
      else -1
    }

  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      println("value of {} is (), type is Unit")

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      var i: Int = 10
      while (i >= 0) {
        println(i)
        i -= 1
      }

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      countdown(7)

    }

    def countdown(n: Int) {
      var i: Int = n
      while (i >= 0) {
        println(i)
        i -= 1
      }
    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      println(ch2_10(2.0, -2))
      println(ch2_10(3.0, 3))
      println(ch2_10(6.0, 0))

    }

    def ch2_10(x: Double, n: Int): Double = {
      if (n > 0) {
        if (n % 2 == 0)
          pow(ch2_10(x, n / 2), 2)
        else x * ch2_10(x, n - 1)
      } else {
        if (n == 0)
          1
        else 1 / ch2_10(x, -n)
      }

    }
  }

}
