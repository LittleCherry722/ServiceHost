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
      val a1 = Array((0,1), (5, 4), ('S', 'B'), (2.0, 4.0), ("David", "Dave"), (3, 29))
      println(a1.mkString("Array(", ", ", ")"))
      def swap(tup: (Any, Any)) = tup match {
        case (x: Int, y: Int) => (y, x)
        case _ => tup
      }
      println(a1.map(swap).mkString("Array(", ", ", ")"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      val a1 = Array(Array(1), Array(2, 9), Array(8, 6, 5), Array(4, 5, 7, 8))
      println(a1.map(_.mkString("[", ",", "]")).mkString("Array(", ", ", ")"))
      def swap(arr: Array[Int]) = arr match {
        case Array(a, b, c @ _*) => Array(b, a) ++ c
        case _ => arr
      }
      println(a1.map(swap).map(_.mkString("[", ",", "]")).mkString("Array(", ", ", ")"))
    }
  }

  new Task("Task 4") {
    def solution() = {
      sealed abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      case class Multiple(multi: Int, itm: Item) extends Item

      def price(it: Item) : Double = it match {
        case Article(_, p) => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(multi, itm) => multi * price(itm)
      }

      val bundle = Bundle("Father's day special", 20.0,
        Article("Scala for the Impatient", 39.95),
        Multiple(2,
          Bundle("Anchor Distillery Sampler", 10.0,
            Article("Old Potrero Straigt Rye Whiskey", 79.95),
            Article("Junipero Gin", 32.95)
          )
        ),
        Multiple(10, Article("Blackwell Toaster", 29.95))
      )
      println("Bundle costs: " + price(bundle))
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

      def sum(tree: BinaryTree): Int = tree match {
        case Node(left, right) => sum(left) + sum(right)
        case Leaf(value) => value
      }

      println("Tree-Sum: " + sum(Node(Node(Node(Leaf(3), Leaf(8)), Leaf(2)), Leaf(5))))
    }
  }

  new Task("Task 9") {
    def solution() = {
      val lst = List(None, Some(3), None, Some(8), Some(2), None, None, Some(5), None)
      println("Tree-Some-Sum: " + lst.flatten.sum)
    }
  }
}
