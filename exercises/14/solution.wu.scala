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
      println(swap((1,2)))

    }
    
    def swap(a: Any) = {
      a match {
        case (x: Int, y: Int) => (y, x)
        case _ => "no match"
      }
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      val result = swap(Array(1,2,3,4))
      result match {
        case x : Array[Any] => println(x.mkString(" "))
        case _ => println("no match")
      }

    }
    
    def swap(a: Array[Any]) = {
      a match {
        case Array(x, y, rest @ _*) => Array(y, x, rest)
        case _ => "no match"
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val item = Bundle("something", 20, Article("Thinking in scala", 19.99),
          Multiple(5, Bundle("sport", 10, Article("Basketball", 40), Article("Football", 50))))
      println(price(item))

    }
    
    abstract class Item
    case class Article(description: String, price: Double) extends Item
    case class Bundle(description: String, discount: Double, items: Item*) extends Item
    case class Multiple(amount: Int, item: Item) extends Item
    
    def price(it: Item): Double = it match {
      case Article(_, p) => p
      case Bundle(_, disc, its @ _*) => its.map(price _).sum - disc
      case Multiple(a, item) => (price(item)) * a
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      val tree = Node(Node(Leaf(5), Node(Leaf(1),Leaf(6))), Node(Leaf(2), Node(Leaf(3), Leaf(4))))
      println(sumOfLeaves(tree))

    }
    
    sealed abstract class BinaryTree
    case class Leaf(value: Int) extends BinaryTree
    case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
    
    def sumOfLeaves(t: BinaryTree): Int = t match {
      case Leaf(v) => v
      case Node(l, r) => sumOfLeaves(l) + sumOfLeaves(r) 
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here
      val lst = List( Some(2), None, Some(3), None, Some(1) )
      println(sumOfOption(lst))

    }
    
    def sumOfOption(lst: List[Option[Int]]) = {
      lst.filter(_ != None).map(_.get).sum
    }
    
  }


}
