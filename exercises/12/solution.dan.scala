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
      def values(fun: (Int) => Int, low: Int, high: Int) = {
        for (i <- low to high)
          yield (i, fun(i))
      }
      println(values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def largest(array: Array[Int]) = array.reduceLeft(_ max _)
      assert(largest(Array(1, 10, -4, 15, 9)) == 15)
    }
  }

  new Task("Task 3") {
    def solution() = {
      def factorial(i: Int) = (1 to i).reduceLeft(_ * _)
      assert(factorial(4) == 24)
    }
  }

  new Task("Task 4") {
    def solution() = {
      def factorial(i: Int) = (1 to i).foldLeft(1)(_ * _)
      assert(factorial(0) == 1)
      assert(factorial(4) == 24)
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
        inputs.map(fun).reduceLeft(_ max _)
      }
      assert(largest(x => 10 * x - x * x, 1 to 10) == 25)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def unless(condition: => Boolean)(block: => Unit) {
        if (!condition)
          block
      }
      System.out.println("first parameter no need to be call-by-name, because the condition can be evaluated before the function call")
      System.out.println("It's also not needed to use currying")

      var x = 1
      unless(x == 2)(x += 3)
      assert(x==4)
      unless(x == 4)(x -= 3)
      assert(x==4)      
    }
  }

}
