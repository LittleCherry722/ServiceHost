
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
      def indexes(input: String) = {
        var result = scala.collection.mutable.Map[Char, scala.collection.mutable.SortedSet[Int]]()
        for ((c,i) <- input.zipWithIndex) { 
          result(c) = result.getOrElse(c, scala.collection.mutable.SortedSet[Int]()) + i
        }
        result
      }

      println(indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      def indexes(input: String) = {
        import scala.collection.mutable._
        var result = Map[Char,SortedSet[Int]]()
        (Map[Char, SortedSet[Int]]() /: input.zipWithIndex) {
          case (m, (c,i)) => m  + (c -> (m.getOrElse(c, SortedSet[Int]()) + i))
        }
      }
      println(indexes("Mississippi"))

    }
  }

  new Task("Task 3") {
    def solution() = {
      import scala.collection.mutable.LinkedList

      // your solution for task 3 here
      def remove0s(list: LinkedList[Int]) = {
        list.filter(_ != 0)
      }

      val list = LinkedList(0,1,2,0,4,5,0,0,0,7)
      println(remove0s(list))
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      def mapping(strings: Array[String], map: Map[String, Int]): Array[Int] = {
        strings.flatMap(map.get(_))
      }
      val strings = Array("Tom", "Fred", "Harry")
      val map = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(mapping(strings, map))
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
      val lst = List[Int](1,2,3,4)
      println ((lst :\ List[Int]()) (_ :: _))
      //lst is returned because nothing is appended
      println((List[Int]() /: lst.reverse)(_ :+ _))
      // a new list is returned with the same elements as lst

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

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
