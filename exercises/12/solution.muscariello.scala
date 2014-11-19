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
      def values(fun: (Int) => Int, low: Int, high: Int) = {
        for (i <- low to high) yield {
          (i,  fun(i))
        }
      }
      values(x => x * x, -5, 5)
    }
  }

  new Task("Task 2") {
    def solution() = {
      Array(1,2,3,4,100,5,6).reduceLeft(scala.math.max(_,  _))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def fac(n: Int): Long = {
        (1 to n).reduceLeft(_ * _)
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      def fac2(n: Int): Long = {
        (1 to n).foldLeft(1)(_ * _)
      }
      assert(fac2(3) == 6)
      assert(fac2(0) == 1)
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]): Int = { // inputs.reduceLeft((x: Int, y: Int) => scala.math.max(fun(x),fun(y)))
        inputs.map(fun(_)).max
      }
      assert(largest(x => 10 * x - x * x, 1 to 10) == 25)
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
      def unless(condition: => Boolean)(block: => Unit) {
        if (!condition) {
          block
        }
      }
      unless (1 == 2) { println("ok") }
      println("it's no problem in this case if the condition evaluates to true or false, hence, call-by-name is not necessary.\n")
      println("currying is not needed either")
    }
  }

}
