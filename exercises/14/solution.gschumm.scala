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
    def swap(pair: (Int, Int)) = pair match {
      case (x, y) => (y, x)
    }

    def solution() = {
      println(swap((1, 2)))
    }
  }

  new Task("Task 3") {
    def swap(arr: Array[Int]) = arr match {
      case Array(first, second, rest @ _*) => Array(second, first) ++ rest
    }

    def solution() = {
      println(swap(Array(1, 2, 3, 4)).mkString)
    }
  }

  new Task("Task 4") {
    abstract class Item
    case class Article(description: String, price: Double) extends Item
    case class Bundle(description: String, discount: Double, items: Item*) extends Item

    case class Multiple(count: Int, item: Item) extends Item

    def price(it: Item): Double = it match {
      case Article(_, p) => p
      case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
      case Multiple(count, item) => count * price(item)
    }

    def solution() = {
      val p = Bundle("Father's day special", 20.0,
        Multiple(3, Article("Scala for the Impatient", 39.95)),
        Multiple(10, Bundle("Anchor Distillery Sampler", 10.0,
          Multiple(2, Article("Old Potrero Straight Rye Whiskey", 79.95)),
          Article("Junipero Gin", 32.95))))

      println(price(p))
    }
  }

  new Task("Task 5") {
    case class Single(x: Int)

    def leafSum(lst: List[Any]): Int = lst match {
      case Nil => 0
      case (x: Int) :: rest => x + leafSum(rest)
      case (x: List[Any]) :: rest => leafSum(x) + leafSum(rest)
    }

    def solution() = {
      println(leafSum(List(List(3, 8), 2, List(5))))
    }
  }

  new Task("Task 6") {
    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

    def leafSum(tree: BinaryTree): Int = tree match {
      case Leaf(x) => x
      case Node(left, right) => leafSum(left) + leafSum(right)
    }

    def solution() = {
      println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2))))
    }
  }

  new Task("Task 7") {
    sealed abstract class Tree
    case class Leaf(value: Int) extends Tree
    case class Node(children: Tree*) extends Tree

    def leafSum(tree: Tree): Int = tree match {
      case Leaf(x) => x
      case Node(children @ _*) => children.map(leafSum).sum
    }

    def solution() = {
      println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))))
    }
  }

  new Task("Task 8") {
    sealed abstract class Tree
    case class Leaf(value: Int) extends Tree
    case class Node(op: (Int, Int) => Int, children: Tree*) extends Tree

    def eval(tree: Tree): Int = tree match {
      case Leaf(x) => x
      case Node(op, single) => op(0, eval(single))
      case Node(op, children @ _*) => children.map(eval).reduceLeft(op)
    }

    def solution() = {
      println(eval(Node(_ + _, Node(_ * _, Leaf(3), Leaf(8)), Leaf(2), Node(_ - _, Leaf(5)))))
    }
  }

  new Task("Task 9") {
    def sum(lst: List[Option[Int]]) = lst.map(_.getOrElse(0)).sum

    def solution() = {
      println(sum(List(None, Some(2), Some(3), None, Some(5))))
    }
  }

  new Task("Task 10") {
    def compose(f: Double => Option[Double], g: Double => Option[Double]) = {
      (x: Double) =>
        f(x) match {
          case None => None
          case Some(y) => g(y)
        }
    }
    def solution() = {
      def f(x: Double) = if (x >= 0) Some(math.sqrt(x)) else None
      def g(x: Double) = if (x != 1) Some(1 / (x - 1)) else None
      val h = compose(f, g)
      println(h(2), h(1), h(0))
    }
  }

}
