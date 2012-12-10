
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import scala.collection.SortedSet
import scala.collection.mutable.LinkedList

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //  TaskManager.execute("Task 1");
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
      println(indices("Mississippi"))
    }

    def indices(s: String) = {
      val a = scala.collection.mutable.Map[Char, SortedSet[Int]]()
      for (i <- 0 until s.length)
        a.put(s.charAt(i), a.getOrElse(s.charAt(i), SortedSet[Int]()) + i)

      a

    }
  }

  new Task("Task 2") {
    def solution() = {

      println(indices("Mississippi"))
    }

    def indices(s: String) = {
      var a = scala.collection.immutable.Map[Char, ArrayBuffer[Int]]()
      for (i <- 0 until s.length) {
        a = a + (s.charAt(i) -> ((a.getOrElse(s.charAt(i), ArrayBuffer[Int]())) + i))
      }

      a

    }
  }

  new Task("Task 3") {
    def solution() = {
      println(killZeros(LinkedList(1, 7, 4, 0, 0, 1, 3, 0, 21)))
    }

    def killZeros(list: LinkedList[Int]) = {
      list.filter(_ != 0)
    }
  }

  new Task("Task 4") {
    def solution() = {
      val a = Array("Tom", "Fred", "Harry")
      val b = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)

      println(getValues(a, b).mkString(" "))
    }

    def getValues(keys: Array[String], map: Map[String, Int]) = {
      val a = keys.flatMap(s => Array(map.get(s))).filter(_ != None)
      val b = ArrayBuffer[Int]()
      for (k <- a)
        b += k.get

      b.toArray
    }
  }

  new Task("Task 5") {
    def solution() = {
    	println(mkString(List(1, "3", "test", 3.9), " | "))
    }
    
    def mkString[T](a: scala.collection.Traversable[T], s: String = "") = {
      a.map(_.toString()).reduceLeft(_ + s + _)
    }

  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here

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