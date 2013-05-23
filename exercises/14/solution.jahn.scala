
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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {
      val p = (1, 2)
      val p2 = swap(p)
      println(p2)
    }

    def swap(pair: (Int, Int)) = pair match {
      case (x, y) => (y, x)
    }
  }

  new Task("Task 3") {
    def solution() = {
      println(swap(Array(1, 2, 3, 4, 5)).mkString(", "))
    }

    def swap[T: Manifest](values: Array[T]) = values match {
      case Array(x, y, z@_*) => Array(y, x) ++ z
      case _ => values
    }
  }

  new Task("Task 4") {
    def solution() = {
      val item1 = new SimpleItem(12.95, "Item A");
      val multiple = Multiple(3, item1);

      println(multiple.description + ": " + multiple.price)
    }

    abstract class Item {
      def price: Double

      def description: String
    }

    class SimpleItem(val price: Double, val description: String) extends Item

    case class Multiple(val count: Int, val item: Item) extends Item {
      def price: Double = count * item.price

      def description: String = count + " * " + item.description
    }

  }

  new Task("Task 6") {
    def solution() = {
      val tree = new Node(new Node(new Leaf(1), new Leaf(2)), new Leaf(3))
      println(sum(tree))
    }

    def sum(tree: BinaryTree): Int = tree match {
      case Leaf(value) => value
      case Node(left, right) => sum(left) + sum(right)
    }

    sealed abstract class BinaryTree

    case class Leaf(value: Int) extends BinaryTree

    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

  }

  new Task("Task 9") {
    def solution() = {
      val lst = List(Some(1), None, Some(2), Some(3), None)
      println(optionalSum(lst))
    }

    def optionalSum(lst: List[Option[Int]]) = lst.flatten.sum
  }
}
