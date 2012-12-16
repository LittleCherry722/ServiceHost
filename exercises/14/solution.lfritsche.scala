object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

      // your solution for task 1 here

    }
  }

  new Task("Task 2") {
    def solution() = {
      println(swap("1", "2"))
      println(swap("1", 2))
      println(swap(1, 2))
    }

    def swap(a: Any, b: Any) = {
      a match {
        case a: Int =>
          b match {
            case b: Int => (b, a)
            case _ => (a, b)
          }
        case _ => (a, b)
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      println(swap(Array(0)).mkString(" "))
      println(swap(Array(2, 3, 4, 1)).mkString(" "))
    }

    def swap[T](array: Array[T]) = {
      array.length match {
        case 0 => array
        case 1 => array
        case _ => val tmp = array(0); array(0) = array(1); array(1) = tmp; array
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      println(Item.price(Bundle("Test", List(Product("Apfel", 1), Multiple(30, Product("Banane", 2))))))

    }

    abstract class Item
    case class Multiple(itemNumber: Int, item: Item) extends Item
    case class Product(description: String, price: Double) extends Item
    case class Bundle(description: String, items: List[Item]) extends Item

    object Item {
      def price(item: Item): Double = {
        item match {
          case Multiple(itemNumber, item) => itemNumber * price(item)
          case Product(_, price) => price
          case Bundle(_, items) => items.map(price _).reduceLeft(_ + _)
        }
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      println(leafSum(List(List(3, 8), 2, List(5))))
    }

    def leafSum(list: List[Any]): Int = {
      list match {
        case Nil => 0
        case (x: Int) :: tail => x + leafSum(tail)
        case (x: List[Any]) :: tail => leafSum(x) + leafSum(tail)
        case _ => 0
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      println(BinaryTree.leafSum(
        Node(Node(Leaf(3), Node(Leaf(3), Leaf(8))), Leaf(10))))
    }

    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

    object BinaryTree {
      def leafSum(bTree: BinaryTree): Int = {
        bTree match {
          case l: Leaf => l.value
          case n: Node => leafSum(n.left) + leafSum(n.right)
        }
      }
    }
  }

  new Task("Task 7") {
    def solution() = {

      println(BinaryTree.leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))))

    }

    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(branches: BinaryTree*) extends BinaryTree

    object BinaryTree {
      def leafSum(bTree: BinaryTree): Int = {
        bTree match {
          case l: Leaf => l.value
          case n: Node => n.branches.map(leafSum(_)).reduceLeft(_ + _)
        }
      }
    }
  }

  new Task("Task 8") {
    def solution() = {

      println(BinaryTree.leafSum(Node(_ + _, Node(_ * _, Leaf(3), Leaf(8)), Leaf(2), Node(_ + _, Leaf(5)))))

    }

    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(f: (Int, Int) => Int, branches: BinaryTree*) extends BinaryTree

    object BinaryTree {
      def leafSum(bTree: BinaryTree): Int = {
        bTree match {
          case l: Leaf => l.value
          case n: Node => n.branches.map(leafSum(_)).reduceLeft(n.f(_, _))
        }
      }
    }
  }

  new Task("Task 9") {
    def solution() = {
      println(sum(List(Option(1), Option(2), Option.empty, Option(4), None)))
    }

    def sum(list: List[Option[Int]]): Int = {
      if (list.isEmpty)
        0
      else if (list(0).isEmpty)
        sum(list.tail)
      else
        list(0).get + sum(list.tail)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def f(x: Double) = if (x >= 0) Some(Math.sqrt(x)) else None
      def g(x: Double) = if (x != 1) Some(1 / (x - 1)) else None
      val h = compose(f, g)
      
      println(h(0))
      println(h(1))
      println(h(2))
      
    }

    def compose(f: Double => Option[Double], g: Double => Option[Double]) = {
      (d: Double) =>
        if (f(d) == None || g(d) == None) None
        else f(g(d).get)
    }
  }

}