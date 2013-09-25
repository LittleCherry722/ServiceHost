import scala.util.logging.Logged

object Solution extends App {
	Tasks.execute();
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
	def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

object Tasks extends Tasks {
	new Task("Task 2") {
		def solution() = {
			def swap(two: (Int, Int)): (Int, Int) = {
				two match {
					case (a, b) => (b, a)
				}
			}
			println(swap(1, 2))
		}
	}

	new Task("Task 3") {
		def solution() = {
			def swap(arr: Array[Int]) = {
				arr match {
					case Array(a, b, c@_*) => Array(b, a) ++ c
				}
			}
			println(swap(Array(1, 2, 3, 4, 5)).mkString(", "))
		}
	}

	new Task("Task 6") {
		def solution() = {
			sealed abstract class BinaryTree
			case class Leaf(value: Int) extends BinaryTree
			case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
			
			def treeSum(tree: BinaryTree): Int = {
				tree match {
					case Leaf(value) => value
					case Node(l, r) => treeSum(l) + treeSum(r)
				}
			}
			
			println(treeSum(Node(Node(Leaf(1), Leaf(2)), Leaf(3))))
		}
	}

	new Task("Task 9") {
		def solution() = {
			def notNoneSum(l: List[Option[Int]]): Int = l.flatten.sum
			
			println(notNoneSum(List(None, Some(1), None, Some(2))))
		}
	}
}