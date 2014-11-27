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
      // the exercise hardcodes implementation details, so its uglier than it needs to be…
      import scala.collection.mutable._
      def indexes(str: String) = {
        val idx = LinkedHashMap[Char, LinkedHashSet[Int]]();
        for ((c, i) <- str.zipWithIndex) {
          idx(c) = idx.getOrElse(c, LinkedHashSet[Int]()) + i;
        }
        idx
      }
      println(indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      // the exercise hardcodes implementation details, so its uglier than it needs to be…
      import scala.collection.immutable._
      def indexes(str: String) = {
        var idx = ListMap[Char, ListSet[Int]]();
        str.zipWithIndex.groupBy(_._1).foreach(
          x => {
            val set : ListSet[Int] = ListSet[Int]() ++ x._2.map(_._2).reverse
            idx += (x._1 -> set)
          }
        )
        idx
      }
      println(indexes("Mississippi"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      import scala.collection.mutable._
      val set = LinkedList(0,1,2,0,3,4,0,0,0,5)
      println(set.filterNot(_ == 0))
    }
  }

  new Task("Task 4") {
    def solution() = {
      import scala.collection.mutable._
      val a1 = Array("Tom", "Fred", "Harry")
      val m1 = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(a1.flatMap(m1.get).mkString("Array(", ", ", ")"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(1, 4, 7, 10)
      // Task: Write code you are unable to understand in 2 weeks:
      // Solution: Make sure to write :\ and /: instead of fold* \o/
      println((lst :\ List[Int]())(_ :: _))
      println((lst :\ List[Int]())((a,b) => b :+ a))
      println((List[Int]() /: lst)(_ :+ _))
      println((List[Int]() /: lst)((a,b) => b :: a))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      //println((prices zip quantities) map { p => p._1 * p._2 })
      println((prices zip quantities) map Function.tupled(_ * _))
    }
  }
}
