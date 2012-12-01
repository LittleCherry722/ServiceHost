object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //  Tasks.execute("Task 9");
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

      // TODO your solution for task 1 here

    }
  }

  new Task("Task 2") {
    def solution() = {
      def swap(p: Any): (Int, Int) = p match {
        case (a: Int, b: Int) => (b, a)
      }

      val p = (6, 3)
      println("swap(%s) = %s".format(p, swap(p)))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swap(p: Array[Int]): Array[Int] = p match {
        case (Array(first, second, rest @ _*)) => Array(second, first) ++ rest
      }

      val a = Array[Int](2, 6, 4, 2)
      println("swap([%s]) = [%s]"
        .format(a.mkString(", "), swap(a).mkString(", ")))
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discout: Double, items: Item*)
        extends Item
      case class Multiple(count: Int, product: Item)
        extends Item

      def price(it: Item): Double = it match {
        case Article(_, p)             => p
        case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
        case Multiple(count, item)     => count * price(item)
      }

      val mul = Multiple(10, Article("Backwell Toaster", 29.95))

      println("price of " + mul + ":")
      println(price(mul))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def leafSum(lst: List[Any]): Int = lst match {
        case num :: tail =>
          num match {
            case head :: tail2 => leafSum(head :: tail2) + leafSum(tail)
            case Nil           => 0
            case _             => num.asInstanceOf[Int] + leafSum(tail)
          }
        case Nil => 0
      }

      val bspList = List(List(3, 8), 2, List(5))
      println(bspList)
      println("Leafsum:")
      println(leafSum(bspList))
    }
  }

  new Task("Task 6") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

      def treeSum(tree: BinaryTree): Int = tree match {
        case Leaf(v)    => v
        case Node(l, r) => treeSum(l) + treeSum(r)
      }

      val bspTree = Node(Node(Leaf(3), Leaf(8)), Node(Leaf(2), Leaf(5)))

      println(bspTree)
      println("Sum: " + treeSum(bspTree))
    }
  }

  new Task("Task 7") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(childs: BinaryTree*) extends BinaryTree

      def treeSum(tree: BinaryTree): Int = tree match {
        case Leaf(value)       => value
        case Node(childs @ _*) => childs.foldLeft[Int](0)(_ + treeSum(_))
      }

      val bsp = Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))
      println(bsp)
      println("Sum: " + treeSum(bsp))

    }
  }

  new Task("Task 8") {
    def solution() = {
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(operator: Char, childs: BinaryTree*) extends BinaryTree

      def treeCalc(tree: BinaryTree): Int = tree match {
        case Leaf(value)            => value
        case Node('+', childs @ _*) => childs.foldLeft[Int](0)(_ + treeCalc(_))
        case Node('*', childs @ _*) => childs.foldLeft[Int](1)(_ * treeCalc(_))
        case Node('-', childs @ _*) => childs.foldLeft[Int](0)(_ - treeCalc(_))
      }

      val bsp = Node('+',
        Node('*', Leaf(3), Leaf(8)),
        Leaf(2),
        Node('-', Leaf(5)))

      println(bsp)
      println("Calculate: " + treeCalc(bsp))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def listSum(lst: List[Option[Int]]) =
        lst.filter(_ != None).foldLeft[Int](0)(_ + _.get)

      val lst: List[Option[Int]] =
        List(Some(2), None, None, Some(1), Some(15), Some(3))
      println("Sum: " + lst)
      println(listSum(lst))
    }
  }

  new Task("Task 10") {
    def solution() = {
      def compose(f: Double => Option[Double], g: Double => Option[Double]) =
        (x: Double) =>
          if (f(x) == None || g(x) == None) None
          else Some(1)
          
      import Math.sqrt
      def f(x: Double) = if (x > 0) Some(sqrt(x))
      else None
      def g(x: Double) = if (x != 1) Some(1 / (x - 1))
      else None

      val h = compose(f, g)

      println("h(2) = %s".format(h(2)))
      println("h(1) = %s".format(h(1)))
      println("h(0) = %s".format(h(0)))

    }
  }

}