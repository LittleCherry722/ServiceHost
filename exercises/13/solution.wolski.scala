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
    // TODO: should use Set instead of ArrayBuffer, but mutable.Set is not present in Scala 2.9.3
    def indexes(str: String) = {
      import scala.collection.mutable.{HashMap, ArrayBuffer}
      val map = new HashMap[Char, ArrayBuffer[Int]]

      for (i <- 0 until str.length) {
        val c = str(i)
        if (!map.contains(c)) { map(c) = new ArrayBuffer[Int] }
        map(c) += i
      }

      map
    }
    def solution() = {

      println("indexes('Mississippi'): " + indexes("Mississippi"))

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here

    }
  }

  new Task("Task 3") {
    def removeZeros(list: scala.collection.mutable.LinkedList[Int]) = {
      list.filter(_ >= 0)
    }
    def solution() = {
      var l = scala.collection.mutable.LinkedList(1, 2, -2, 4, -6)
      println("l: "+l)
      l = removeZeros(l)
      println("l: "+l)
    }
  }

  new Task("Task 4") {
    def applyMap(keys: Collection[String], values: Map[String,Int]) = {
      def fetch(k: String) = {
        if (values.contains(k)) Some(values(k)) else None
      }
      keys.flatMap(fetch(_))
    }
    def solution() = {
      val names = Array("Tom", "Fred", "Harry")
      val values = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println("applyMap: "+applyMap(names, values))
    }
  }

  new Task("Task 5") {
    def myString[A](col: scala.collection.Iterable[A]): String = myString(col, "")
    def myString[A](col: scala.collection.Iterable[A], sep: String): String = myString(col, "", sep, "")
    def myString[A](col: scala.collection.Iterable[A], start: String, sep: String, end: String): String = {
      val strs = col.map(_.toString)
      (start + strs.reduceLeft(_ + sep + _) + end)
    }
    def solution() = {
      val l = scala.collection.mutable.LinkedList(-1,0,1,2)
      println("l: "+myString(l))
      println("l: "+myString(l, "<< \"", "\" -- \"", "\" >>"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      println("both does the same: they contruct a identical new list");
      println("one could modify the first to: (lst :\ List[Int]())((a: Int, b: List[Int]) => b :+ a)")
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }
}
