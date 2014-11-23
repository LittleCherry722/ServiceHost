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
      def random_array(n: Int) = {
        for (_ <- (0 to n).toArray) yield util.Random.nextInt
      }
      println(random_array(10).mkString(" "))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      for (i <- 0.until(a.length - 1, 2)) {
        val tmp = a(i + 1)
        a(i + 1) = a(i)
        a(i) = tmp
      }
      println(a.mkString(" "))
    }
  }

  new Task("Task 3") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      val b = for (i <- 0.until(a.length, 2); j <- Array(i + 1, i) if j < a.length) yield a(j)
      println(a.mkString(" "))
      println(b.mkString(" "))
    }
  }

  new Task("Task 4") {
    def solution() = {
      val array = Array(0, 1, -4, 3, -2, 0, 5, 10, 8, -5)
      val part = array.partition(_ > 0)
      val res = part._1 ++ part._2
      println(res.mkString(" "))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val array = Array(0, 1, 2, 3, 1, 2, 2, 1, 3, 4)
      println(array.distinct.mkString(" "))
    }
  }

  new Task("Task 8") {
    def solution() = {
      import scala.collection.mutable.ArrayBuffer
      val array = ArrayBuffer(0, 1, -4, 3, -2, 0, -1, 5, 10, 8, -5)
      val negindexes = for (i <- 0 until array.length if array(i) < 0) yield i
      val badindexes = negindexes.reverse.dropRight(1)
      for (i <- badindexes) array.remove(i)
      println(array.mkString(" "))
      // while its better than the first naive approach,
      // we still have to move elements multiple times (e.g. 8)
      // so the second approach is still better as every element
      // is moved at most once
    }
  }
}
