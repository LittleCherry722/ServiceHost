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
      def indices(chars: String) = {
        val map = collection.mutable.Map[Char,scala.collection.mutable.ArrayBuffer[Int]]()
        for (i <- 0 until chars.length) {
          val char = chars(i)
          if (!map.contains(char)) map(char) = new collection.mutable.ArrayBuffer[Int]()
          else map(char) += i
        }
        map
      }
      println(indices("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def indices(chars: String) = {
        var map = collection.immutable.Map[Char,scala.collection.mutable.ArrayBuffer[Int]]()
        for (i <- 0 until chars.length) {
          val char = chars(i)
          if (!map.contains(char)) map = map + (char -> new collection.mutable.ArrayBuffer[Int]())
          else map(char) += i
        }
        map
      }
      println(indices("Mississippi"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def filterZeros(list: collection.mutable.LinkedList[Int]) = {
        list.filterNot(_ == 0)
      }
      println(filterZeros(collection.mutable.LinkedList(41,2,5,0,4)))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def thisIsSerious(list: Array[String], map: Map[String,Int]) = {
        def find(i: String) = if (map contains i) Some(map(i)) else None
        list flatMap find
      }

      println(thisIsSerious(Array("Tom", "Fred", "Harry") , Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List[Int](5 , 2, 24, 2)
      println((lst :\ List[Int]())(_ :: _))
      println((List[Int]() /: lst)(_ :+ _))
      // Stipid Scala-Compiler thinks "+:" is near enough at "+". So we need to make clear this is not the case by explicit writing
      println((List[Int]() /: lst)(_.+:(_)))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = Array[Int](1,3,4)
      val quantities = Array[Int](2,3,4)
      println((prices zip quantities) map ((_: Int) * (_: Int)).tupled)
    }
  }

}
