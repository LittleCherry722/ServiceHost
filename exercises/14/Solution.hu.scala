package task
import scala.collection.mutable.Map
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

object Tasks extends Tasks {
  new Task("chapter14_2") {
    def solution() = {
      def swap(num: (Int, Int)): (Int, Int) = {
        num match {
          case (first, second) => (second, first)
        }
      }

      print(swap((1, 2)))
    }
  }

  new Task("chapter14_3") {
    def solution() = {
      def swap(num: Array[Int]): Array[Int] = {
        num match {
          case Array(a, b, rest @ _*) => Array(b, a) ++ rest
        }
      }

      val arr = Array(1, 2, 4, 5, 5, 3, 2, 3)
      print(swap(arr).mkString(","))
    }
  }
  
  new Task("chapter13_4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Multiple(num: Int, items: Item) extends Item
      
      def price(it: Item) :Double = {
        it match{
          case Article(_, p) => p
          case Bundle(_, disc, its@_*) => its.map(price).sum - disc
          case Multiple(n, it) => n * price(it)
        }
      }
      val priceOfItem = price(Multiple(10, Article("Blackwell Toaster", 29.95)))
      print(priceOfItem)
    }
  }
  
  new Task("chapter14_6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      
      def sum (tree: BinaryTree): Int = {
        tree match {
          case Leaf(v) => v
          case Node(l, r) => sum(l) + sum(r)
        }
      }
      
      val tree = Node(Node(Leaf(2), Leaf(3)), Node(Leaf(4), Leaf(5)))
      print(sum(tree))
      
    }
  }
  
  new Task("chapter14_9") {
    def solution() = {
      val test : List[Option[Int]]= List(Option(23), Option(90), Option(-12), None)
      print(test.map(_.getOrElse(0)).sum)
    }
  }

}