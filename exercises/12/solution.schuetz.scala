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

      // your solution for task 1 here
      def values(fun: (Int) => Int, low: Int, high: Int) = {
        
        for (v <-(low to high))  yield (v, fun(v))
      }

      println (values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val a = Array(1,5,2,3,4)
      println(a.reduceLeft(_ max _))

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      def factorial(value: Int) = {
        if (value < 0)
          null
        else if (value == 0)
          1
        else
            (1 to value).reduceLeft(_ * _)
      }
      println(factorial(-2))
      println(factorial(0))
      println(factorial(6))

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      def factorial(value: Int) = {
        (1 to value).foldLeft(1)(_ * _)
      }
      
      println(factorial(-2))
      println(factorial(0))
      println(factorial(6))

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
        var large = 0
        for (i <- inputs) (if (fun(i) > large) large = fun(i))
        large
      }

      println(largest(x => 10 * x -x * x, 1 to 10))
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

      // your solution for task 10 here

    }
  }

}
