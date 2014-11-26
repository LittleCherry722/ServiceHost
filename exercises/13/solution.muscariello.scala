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
      import scala.collection.mutable
      def fun1(s: String): mutable.Map[Char, mutable.Set[Int]] = {
        val m = mutable.Map[Char, mutable.Set[Int]]()
        for ((c, i) <- s.zipWithIndex) {
          m(c) = m.getOrElse(c, mutable.Set[Int]()) + i
        }
        m
      }
      println("there is no mutable SortedSet for scala, so you have to use Java's TreeSet in order to have it sorted")
    }
  }

  new Task("Task 2") {
    def solution() = {
      def fun2(s: String): Map[Char, List[Int]] = {
        (Map[Char, List[Int]]() /: s.zipWithIndex) {
          case (m, (c1, c2)) => m + (c1 -> (m.getOrElse(c1, List()) :+ c2))
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      def fun3(l: scala.collection.mutable.LinkedList[Int]): scala.collection.mutable.LinkedList[Int] = {
        return l.filter(_ != 0)
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      def fun4(a: Array[String], m: Map[String, Int]): Array[Int] = {
        a.flatMap(m.get(_))
      }
      val y = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      val x = Array("Tom", "Fred", "Harry")
      fun4(x, y)
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      println("""lst :\ List[Int]())(_ :: _):""")
      println("Since List[Int]() is empty, there are no elements to iterate over. Hence, lst is returned.")
      println("""(List[Int]() /: lst)(_ :+ _):""")
      println("Each element of lst is appended to the empty list, the result is lst.")
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      (prices zip quantities) map { Function.tupled { _ * _ } }
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

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
