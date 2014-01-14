import scala.collection.JavaConverters._

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
      def values(fun: (Int) => Int, low: Int, high: Int) = (for (n <- low to high) yield (n, fun(n)))
      println(values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def getMinimum(input: Array[Int]) = input.reduceLeft(_ min _)
      println(getMinimum(Array(20, 12, 6, 15, 2, 9)))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def fac(n: Int) = if (n == 0) 1 else (1 to n).reduceLeft(_ * _)
      println(fac(0))
      println(fac(2))
      println(fac(11))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def fac(n: Int) = (1 to n).foldLeft(1)(_ * _)
      println(fac(0))
      println(fac(2))
      println(fac(11))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int)=>Int, inputs: Seq[Int]) = (for (i <- inputs) yield fun(i)).reduceLeft(_ max _)
      println(largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 6") {
    def solution() = {
      def largestAt(fun: (Int)=>Int, inputs: Seq[Int]) = inputs.reduceLeft((a,b) => if (fun(a) > fun(b)) a else b)
      println(largestAt(x => 10 * x - x * x, 1 to 10))
    }
  }
  
  new Task("Task 10") {
    def solution() = {
      def unless(guard: Boolean)(action : => Unit) {
        if (!guard) action
      }
      unless (false) {
        println("works")
      }
    }
  }

}
