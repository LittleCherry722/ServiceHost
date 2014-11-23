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
      def signum(x: Int): Int = if (x > 0) 1 else if (x == 0) 0 else -1
    }
  }

  new Task("Task 2") {
    def solution() = {
      "The value is (), the type is Unit"

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      for (i <- 10 to 0 by -1)
        println(i)
    }
  }

  new Task("Task 5") {
    def solution() = {
      def countdown(n: Int): Unit = {
        for (i <- n to 0 by -1)
          println(i)
      }

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
      def myPow(x: Int, n: Int): Int = {
        def even(x: Int): Boolean = x % 2 == 0
        def odd(x: Int): Boolean = !even(x)
        n match {
          case 0 => 1;
          case nn if (even(nn) && nn >= 0) => {
            val r: Int = myPow(x, nn/2)
            r*r
          }
          case nn if (odd(nn) && nn >= 0) => x * myPow(x, nn-1)
          case nn if n < 0 => 1 / myPow(x, -nn)
        }
      }
    }
  }

}
