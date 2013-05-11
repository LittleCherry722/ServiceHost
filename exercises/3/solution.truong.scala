import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import java.util.TimeZone
import java.awt.datatransfer._
import collection.JavaConversions._

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
      val n = 20
      val a = (for (i <- 0 to n) yield Random.nextInt(n + 1)).toArray
      for (i <- a) {
        println(i)
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      for (i <- 0 until a.length if i % 2 == 1) {
        val temp = a(i)
        a(i) = a(i - 1)
        a(i - 1) = temp
      }
      for (i <- 0 until a.length) {
        println(a(i))
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      var a = Array(1, 2, 3, 4, 5)
      a = (for (i <- 0 until a.length) yield (if (i % 2 == 1) a(i - 1) else {
        if (i < a.length - 1) a(i + 1) else a(i)
      })).toArray
      for (i <- 0 until a.length) {
        println(a(i))
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      val a = Array(1, -2, 3, -4, -5, 6, 0, 7, -9, 14)
      val positive = for (i <- a if i > 0) yield i
      val negative = for (i <- a if i <= 0) yield i
      val result = positive ++ negative
      for (i <- result) println(i)
    }
  }

  new Task("Task 5") {
    def solution() = {
      val a = Array(1.0, 2.0, 4.0, 5.0)
      println(a.sum / a.length)
    }
  }

  new Task("Task 6") {
    def solution() = {
      val a = Array(1, -2, 4, 2, -5, 6)
      val a_sorted = a.sorted.reverse
      for (i <- a_sorted) println(i)

      val b = ArrayBuffer(1, -2, 4, 2, -5, 6)
      val b_sorted = b.sorted.reverse
      for (i <- b_sorted) println(i)
    }
  }

  new Task("Task 7") {
    def solution() = {
      val a = Array(1, -2, 4, 2, -5, 6, 1, -2, 4)
      val a_new = a.distinct
      for (i <- a_new) println(i)
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a = ArrayBuffer(1, -2, 4, 2, -5, 6, -1)
      val negIndexes = (for (i <- 0 until a.length if a(i) < 0) yield i).reverse
      for (i <- negIndexes.dropRight(1)) a.remove(i)
      println(a.mkString(", "))
    }
  }

  new Task("Task 9") {
    def solution() = {
      val timezones = TimeZone.getAvailableIDs()
      val prefix = "America/"
      val americaZones = for (i <- timezones if i.startsWith(prefix)) yield i.stripPrefix(prefix)
      println(americaZones.mkString(", ")
    }
  }

  new Task("Task 10") {
    def solution() = {
      val flavors = SystemFlavorMap.getDefaultFlavorMap().asInstanceOf[SystemFlavorMap]
      val f = flavors.getNativesForFlavor(DataFlavor.imageFlavor)
      val buffer = for (i <- f) yield i
      println(buffer.mkString(", "))
    }
  }

}
