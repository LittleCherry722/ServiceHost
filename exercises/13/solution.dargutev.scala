import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap
import java.awt.geom.Rectangle2D
import java.awt.Point
import scala.collection.mutable.SortedSet
import scala.collection.immutable.List
import scala.collection.immutable.Map
import scala.collection.mutable.LinkedList

object Solution extends App {

  // execute all tasks
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

  def values(fun: (Int) => Int, low: Int, high: Int) = {
    for (i <- low until high + 1)
      yield (i, fun(i))
  }

  new Task("Task 1") {
    def solution() = {

      println(toMapToSet("Mississippi"))
    }
  }

  def toMapToSet(text: String): scala.collection.mutable.Map[Char, SortedSet[Int]] = {
    val lowerText = text.toLowerCase();
    val distinctText = lowerText.distinct;

    var result = scala.collection.mutable.Map[Char, SortedSet[Int]]()
    for (i <- 0 to distinctText.length() - 1) {
      var set = SortedSet[Int]()
      for (j <- 0 to lowerText.length() - 1) {
        if (distinctText.charAt(i) == lowerText.charAt(j)) {
          set += j
          result += (distinctText.charAt(i) -> set)
        }
      }
    }
    result
  }

  new Task("Task 2") {
    def solution() = {
      println(toMapToList("Mississippi"))
    }
  }

  def toMapToList(text: String): Map[Char, List[Int]] = {
    val lowerText = text.toLowerCase();
    val distinctText = lowerText.distinct;
    var result = Map[Char, List[Int]]()
    for (i <- 0 to distinctText.length() - 1) {
      var list = List[Int]()
      for (j <- 0 to lowerText.length() - 1) {
        if (distinctText.charAt(i) == lowerText.charAt(j)) {
          list = list :+ j
          result += (distinctText.charAt(i) -> list)
        }
      }
    }
    result
  }

  new Task("Task 3") {
    def solution() = {
      println(removeZeros(LinkedList(1, 0, 0, 0, 0, 0, 4, 6, 0)))
    }
  }

  def removeZeros(list: LinkedList[Int]): LinkedList[Int] = {
    for (i <- list if (i != 0)) yield i
  }

  def flat(arr: Array[String], map: Map[String, Int]): Array[Int] = {
    arr.flatMap(x => map.get(x))
  }

  new Task("Task 4") {
    def solution() = {

      for (x <- flat(Array("t", "s"), Map("t" -> 3, "s" -> 2, "d" -> 1))) {
        print(x + ",")
      }
    }
  }

  new Task("Task 6") {
    def solution() = {

      println((List(1, 2, 3, 4, 5).reverse :\ List[Int]())(_ +: _))
      // :\ is the same as foldRight, whereas /: is the same as foldLeft

    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(1, 2, 3, 4, 5)
      val quantities = List(2, 3, 4, 5, 6)
      println((prices zip quantities) map { Function.tupled(_ * _) })
    }
  }

}
