object Solution extends App {
  Tasks.execute()
}

abstract class Task(val name: String) {
  Tasks.add(this)

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = tasks :+= t
  def execute() = tasks foreach { _.execute() }
  def execute(name: String) =
    (tasks filter { _.name==name }).head.execute()
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {
      def swap(is: (Int,Int)) = is match { case (i1,i2) => (i2,i1) }
      println(swap((1,2)))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[Int]) = a match {
        case a if a.length>=2 => { val tmp=a(0); a(0) = a(1); a(1) = tmp; a }
        case a => a
      }
      println(swap(Array(1,2)).mkString("Array(", ",", ")"))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Multiple(n: Int, o:Item) extends Item
      def price(it: Item): Double = it match {
          case Article(_, p) => p
          case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
          case Multiple(n, i) => price(i) * n
      }
      val it = Multiple(2, Bundle("Cappuchino & Muffin", 1.0, Article("Cappuchino", 3), Article("Muffin", 2)))
      println(price(it)) // ((3+2) - 1) * 2 = 8
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      def sum(t: BinaryTree):Int = t match {
        case Leaf(v) => v
        case Node(l, r) => sum(l) + sum(r)
      }
      val t = Node(Node(Leaf(5),Node(Leaf(2),Leaf(3))),Leaf(5))
      println(sum(t))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def sum(l: List[Option[Int]]) = l flatMap { i => i} sum
      val l = List(Some(1), None, Some(3))
      println(sum(l))
    }
  }

}
