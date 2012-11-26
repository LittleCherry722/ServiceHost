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
      println("TODO")
    }
  }

  new Task("Task 2") {
    def swap(p: (Int, Int)): (Int, Int) = p match {
      case (x, y) => (y, x)
    }

    def solution() = {
      val p1 = 4 -> 6
      val p2 = 1 -> 9
      println(p1 + " --> " + swap(p1))
      println(p2 + " --> " + swap(p2))
    }
  }

  new Task("Task 3") {
    def swap(a: Array[Int]): Array[Int] = a match {
      case Array() => Array()
      case Array(x) => Array(x)
      case Array(x, y, rest @ _*) => Array(y, x) ++ rest
    }

    def arrayToString(a: Array[Int]): String = {
      val aS = a.deep.mkString(", ")
      aS
    }

    def solution() = {
      val a1: Array[Int] = Array()
      val a2 = Array(3, 5, 8, 1, 0)
      val a3 = Array(6, 9)
      println(arrayToString(a1) + " --> " + arrayToString(swap(a1)))
      println(arrayToString(a2) + " --> " + arrayToString(swap(a2)))
      println(arrayToString(a3) + " --> " + arrayToString(swap(a3)))
    }
  }

  new Task("Task 4") {
    abstract class Item
    case class Article(description: String, price: Double) extends Item
    case class Bundle(description: String, discount: Double, items: Item*) extends Item
    case class Multiple(quantity: Int, item: Item) extends Item

    def price(it: Item): Double = it match {
      case Article(_, p) => p
      case Multiple(q, p) => q * price(p)
      case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
    }

    def solution() = {
      val product1 = Bundle("Father's day special", 20.0,
        Article("Scala for the Impatient", 39.95),
        Bundle("Anchor Distillery Sampler", 10.0,
          Article("Old Potrero Straight Rye Whiskey", 79.95),
          Article("Junípero Gin", 32.95)))

      println(price(product1))

      val product2 = Bundle("b1", 5.0,
        Article("a1", 5.90),
        Multiple(6, Article("a2", 1.0)),
        Multiple(3, Bundle("b2", 3.0,
          Article("a3", 8.0), Article("a4", 3.0))))

      println(price(product2))
    }
  }

  new Task("Task 5") {
    def leafSum(tree: List[Any]): Int = tree match {
      case Nil => 0
      case (x: Int) :: (ls: List[Any]) => x + leafSum(ls)
      case (l: List[Any]) :: (ls: List[Any]) => leafSum(l) + leafSum(ls)
    }

    def solution() = {
      val t1: List[Any] = List(List(3, 8), 2, List(5))
      println(leafSum(t1))
    }
  }

  new Task("Task 6") {
    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree

    def leafSum(tree: BinaryTree): Int = tree match {
      case Leaf(x) => x
      case Node(l, r) => leafSum(l) + leafSum(r)
    }

    def solution() = {
      val t1 = Node(Node(Leaf(3), Leaf(8)), Leaf(7))
      println(leafSum(t1))
    }
  }

  new Task("Task 7") {
    sealed abstract class Tree
    case class Leaf(value: Int) extends Tree
    case class Node(firstChild: Tree, children: Tree*) extends Tree

    def leafSum(tree: Tree): Int = tree match {
      case Leaf(x) => x
      case Node(c1, c @ _*) => leafSum(c1) + c.map(leafSum(_)).sum
    }

    def solution() = {
      val t1 = Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))
      val t2 = Node(Node(Leaf(9)), Leaf(3))
      println(leafSum(t1))
      println(leafSum(t2))
    }
  }

  new Task("Task 8") {
    sealed abstract class Tree
    case class Leaf(value: Int) extends Tree
    case class Node(operator: String, firstChild: Tree, restChildren: Tree*)
      extends Tree

    def eval(tree: Tree): Int = tree match {
      case Leaf(x) => x
      case Node("+", c1, c @ _*) => eval(c1) + c.map(eval(_)).sum
      case Node("-", c1, c @ _*) => -(eval(c1) + c.map(eval(_)).sum)
      case Node("x", c1, c @ _*) => eval(c1) * c.map(eval(_)).product
    }
    def solution() = {
      val t1 = Node("+", Node("x", Leaf(3), Leaf(8)), Leaf(2), Node("-", Leaf(5)))
      println(eval(t1))
    }
  }

  new Task("Task 9") {
    def sumNotNone(l: List[Option[Int]]): Int =
      l.flatten.sum

    def solution() = {
      val l = List(Some(4), None, Some(8))
      println(sumNotNone(l))
    }
  }

  new Task("Task 10") {
    
    def solution() = {

      // TODO

    }
  }

}
