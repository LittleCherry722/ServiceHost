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
      def signum(x : Int) = {if(x>0) 1 else if (x == 0) 0 else -1}
            println(signum(19))
            println(signum(0))
            println(signum(-4))
      println("def signum(x : Int) = {if(x>0) 1 else if (x == 0) 0 else -1}")
    }
  }

  new Task("Task 2") {
    def solution() = {
          	val x = {}
          	println(x)
      println("{} has type Unit with value ()")
      // your solution for task 2 here
    }
  }

  new Task("Task 4") {
    def solution() = {
      var reverseList = for { a <- 0 to 10 } yield 10 - a
      for (i <- reverseList)
        println(i)
      // your solution for task 3 here

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 4 here
      def countdown(n: Int) = {
        for { i <- 0 to n }
          println(n - i);
      }
      countdown(4)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def signum(x: Int) = { if (x > 0) 1 else if (x == 0) 0 else -1 }
      // your solution for task 5 here
      def pow(x: Int, n: Int): Int = {
        signum(n) match {
          case 1 =>
            if (n % 2 == 0) {
              var tmp = pow(x, n / 2)
              tmp * tmp
            } else {
              x * pow(x, n - 1)
            }
          case 0 => 1
          case -1 => 1 / pow(x, -1 * n)
        }
      }
      println(pow(2, 5))
    }
  }

}
