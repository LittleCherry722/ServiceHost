
package chapter14

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length

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

  new Task("Task 2") {
    def solution() = {
      def swap(t: (Int,Int)) = t match {
        case (x, y) => (y, x)
        case _ => None
      }
      
      val t = (1,2)
      println("t = " + t.toString)
      println("swap(t) = " + swap(t))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[Int]): Array[Int] = a match {
//        case Array(x, y) => Array(y,x)
        case Array(a, b, rest @ _ *) => Array(b, a) ++ rest
        
      }
      val a = Array(1, 2, 3, 4)
      println("a = " + a.mkString("Array(", ", ", ")"))
      println("swap(a) = "  + swap(a).mkString("Array(", ", ",")"))
    }
  }
  new Task("Task 4") {
    def solution() = {
     
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Multiple(a: Int, i: Item*) extends Item
      
      def price(it: Item): Double = it match {
        case Article (_, p) => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(x, its @ _*) => its.map(price _).sum * x
      }
      
      // TESTS
      val m1 = Multiple(10, Article("Blackwell Toaster", 29.95))
      val m2 = Multiple(10, Multiple(10, Article("Blackwell Toaster", 29.95)))
      val m3 = Multiple(10, Bundle("Some Discount", .95, Article("Blackwell Toaster", 29.95)))
      println("m1 = Multiple(10, Article(\"Blackwell Toaster\", 29.95))")
      println("price(m1) = " + price(m1) + "\n")
      println("m2 = Multiple(10, Multiple(10, Article(\"Blackwell Toaster\", 29.95)))")
      println("price(m2) = " + price(m2) + "\n")
      println("m3 = Multiple(10, Bundle(\"Some Discount\", .95, Article(\"Blackwell Toaster\", 29.95)))")
      println("price(m3) = " + price(m3) + "\n")
    }
  }
  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      
      def treeSum (tree: BinaryTree): Int = tree match {
        case Leaf(x) => x
        case Node(left, right) => treeSum(left) + treeSum(right)
      }
      val tree = Node(Node(Leaf(10), Leaf(5)), Leaf(15))
      println("tree = tree = Node(Node(Leaf(10), Leaf(5)), Leaf(15))")
      println("treeSum(tree) = " + treeSum(tree))
    }
  }
  
  new Task("Task 9") {
    def solution() = {
      def sumOption(lst: List[Option[Int]]): Int =  sum(lst flatten) 
      def sum(lst: List[Int]) = lst sum
    
      
      val list = List(Some(1), Some(2), None, Some(4))
      
      println("list = List(Some(1), Some(2), None, Some(4))")
      println("sumOption(list) = " + sumOption(list))
      
    }
  }
}
