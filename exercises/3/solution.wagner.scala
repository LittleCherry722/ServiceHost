import collection.mutable.ArrayBuffer

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

  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      for (n <- (0 until a.size).filter(_ % 2 == 1)) {
        val buf = a(n)
        a(n) = a(n - 1)
        a(n - 1) = buf
      }
      a.foreach(println)
    }
  }

  new Task("Task 3") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      val a2 = for (n <- (0 until a.size)) yield {
        if (n % 2 == 1) a(n - 1)
        else if (a.size > n + 1) a(n + 1)
        else a(n)
      }
      a2.foreach(println)
    }
  }

  new Task("Task 4") {
    def solution() = {
      val a = Array(1, -1, 0, 3, -4, 5)
      val a2 = a.filter(_ > 0) ++ a.filter(_ <= 0)
      a2.foreach(println)
    }
  }

  new Task("Task 7") {
    def solution() = {
      val a = Array(1, 1, 2, 3, 3, 3, 4, 5, 6)
      a.toSet.foreach(println)

    }
  }

  new Task("Task 8") {
    def solution() = {
      val a = ArrayBuffer(1, 2, 3, 4, -5, 6, -7, -8)
      val indexes = for (i <- (0 until a.size) if a(i) < 0) yield i
      for (j <- indexes.drop(1).reverse) a.remove(j)
      a.foreach(println)
    }
  }

}
