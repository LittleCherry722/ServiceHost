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

  def swap(in: Pair[Int, Int]): Pair[Int, Int] = in match {
    case (n, m) => (m, n)
  }

  new Task("Task 2") {
    def solution() = {

      println(swap((5, 6)))
    }
  }

  def swapArray(in: Array[Any]): Array[Any] = in match {
    case (Array(x, y, _*)) => Array(y, x) ++ in.slice(2, in.length)
    case _ => in
  }

  new Task("Task 3") {
    def solution() = {
      println(swapArray(Array(1, 2, 3)).toList)
      println(swapArray(Array(3)).toList)
      println(swapArray(Array(1, 2)).toList)
    }
  }

  abstract class Item
  case class Article(description: String, price: Double) extends Item
  case class Bundle(description: String, discount: Double, items: Item*) extends Item
  case class Multiple(amount: Int, it: Item) extends Item

  def price(it: Item): Double = it match {
    case Article(_, p) => p
    case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
    case Multiple(n, it) => n * price(it)
  }

  new Task("Task 4") {
    def solution() = {
      val item = Multiple(10, Multiple(3, Article("Blackwell Toaster", 5)))
      println(price(item))
    }
  }

  sealed abstract class BinaryTree
  case class Leaf(value: Int) extends BinaryTree
  case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

  new Task("Task 6") {
    def solution() = {
      println(sum(Node(Node(Leaf(3), Leaf(4)), Node(Leaf(5), Node(Leaf(6), Leaf(7))))))
    }
  }

  def sum(in: BinaryTree): Int = in match {
    case Leaf(v) => v
    case Node(l, r) => sum(l) + sum(r)
  }

  def sumNonNone(list: List[Option[Int]]): Int = {
    var sum = 0;
    for (i <- 0 to list.length - 1) {
      sum += list(i).getOrElse(0)
    }
    sum
  }

  new Task("Task 9") {
    def solution() = {
      println(sumNonNone(List(Some(4), None, Some(5))))
    }
  }

}
