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
      println(signum(-10))
      prinltn(signum(0))
      println(signum(20))

      def signum(x: Double) = {
        if (x < 0) -1 else {
          if (x == 0) 0 else 1
        }
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("Value of an emtpy block expression {} is (), its type is Unit")
    }
  }

  new Task("Task 3") {
    def solution() = {
      var x = ()
      var y = 0
      x = y = 1
    }
  }

  new Task("Task 4") {
    def solution() = {
      for (i <- (0 to 10).reverse) {
        println(i)
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      def countdown(n: Int) => {
        for (i <- (0 to n).reverse) {
          println(i)
        }
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      val s = "Hello"
      var prod = 1
      for (c <- s) {
        prod *= c
      }
      println(prod)
    }
  }

  new Task("Task 7") {
    def solution() = {
      val s = "Hello"
      val prod = s.foldLeft(1L)(_ * _)
      println(prod)
    }
  }

  new Task("Task 8") {
    def solution() = {
      def product(s: String) = {
        s.foldLeft(1L)(_ * _)
      }
    }
  }

  new Task("Task 9") {
    def solution() = {
      println(product("Hello"))
      
      def product(s: String): Long = s match {
      	case "" => 1L
      	case _ => s.head.toLong * product(s.tail)
      }
    }
  }

  new Task("Task 10") {
    def solution() = {
      def power(x: Double, n: Int): Double = {
        if (n < 0) 1 / power(x, -n) else {
          if (n == 0) 1 else {
            if (n % 2 == 0) power(x, n / 2) * power(x, n / 2) else x * power(x, n - 1)
          }
        }
      }
    }
  }

}
