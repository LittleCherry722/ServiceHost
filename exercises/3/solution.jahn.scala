import scala.collection.mutable.ArrayBuffer

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)

      for (i <- 0 until (a.length / 2)) {
        val index1 = 2 * i
        val index2 = 2 * i + 1

        val tmp = a(index1)
        a(index1) = a(index2)
        a(index2) = tmp
      }

      println(a.mkString(","))
    }
  }

  new Task("Task 3") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)

      val tmp = for (g <- a.grouped(2)) yield g match {
        case Array(x, y) => Array(y, x)
        case z => z
      }

      println(tmp.flatten.mkString(","))
    }

  }

  new Task("Task 4") {
    def solution() = {
      val a = Array(4, -6, 3, 0, 7, -2)
      val sorted = task4(a)
      println(sorted.mkString(","))
    }

    def task4(a: Array[Int]) = {
      val buffer = ArrayBuffer[Int]()
      buffer ++= a.filter(_ > 0)
      buffer ++= a.filterNot(_ > 0)

      buffer.toArray
    }
  }

  new Task("Task 7") {
    def solution() = {
      val a = Array(1, 1, 1, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4)
      val s = a.toSet
      println(s.mkString(","))
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a = Array(1, -2, 3, -4, 5, -6, 7, -8, 9).toBuffer

      val indices = for (i <- 0 until a.length if a(i) < 0) yield i
      for (i <- indices.drop(1).reverse) a.remove(i)

      println(a.mkString(","))
    }

  }

}
