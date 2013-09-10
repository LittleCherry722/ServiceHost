import scala.collection.mutable.LinkedList
import scala.collection.mutable.Set
import scala.collection.mutable.SortedSet

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
  import scala.collection.mutable.Map
  new Task("Task 1") {
    def solution() = {
      println(indexes("Mississippi"))
    }

    def indexes(word: String): Map[Char, SortedSet[Int]] = {
      val result = Map[Char, SortedSet[Int]]()
      for (i <- word.view.zipWithIndex) {
        result.put(i._1, (result.getOrElse(i._1, SortedSet[Int]())) += i._2)
      }
      result
    }
  }

  new Task("Task 2") {
    import scala.collection.immutable.Map
    def solution() = {
      println(indexes("Mississippi"))
    }

    def indexes(word: String): Map[Char, SortedSet[Int]] = {
      var result = Map[Char, SortedSet[Int]]()

      for (i <- word.view.zipWithIndex) {
        result += (i._1 -> (result.getOrElse(i._1, SortedSet[Int]()) += i._2))
      }
      result
    }
  }

  new Task("Task 3") {
    def solution() = {
      val numbers = LinkedList(0, 4, 0, 5, 42, 1, 0, 5, 0, 2, 0)
      val numbers2 = LinkedList(0)
      val numbers3 = LinkedList(42)

      removeZeroes(numbers)
      println(numbers)
      removeZeroes(numbers2)
      println(numbers2)
      removeZeroes(numbers3)
      println(numbers3)

    }

    def removeZeroes(numbers: LinkedList[Int]): LinkedList[Int] = {
      var cur = numbers
      if (cur.length > 1) {
        if (cur.head == 0) {
          cur.elem = cur.next.head
          cur.next = cur.next.next
        }
        removeZeroes(cur.tail)
      } else if (cur.length == 1 && cur.head == 0) cur.next = cur
      cur
    }
  }

  new Task("Task 4") {
    def solution() = {
      val array = Array("Tom", "Fred", "Harry")
      val map = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
    }

    def correspondingValues(arr: Array[String], map: Map[String, Int]): Array[Int] = {
      arr.flatMap(map.get(_))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(4, 5, 42, 39, 1, 0)
      println("foldr operator for building up the list: " + (lst :\ List[Int]())(_ :: _))
      println("foldl operator for building up the list: " + (List[Int]() /: lst)(_ :+ _))
      println("foldr operator for reversing the list: " + (lst :\ List[Int]())((x, y) => y :+ x))
      println("foldl operator for reversing the list: " + (List[Int]() /: lst)((x, y) => y :: x))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)

      println(*(prices, quantities))
    }

    def *(doubles: List[Double], ints: List[Int]) = {
      ((doubles zip ints) map Function.tupled { _ * _ })
    }
  }

}
