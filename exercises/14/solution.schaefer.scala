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
      println(swap(2, 4))
    }

    def swap(pair: (Int, Int)): (Int, Int) = pair match {
      case (x, y) => (y, x)
    }
  }

  new Task("Task 3") {
    def solution() = {
      val arr = Array(1, 2, 3, 4, 5, 6)
      println(swap(arr).mkString(", "))
    }

    def swap(arr: Array[Int]): Array[Int] = arr match {
      case Array(x, y, z @ _*) => Array(y, x) ++ z
    }

  }

  new Task("Task 4") {
    def solution() = {
      //this solution isn't very intuitive, but it works and shows how the concept of case classes works
      val simpleItem = new SimpleItem("Brick", 4.2)
      val multi = new Multiple(10, simpleItem)
      val multiMult = new Multiple(100, multi)
      println(simpleItem)
      println(multi)
      println(multiMult)
    }

    abstract class Item {
      def description(): String
      def price(): Double
      override def toString() = description + ": " + price
    }
    case class SimpleItem(val description: String, price: Double) extends Item {}

    case class Multiple(amount: Int, item: Item) extends Item {
      def description(): String = {
        amount + " x " + item.description
      }

      def price(): Double = item match {
        case SimpleItem(description, price) => amount * price
        case Multiple(amount, product) => amount * item.price()
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      val leaf1 = Leaf(42)
      val leaf2 = Leaf(-21)
      val leaf3 = Leaf(5)
      val leaf4 = Leaf(5)
      val node1 = Node(leaf1, leaf2)
      val node2 = Node(leaf3, leaf4)
      val tree = Node(node1, node2)
      println(sum(tree))
    }
    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree {}

    def sum(tree: BinaryTree): Int = tree match {
      case Leaf(value) => value
      case Node(left, right) => sum(left) + sum(right)
    }
  }

  new Task("Task 9") {
    def solution() = {
      val list = List(Some(0), Some(42), None, Some(5), None, None)
      println(sum(list))

    }

    def sum(list: List[Option[Int]]): Int = {
      (list flatten) sum
    }
  }

}
