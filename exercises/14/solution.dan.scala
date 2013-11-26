

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
      def swap(t: Tuple2[Int, Int]) =
        t match {
          case (a, b) => (b, a)
        }
      val x = (1, 2)
      assert(swap(x) == (2, 1))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[Any]) = a match {
        case Array(x, y, rest @ _*) => Array(y, x) ++ rest
        case _ => a
      }
      val x = Array[Any](1, 2, 4, 3)
      assert(swap(x) sameElements Array(2, 1, 4, 3))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(decription: String, price: Double) extends Item
      case class Bundle(decription: String, discount: Double, items: Item*) extends Item
      case class Multiple(quantity: Int, item: Item) extends Item
      def price(item: Item): Double = item match {
        case Multiple(quantity, item) => quantity * price(item)
        case Bundle(_, discount, items @ _*) => items.map(price _).sum - discount
        case Article(_, price) => price
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

      def leafSum(tree: BinaryTree): Int = {
        tree match {
          case Leaf(value) => value
          case Node(left, right) => leafSum(left) + leafSum(right)
        }
      }
      val test = Node(Node(Leaf(3), Leaf(8)), Node(Leaf(2), Leaf(5)))
      assert(leafSum(test) == (3 + 8 + 2 + 5))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def sumNonNode(lst: List[Option[Int]]) = {
        lst.map(_.getOrElse(0)).sum
      }
      val x = List(Some(1), None, Some(2), None, Some(3))

      assert(sumNonNode(x)==6)
    }
  }

}
