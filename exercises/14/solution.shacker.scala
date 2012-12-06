class CH14 {
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
        def swap(pair: Pair[Int, Int]) = {
          val result = pair match {
            case (x, y) => (y, x)
          }
          result
        }
        // your solution for task 2 here

      }
    }

    new Task("Task 3") {
      def solution() = {
        def swap(pair: Array[Int]) = {
          val result = pair.length match {
            case (0) => new Exception("to short, min 2 Elements")
            case (1) => new Exception("to short, min 2 Elements")
            case _ => Array(pair(1), pair(0))
          }
          result
        }
        // your solution for task 3 here

      }
    }

    new Task("Task 4") {
      def solution() = {
        abstract class Item(description: String, price: Double)
        case class Product(description: String, price: Double) extends Item
        case class Multiple(mult: Int, items: Item) extends Item {
          val listItems = List[Item]
          def price() = mult * items.price
        }
        case class Bundle(val description: String) extends Item {
          var items = List[Item]()
          def price: Double = {
            val prices = items.map(_.price)
            var total = prices.reduceLeft(_ + _)
            total
          }
          def add(item: Item) = {
            items = item :: items
          }
        }
        // your solution for task 4 here

      }
    }

    new Task("Task 5") {
      def solution() = {
        def leafSum(sum: Int, lst: List[Any]): Int = {
          lst match {
            case List() => sum
            case head :: tail =>
              head match {
                case a: Int => leafSum(a + sum, tail)
                case b: List[Any] => leafSum(sum, b ::: tail)
                case _ => sum
              }
            case _ => sum
          }
        }
        // your solution for task 5 here

      }
    }

    new Task("Task 6") {
      def solution() = {
        sealed abstract class BinaryTree
        case class Leaf(value: Int) extends BinaryTree
        case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
        def sumLeaves(total: Int, tree: BinaryTree): Int = {
          tree match {
            case leaf: Leaf => total + leaf.value
            case binaryNode: Node =>
              treeSumImpl(total, binaryNode.left) + treeSumImpl(total, binaryNode.right)
          }
          total
        }
        // your solution for task 6 here

      }
    }

    new Task("Task 7") {
      def solution() = {
        sealed abstract class Tree
        case class Leaf(value: Int) extends Tree
        case class Node(nodes: Tree*) extends Tree

        def leafSum(tree: Tree): Int = {
          tree match {
            case Leaf(x) => x
            case Node(children @ _*) => children.map(leafSum).sum
          }
        }
        // your solution for task 7 here
      }
    }

    new Task("Task 8") {
      def solution() = {
        sealed abstract class Tree
        case class Leaf(value: Int) extends Tree
        case class Node(op: (Int, Int) => Int, children: Tree*) extends Tree

        def eval(tree: Tree): Int = tree match {
          case Leaf(x) => x
          case Node(op, single) => op(0, eval(single))
          case Node(op, children @ _*) => children.map(eval).reduceLeft(op)
        }
        // your solution for task 8 here
      }
    }

    new Task("Task 9") {
      def solution() = {
        def sumOptions(options: List[Option[Int]]): Int = {
          for (i <- 0 until options.length)
            options(i).getOrElse(_ + _)
        }
        // your solution for task 9 here

      }
    }

    new Task("Task 10") {
      def solution() = {
        def compose(f: Double => Option[Double], g: Double => Option[Double],
          value: Double): Option[Double] = {
          f(g(value).getOrElse(return None))
        }
        // your solution for task 10 here
      }
    }

  }

}