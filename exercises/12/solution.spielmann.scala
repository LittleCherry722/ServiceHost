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
        (low to high) map (x => (x, fun(x)))
      }
      println("var v = values(x => x * x, -5, 5)")
      var v = values(x => x * x, -5, 5)
      println("v = " + v)
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("var a = Array(435,53,25234,24,53)")
      var a = Array(435,53,24,24,53)
      println("a reduceLeft((x,y) => if (x > y) x else y)")
      var max = a reduceLeft((x,y) => if (x > y) x else y)
      println("max value is: " + max)
    }
  }

  new Task("Task 3") {
    def solution() = {
      def factorial(x: Int) = {
        if (x < 1) {
          if (x == 0)
            1
          else
            throw new IllegalArgumentException("Factorial is not defined for numbers < 0")
        }
        else
            (1 to x) reduceLeft ((a, b) => a * b)
      }
      println("factorial(5) = " + factorial(5))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def factorial(x: Int) = {
        if (x < 0)
          throw new IllegalArgumentException("Factorial is not defined for numbers < 0")
        else
          (1 to x).foldLeft(1)((a, b) => a * b)
      }
      println("factorial(5) = " + factorial(5))
      println("factorial(0) = " + factorial(0))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
        (inputs map fun).max
      }
      println("largest(x => 10 * x - x * x, 1 to 10) should be 25 and is " + largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 6") {
    def solution() = {
    }
  }

  new Task("Task 7") {
    def solution() = {
    }
  }

  new Task("Task 8") {
    def solution() = {
    }
  }

  new Task("Task 9") {
    def solution() = {
    }
  }

  new Task("Task 10") {
    def solution() = {
      def unless(cond: Boolean)(ifFalse: => Any)(ifTrue: => Any) = {
        if (!cond) ifFalse
        else ifTrue
      }
      println("unless(1>1){ 1 }{ 2 } evaluates to: " + unless(1>1){ 1 }{ 2 })
    }
  }

}
