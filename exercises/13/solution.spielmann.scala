import scala.collection._


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
      def indexes(s: String) = {
        var sWithIndexes = s zipWithIndex
        var initialMap = mutable.LinkedHashMap[Char, SortedSet[Int]]()
        for ((c,i) <- sWithIndexes) {
          var s = initialMap.getOrElse(c, SortedSet[Int]())
          s += i
          initialMap(c) = s
        }
        initialMap
      }
      print("indexes(\"Mississippi\") evaluates to " + indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def indexes(s: String) = {
        var sWithIndexes = s zipWithIndex
        var initialMap = scala.collection.immutable.Map[Char, List[Int]]()
        for ((c,i) <- sWithIndexes) {
          var s = initialMap.getOrElse(c, List[Int]())
          s = s :+ i
          var x = (c,s)
          initialMap += x 
        }
        initialMap
      }
      print("indexes(\"Mississippi\") evaluates to " + indexes("Mississippi"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def removeZeroes(l: mutable.LinkedList[Int]) = {
        l filter (_ != 0)
      }
      var l = mutable.LinkedList(1,34,0,4,0,213,41,2,4,0)
      println("list before: " + l)
      println("list after: " + removeZeroes(l))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def stringToInt(c: Traversable[String], m: Map[String, Int]) = {
        c.flatMap(x => m.get(x))
      }
      var a = Array("Tom", "Fred", "Harry")
      var m = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println("array: Array(\"Tom\", \"Fred\", \"Harry\")")
      println("map: " + m)
      println("result: " + stringToInt(a,m))
    }
  }

  new Task("Task 5") {
    def solution() = {
    }
  }

  new Task("Task 6") {
    def solution() = {
      var lst = List(1,34,0,4,0,213,41,2,4,0)
      println("lst = " + lst)
      // This will generate a list that is equal to lst.
      // The items of lst will be prepended to the the empty list from right to left
      println("(lst :\\ List[Int]())(_ :: _) = " + ((lst :\ List[Int]())(_ :: _)))
      
      // This will generate a list that is equal to lst.
      // The items of lst will be appended to the the empty list from left to right
      println("(List[Int]()/: lst)(_ :+ _) = " + ((List[Int]()/: lst)(_ :+ _)))
      
      println("reversing lists")
      println("((lst :\\ List[Int]())((a,b) => b :+ a)) = " + ((lst :\ List[Int]())((a,b) => b :+ a)))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      (prices zip quantities) map { ((a: Double,b: Int) => a*b).tupled(_) }
      println("val prices = List(5.0, 20.0, 9.95)")
      println("val quantities = List(10, 2, 1)")
      println("(prices zip quantities) map { ((a: Double,b: Int) => a*b).tupled(_) } = " + ((prices zip quantities) map { ((a: Double,b: Int) => a*b).tupled(_) }))

    }
  }

  new Task("Task 8") {
    def solution() = {
    }
  }

  new Task("Task 9") {
    def solution() = {
    }
  }

  new Task("Task 10") {
    def solution() = {
    }
  }

}
