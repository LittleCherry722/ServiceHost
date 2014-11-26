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
    }
  }

  new Task("Task 2") {
    def solution() = {
      def swap(x: (Int, Int)): (Int, Int) = {
        x match {
          case (first, second) => (second, first)
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(x: Array[Int]): Array[Int] = {
        x match {
          case Array(first, second, rest@_*) => Array(second, first) ++ rest
          case y => y
        }
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Multiple(number: Int, article: Item) extends Item
      def price(it: Item): Double = it match {
        case Article(_, p) => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(number, item) => number * price(item)
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
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      def sum(b: BinaryTree): Int = b match {
        case Leaf(value) => value
        case Node(left, right) => sum(left) + sum(right)
      }
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {
      def sum(values: List[Option[Int]]): Int = {
        values.map(_.getOrElse(0)).sum
      }
    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
