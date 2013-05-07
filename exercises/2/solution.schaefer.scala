import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import java.util.Calendar
import java.util.BitSet

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
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
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }

  def printArray(arr: Array[Int]) {
    for (i <- 0 to arr.length - 1) {
      print(arr(i) + " ")
    }
    println()
  }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    //2.1
    def solution() = {
      println("signum  -3  0  42")
      print("------  " + signum(-3) + "  ")
      print(signum(0) + "  ")
      print(signum(42) + "  ------")

    }

    def signum(i: Int): Int = {
      if (i < 0) -1
      else if (i == 0) 0
      else 1
    }
  }

  new Task("Task 2") {
    //2.2
    def solution() = {
      println("empty() liefert " + empty() + ". Der Typ ist Unit.")
    }

    def empty() {}
  }

  new Task("Task 4") {
    //2.4
    def solution() = {
      for (i <- 10 to 0 by -1) {
        println(i)
      }
    }
  }

  new Task("Task 5") {
    //2.5
    def solution() = {
      println("countdown(7):")
      countdown(7)
    }
    def countdown(n: Int) {
      for (i <- n to 0 by -1) {
        print(i + " ")
      }
    }
  }

  new Task("Task 10") {
    //2.10
    def solution() = {
      println("2^5 = " + pow(2, 5))
      println("10^-2 = " + pow(10, -2))
      println("42^0 = " + pow(42, 0))
      println("5^4 = " + pow(5, 4))
    }

    def pow(x: Int, n: Int): Double = {
      if (n == 0) 1
      else if (n < 0) 1 / pow(x, -n)
      else if (n % 2 == 0) {
        val y = pow(x, n / 2)
        y * y
      } else x * pow(x, n - 1)
    }
  }
}
