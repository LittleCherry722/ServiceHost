import scala.collection.mutable._
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
      val test = indexes("Mississippi")
      for ((k, v) <- test) {
        println(k + ": " + v.toString)
      }

    }

    def indexes(str: String): Map[Char, SortedSet[Int]] = {

      val m = Map[Char, SortedSet[Int]]()

      for (i <- 0 until str.length) {
        m(str(i)) = m.getOrElse(str(i), SortedSet(i)) + i
      }
      
      m

    }

  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val test = indexes("Mississippi")
      for ((k, v) <- test) {
        println(k + ": " + v.toString)
      }

    }

    def indexes(str: String): collection.immutable.Map[Char, List[Int]] = {

      var m = collection.immutable.Map[Char, List[Int]]()

      for (i <- 0 until str.length) {
        m += (str(i) -> (m.getOrElse(str(i), List()) :+ i))
      }

      m

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      println(deleteZeros(LinkedList(1, 0, 2, 0, -5, 0, 9)).toString)

    }

    def deleteZeros(lst: LinkedList[Int]) = {
      lst.filter(_ != 0)
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      println(toIntegers(Array("Who","cares"), Map("Who" -> 1, "cares" -> 2)).mkString(" "))

    }
    
    def toIntegers(seq: Iterable[String], map: Map[String, Int]) = {
      seq.flatMap(map.get(_))
    }
  }


  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      val lst = List(1,2,3)
      println( (lst :\ List[Int]())( (x,y) => y :+ x).toString)

    }
    
    
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      val prices = Array(20,10,5.5)
      val quantities = Array(2,3,6)
      
      val result = (prices zip quantities) map { mul.tupled }
      println( result.mkString(" ") )

    }
    
    val mul = (x: Double, y: Int) => x * y
    
    
  }


}
