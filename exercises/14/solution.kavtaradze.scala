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

      def swap(p: Tuple2[Int, Int]) = p match {
      	case (a, b) => (b, a)
      }

      val x = (0, 1)
      val y = swap(x)

      println(x)
      print(y)

    }
  }

  new Task("Task 3") {
    def solution() = {

      def swap(a: Array[Int]) = a match {
      	case Array(a, b, rest @ _*) => Array(b, a) ++ rest
	  }

      val x = Array(0, 1, 2, 3)
      val y = swap(x)

      println(x.mkString(" "))
      println(y.mkString(" "))

    }
  }

  new Task("Task 4") {
    def solution() = {

      abstract class Item 
      case class Article(desc: String, price: Double) extends Item 
      case class Multiple(count: Int, item: Item) extends Item

      def price(item: Item): Double = item match { 
      	case Article(_, p) => p
      	case Multiple(count, item) => price(item) * count
  
      	}

      val a = Article("Blackwell Toaster", 29.95)
      val b = Multiple(10, a)

      println(a)
      println("Price: " + price(a))
      println(b)
      print("Total Price:" + price(b))

    }
  }

  new Task("Task 6") {
    def solution() = {

      sealed abstract class BinaryTree 
      case class Leaf(value: Int) extends BinaryTree 
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

      def Sum(tree: BinaryTree): Int = tree match {
      	case Node(left, right) => Sum(left) + Sum(right)
      	case Leaf(a) => a
      }

      val x = Node(Node(Leaf(3), Leaf(6)), Leaf(9))
      println(x)
      println("Sum:" + Sum(x))

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

}
