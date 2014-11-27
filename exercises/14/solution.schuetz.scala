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

  new Task("Task 1") {
    def solution() = {

      // your solution for task 1 here

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      def swap(pair: (Int,Int)) = {
        pair match {
          case (a, b) => (b, a)
        }
      }

      val pair = (1,2)
      println (pair)
      println (swap(pair))
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      def swap(array: Array[Int]): Array[Int] = {
        array match {
          case Array (a, b) => Array (b, a)
          case Array (a, b, rest @ _*) => Array (b, a) ++ rest
          case _ => array
        }
      }
      
      var array = Array(1,2,3,4,5)
      println (array.mkString(","))
      println (swap(array).mkString(","))
      array = Array(1,2)
      println (array.mkString(","))
      println (swap(array).mkString(","))
      array = Array(1)
      println (array.mkString(","))
      println (swap(array).mkString(","))

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      abstract class Item
      case class Multiple(amount: Int, item: Item) extends Item
      case class Article(description: String, price: Double) extends Item
      case class Bundle(description: String, discount: Double, items: Item*) extends Item
      
      def price(item: Item): Double = {
        item match {
	      	case Multiple(amount, item) => amount * price(item)
	        case Article(_, price) => price
	        case Bundle(_, discount, items) => discount * price(items)
        }
        
      }

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      sealed abstract class BinaryTree
      case class Leaf(value: Int) extends BinaryTree
      case class Node(Left: BinaryTree, Right: BinaryTree) extends BinaryTree
      def leafSum(tree: BinaryTree): Int = {
        tree match {
          case Leaf(value) =>  value
          case Node(left, right) => leafSum(left) + leafSum(right)
        }
      }

    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here
      def sum (lst: List[Option[Int]]) = {
        var sum = 0
        for (elem <- lst if (elem != None)) yield (sum += elem.get)
      }

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
