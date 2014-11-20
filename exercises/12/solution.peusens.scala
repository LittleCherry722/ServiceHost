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
        for (j <- low to high) yield (j, fun(j))
      }

      val result = values(x => x * x, -5, 5)
      println(res);
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      println("call the reduceLeft(arg) with the argument _ max _")
      println("it will step by step select the max value from the last two elements and removes the smaler one (iterative)")
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      def factorial(x: Int) = {
        if (x != 0){
          (1 to x).reduceLeft(_ * _)
        }
        else {
          0
        }
      }
      
      val result = factorial(5)
      println(result)
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      def factorial(x: Int) = {
        (1 to x).foldLeft(1)(_ * _)
      }
      
      val result = factorial(5)
      println(result)

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
        inputs.reduceLeft(_ max fun(_))
      }  
      
      var result = largest(x => 10 * x - x, 1 to 10)
      println(result)
      
    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      def unless(ifNot: Boolean)(doFunction: => Any) = {
        if (!ifNot) doFunction
      }

      println("dont need currying")
      
      unless(1 > 0){
        print("!(1 > 0)")
      }
    }
  }

}
