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
    def swap(p: (Int, Int)) = p match {
      case (x, y) => (y, x)
    }
    def solution() = {
      val a = (1, 2)
      println("a: "+a)
      println("swapped: "+swap(a))
    }
  }

  new Task("Task 3") {
    def swap(p: Array[Int]) = p match {
      case Array(x, y, z @ _*) => Array(y, x) ++ z
    }
    def solution() = {
      val a = Array(1, 2)
      println("a: "+a.mkString(" "))
      println("swapped: "+swap(a).mkString(" "))

      val b = Array(3, 4, 5, 6)
      println("b: "+b.mkString(" "))
      println("swapped: "+swap(b).mkString(" "))
    }
  }

  new Task("Task 4") {
    abstract class Item {
      def price: Double = {
        this match {
          case Multiple(amount, item) => amount * item.price
          case Product(_, price) => price
          case Bundle(items) => items.foldLeft(0.0)(_ + _.price)
	}
      }
    }
    case class Multiple(amount: Int, item: Item) extends Item
    case class Product(name: String, productprice: Double) extends Item
    case class Bundle(items: Collection[Item]) extends Item

    def solution() = {
      val myRucksack = Bundle(Array(Product("Schokolade", 0.95), Multiple(2, Product("Saftschorle", 0.6))))
      println("myRucksack is worth: " + myRucksack.price)
    }
  }

  new Task("Task 6") {
    sealed abstract class BinaryTree {
      def sum: Int = {
        this match {
          case Leaf(v) => v
          case Node(l, r) => l.sum + r.sum
	}
      }
    }
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

    def solution() = {
      val myTree = Node(Leaf(1), Node(Leaf(2), Leaf(3)))
      println("sum: " + myTree.sum)
    }
  }

  new Task("Task 9") {
    def mySum(l: List[Option[Int]]): Int = {
      def getVal(opt: Option[Int]): Int = {
        if (opt != None) opt.get else 0
      }
      l.foldLeft(0)(_ + getVal(_))
    }
    def solution() = {
      val l: List[Option[Int]] = List(Some(1), Some(2), None, Some(3))
      println("sum: " + mySum(l))
    }
  }
}
