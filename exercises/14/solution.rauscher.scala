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
      def swap(tuple: Tuple2[Any,Any]) = tuple match {
        case (a,b) => (b,a)
      }
      
      println(swap(1,2))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(list: Array[Any]) = list match {
        case Array(a, b, r @ _*) => Array(b, a) ++ r
        case _ => list
      }
      
      println(swap(Array(2,3,4)).mkString("<",",",">"))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(decription: String, price: Double) extends Item
      case class Bundle(decription: String, discount: Double, items: Item*) extends Item
      case class Multiple(amount: Int, item: Item) extends Item
      
      def price(item: Item): Double = item match {
        case Article(_, price) => price
        case Bundle(_, discount, items) => discount * price(items)
        case Multiple(amount, item) => amount * price(item)
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
      
      def sum(tree: BinaryTree): Int = tree match {
        case Leaf(a) => a
        case Node(left, right) => sum(left) + sum(right)
      }
    }
  }

  new Task("Task 9") {
    def solution() = {
      def listSum(list: List[Option[Int]]) = (for (i <- list if i != None) yield i.get).sum
      
      println(listSum(List(Some(1), None)))
    }
  }

}
