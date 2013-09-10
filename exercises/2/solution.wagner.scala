object Solution extends App {
  Tasks.execute()
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
      def signum(num: Int): Int = {
        if (num > 0) 1
        else if (num < 0) -1
        else 0
      }
      assert(signum(5) == 1)
      assert(signum(-5) == (-1))
      assert(signum(0) == 0)
    }
  }

  new Task("Task 2") {
    def solution() = {
      // value: ()
      // type: Unit
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

      def countdown(n: Int) {
        (0 to n).reverse.foreach(println)
      }
      countdown(5)

    }
  }

  new Task("Task 6") {
    def solution() = {
      var prod = 1
      for (c <- "Hello") {
        prod *= c.toInt
      }
      println(prod)
    }
  }

  new Task("Task 9") {
    def solution() = {

      def calcProd(s: String): Int = if (s.isEmpty) 1 else s.head.toInt * calcProd(s.tail)
      println(calcProd("Hello"))

    }
  }

  new Task("Task 10") {
    def solution() = {

      def pot(x: Int, n: Int): Int = {
        if (n > 0)
          if (n % 2 == 0) pot(x, n / 2) * pot(x, n / 2)
          else x * pot(x, n - 1)
        else if (n < 0)
          1 / pot(x, (-n))
        else
          1
      }
      assert(pot(2, 5) == 32)
      assert(pot(2, -1) == 1 / 2)
      assert(pot(2, 0) == 1)

    }
  }

}