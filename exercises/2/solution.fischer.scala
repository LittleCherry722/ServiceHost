
package chapter2

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
      def sig(i: Int) = {
        if (i == 0) 0
        else if (i > 0) 1 else -1
      }
      println(sig(-5))
      println(sig(0))
      println(sig(5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("Unit")
    }
  }
  new Task("Task 4") {
    def solution() = {
      (0 to 10).reverse.foreach(println)
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
  new Task("Task 10") {
    def solution() = {
      def mypow(b: Int, e: Int): Int = {
        println("b: " + b + ", e: " + e)
        if (e == 0) 1
        else {
          if (e > 0) {

            if (e % 2 == 0) {
              val y = mypow(b, e / 2)
              print(" currentY: " + y);
              print(" square: " + y * y + "\n");
              y * y
            } else b * mypow(b, e - 1)
          } else 1 / mypow(b, -e)
        }
      }
      println(mypow(10, 10))
    }
  }

}
