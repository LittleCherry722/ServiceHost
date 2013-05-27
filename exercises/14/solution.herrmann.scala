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



object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {
    	def swap(x: Pair[Int, Int]) = x match {
    	  case (a, b) => Pair(b, a)
    	}
    	
    	val p = Pair(1, 2)
    	println(swap(p))
    }
  }

  new Task("Task 3") {
    def solution() = {
    	def swap(a: Array[Int]) = a match {
    	  case Array(x, y, z @ _*) => Array(y, x) ++ z
    	}
    	
    	val a = Array(1, 2, 3, 4, 5)
    	println(swap(a).mkString)
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
    	
    	val m = Multiple(10, Product("Blackwell Toaster", 29.95))
    	
    	println(price(m))
    }
  }

  new Task("Task 6") {
    def solution() = {
    	sealed abstract class BinaryTree
    	case class Leaf(value: Int) extends BinaryTree
    	case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
    	
    	def sum(b: BinaryTree): Int = b match{
    	  case Leaf(value) => value
    	  case Node(left, right) => sum(left) + sum(right)
    	}
    	
    	val tree = Node(Node(Leaf(1), Leaf(2)), Leaf(3))
    	println(sum(tree))
    }
  }

  new Task("Task 9") {
    def solution() = {
    	def sum(l: List[Option[Int]]) = l flatMap { i => i} sum
    	
    	val l = List(Some(1), Some(2), None, Some(4))
    	println(sum(l))
    }
  }

}