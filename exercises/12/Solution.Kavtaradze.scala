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
    	  low.to(high) zip low.to(high).map(fun)
      }
      
      val a = values(x => x * x, -5, 5)
      println(a.mkString(" "))

    }
  }

  new Task("Task 2") {
    def solution() = {

      def Max(x: Int, y: Int) = {
    	  if (x >= y) x
    	  else y
      }
  
      val a = Array(1, 3, 9, 6, 18, 15, 12)
      println(a.reduceLeft(Max))

    }
  }

  new Task("Task 3") {
    def solution() = {

      def factorial(x: Int) = {
    	  (x to 1 by -1).reduceLeft(_ * _)
      }
  
      println(factorial(1) + " " + factorial(2) + " " + factorial(3) + " " + factorial(4))

    }
  }

  new Task("Task 4") {
    def solution() = {

      def factorial(x: Int) = {
    	  (x to 1 by -1).foldLeft(1)(_ * _)
      }
  
      println(factorial(1) + " " + factorial(2) + " " + factorial(3) + " " + factorial(-4))
  
    }
  }

  new Task("Task 5") {
    def solution() = {

      def list(fun: (Int) => Int, inputs: Seq[Int]) = {
    	  inputs.map(fun)
      }
  
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
    	  inputs.map(fun).max
      }
  
      val a = largest(x => 10 * x - x * x, 1 to 10)
      val b = list(x => 10 * x - x * x, 1 to 10)
      println("List: " +b.mkString(" "))
      println("Max: " +a)

    }
  }


}
