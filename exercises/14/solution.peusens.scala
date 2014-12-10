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

      // your solution for task 2 here
      def swap(pair: (Int, Int)) = pair match {
    	case (x, y) => (y, x)
      }
    	
      val test = (-1, 1)
      println(swap(test))
    }
  }

  new Task("Task 3") {

      // your solution for task 3 here
    def solution() = {
      def swap2(array: Array[Int]) = array match {
    	case Array(x, y, rest @ _*) => Array(y, x) ++ rest
      }
      
      val test = Array(-1, 0, 1)
      println(swap2(test).mkString(","))
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      abstract class Item
      
      case class Article(text: String, price: Double) extends Item
      case class Bundle(itemColl: Collection[Item]) extends Item      
      case class Multiple(amount: Int, item: Item) extends Item
      
      def price(it: Item): Double = it match {
        case Article (_, price) => price
        case Bundle(itemColl) => items.foldLeft(0.0)(_ + _.price)
        case Multiple(amount, item) => amount * item.price
      }
      
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      
      def leafSum (btree: BinaryTree): Int = btree match {
        case Node(left, right) => leafSum(left) + leafSum(right)
        case Leaf(value) => value
      }
      
      var testTree = Node(Node(Leaf(1), Leaf(2)), Node(Leaf(3), Leaf(4)))
      println(leafSum(testTree))
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }
}
