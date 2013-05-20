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
      val v = values(x => x * x, -5, 5)
      println(v.mkString(" "))

    }
    
    def values(fun: (Int) => Int, low: Int, high: Int) = {
      (low to high) zip (low to high).map( fun(_) )
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      println( (1 to 9).reduceLeft( (x: Int, y: Int) => if (x > y) x else y ) )
          
          

    } 
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      println(fact(4))

    }
    
    def fact(n: Int) = {
      (1 to n).reduceLeft(_*_)
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      println(fact(0))

    }
    
    def fact(n: Int) = {
      (1 to n).foldLeft(1)(_*_)
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      println(largest(x => 10*x - x*x, 1 to 10))

    }
    
    def largest(fun: (Int) => Int, inputs: Seq[Int] ) = {
      inputs.map(fun(_)).max
    }
  }
  
  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      var x = 3
      unless (x == 0) {
        println(x)
        x -= 1
      }

    }
    
    def unless(condition: => Boolean)(block: => Unit) {
      if (!condition) {
        block
        unless(condition)(block)
      }
    }
  }

}
