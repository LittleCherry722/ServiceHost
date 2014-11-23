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

    }
  }

  new Task("Task 2") {
    def solution() = {
      def swap_with_loop(a: Array[Int]): Unit = {
        var offset = 1
        var tmp: Int = a(0)
        for (i <- 0 until a.length) {
          if (offset == 1) {
            tmp = a(i)
            a(i) = a(i + offset min a.length - 1)
            offset = -1
          }
          else {
            a(i) = tmp
            offset = + 1
          }
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[Int]): Array[Int] = {
        def offset(a: Int): Int = if (a % 2 == 0) 1 else -1
        (for (i <- 0 until a.length)
          yield a(i + offset(i) min a.length - 1)).toArray
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      def section(a: Array[Int]): Array[Int] = {
        val (pos, neg) = a.partition(_ > 0)
        pos ++ neg
      }
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here

    }
  }

  new Task("Task 7") {
    def solution() = {
      def myDistinct(a: Array[Int]): Array[Int] = {
        a.distinct
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      import scala.collection.mutable.ArrayBuffer
      def mystery(a: ArrayBuffer[Int]): Unit = {
        val neg_indices = (0 until a.length).filter(i => a(i) < 0).reverse.dropRight(1)
        neg_indices.map(i => a.remove(i))
      }
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
