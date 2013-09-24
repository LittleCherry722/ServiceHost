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
    	def swap(p: (Int, Int)) = p match {
    	  case (a,b) => (b,a)
    	}
    	var p = (23,42)
    	println(p + " swapped is " + swap(p))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(a: Array[_]) = a match {
        case Array(m, n, o @ _*) => Array(n, m) ++ o
        case _ => a
      }
      var a = Array(2,5,63,9)
      println("Array: " + a.mkString(", "))
      println("Swapped: " + swap(a).mkString(", "))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Product(description: String, price: Double) extends Item
      case class Multiple(n: Int, item: Item) extends Item
      
      def price(it: Item): Double = it match {
        case Article(_, p) => p
        case Product(_, p) => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(n, i) => price(i) * n
      }
      print("price(Multiple(10, Product(\"Blackwell Toaster\", 29.95)) = " + price(Multiple(10, Product("Blackwell Toaster", 29.95))))
    }
  }

  new Task("Task 5") {
    def solution() = {
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      def leafSum(t: BinaryTree): Int = t match {
        case Leaf(n) => n
        case Node(l, r) => leafSum(l) + leafSum(r)
      }
      println("leafSum of Node(Node(Leaf(3), Leaf(8)), Leaf(2)) = " + leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2))))
    }
  }

  new Task("Task 7") {
    def solution() = {
    }
  }

  new Task("Task 8") {
    def solution() = {
    }
  }

  new Task("Task 9") {
    def solution() = {
      def optionsSum(l: List[Option[Int]]) = {
        l.map(_.getOrElse(0)).sum
      }
      var list = List(Some(4),None,Some(24))
      println("sum of " + list + " = " + optionsSum(list))
    }
  }

  new Task("Task 10") {
    def solution() = {
    }
  }

}
