import java.util.LinkedList
import scala.collection.mutable.LinkedList
import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach
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
  def execute(name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

    }
  }

  new Task("Task 2") {
    def solution() = {

      def swap(pair: (Int, Int)) = {
        pair match {
          case (a, b) => (b, a)
        }
      }

      println(swap(1, 2))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(arr: Array[Any]) = {
        arr match {
          case Array(a, b, x @ _*) => Array(b, a) ++ x
        }
      }

      println(swap(Array(1, 2, 3, 4)).mkString(" "))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item

      case class Multiple(count: Int, item: Item) extends Item

      def price(it: Item): Double = it match {
        case Article(_, p) => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(count, item) => count * price(item)
      }
      val p = Bundle("Pool", 1.0,
        Multiple(3, Article("Drill", 30.11)),
        Multiple(10, Bundle("Im Bored", 10.0,
          Multiple(2, Article("This example is way too long", 00.01)),
          Article("Even longer", 13.37))))

      println(price(p))
    }
  }

  new Task("Task 5") {
    def solution() = {
      case class Single(x: Int)

      def leafSum(lst: List[Any]): Int = lst match {
        case Nil => 0
        case (x: Int) :: other => x + leafSum(other)
        case (x: List[Any]) :: other => leafSum(x) + leafSum(other)
      }

      println(leafSum(List(List(1, 2), 3, List(List(1, 1)))))
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      def leafSum(tree: BinaryTree): Int = tree match {
        case Leaf(x) => x
        case Node(left, right) => leafSum(left) + leafSum(right)
      }
      println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2))))
    }
  }

  new Task("Task 7") {
    def solution() = {
      sealed abstract class Tree
      case class Leaf(value: Int) extends Tree
      case class Node(children: Tree*) extends Tree

      def leafSum(tree: Tree): Int = tree match {
        case Leaf(x) => x
        case Node(children @ _*) => children.map(leafSum).sum
      }
      println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))))
    }
  }

  new Task("Task 8") {
    def solution() = {
      sealed abstract class Tree
      case class Leaf(value: Int) extends Tree
      case class Node(op: (Int, Int) => Int, children: Tree*) extends Tree

      def eval(tree: Tree): Int = tree match {
        case Leaf(x) => x
        case Node(op, single) => op(0, eval(single))
        case Node(op, children @ _*) => children.map(eval).reduceLeft(op)
      }

      println(eval(Node(_ + _, Node(_ * _, Leaf(3), Leaf(8)), Leaf(2), Node(_ - _, Leaf(5)))))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def sum(lst: List[Option[Int]]) = lst.map(_.getOrElse(0)).sum
      println(sum(List(None, Some(2), Some(3), None, Some(5))))
    }
  }

  new Task("Task 10") {
    def solution() = {
      def compose(a: Double => Option[Double], b: Double => Option[Double]) = {
        (x: Double) =>
          a(x) match {
            case None => None
            case Some(y) => b(y)
          }
      }
      def a(x: Double) = if (x >= 0) Some(math.sqrt(x)) else None
      def b(x: Double) = if (x != 1) Some(1 / (x - 1)) else None
      val c = compose(a, b)
      println(c(2), c(1), c(0))
    }
  }
}
