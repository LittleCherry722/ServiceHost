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
      def swap(x: Pair[Int, Int]) = x match {
        case (a, b) => Pair(b, a)
      }

      println(swap(Pair(1, 2)))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[Int]) = a match {
        case Array(x, y, z @ _*) => Array(y, x) ++ z
      }

      val x = Array(1, 2, 3, 4)
      println(swap(x).mkString(", "))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Product(name: String, price: Double) extends Item
      case class Multiple(amount: Int, product: Product) extends Item

      def price(i: Item): Double = i match {
        case Product(name, price) => price
        case Multiple(amount, product) => amount * price(product)
      }

      val p1 = Product("iphone", 600)
      val m1 = Multiple(10, p1)
      println(price(m1))
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

      def treeSum(t: BinaryTree): Int = t match {
        case Leaf(v) => v
        case Node(l, r) => treeSum(l) + treeSum(r)
      }

      val t = Node(Node(Leaf(3), Leaf(8)), Node(Leaf(2), Leaf(5)))
      println(treeSum(t))
    }
  }

  new Task("Task 9") {
    def solution() = {

      def nonNoneSum(l: List[Option[Int]]) = l.filter(_ != None).map(_.get).sum

      val l = List(Some(1), None, Some(2), None, Some(3), Some(4), Some(5))
      println(nonNoneSum(l))
    }
  }

}
