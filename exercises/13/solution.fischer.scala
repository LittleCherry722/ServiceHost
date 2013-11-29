
package chapter13

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length


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
      def indixesLinked (s: String): scala.collection.mutable.Map[Char, scala.collection.mutable.LinkedHashSet[Int]] = {
        val map = scala.collection.mutable.Map[Char, scala.collection.mutable.LinkedHashSet[Int]]()
        for (i <- 0 until s.length) yield map += (s(i) -> (map.getOrElse(s(i), scala.collection.mutable.LinkedHashSet(i)) += i))
        map
      }
      
      def indixes (s: String): scala.collection.mutable.Map[Char, scala.collection.mutable.Set[Int]] = {
        val map = scala.collection.mutable.Map[Char, scala.collection.mutable.Set[Int]]()
        for (i <- 0 until s.length) yield map += (s(i) -> (map.getOrElse(s(i), scala.collection.mutable.Set(i)) += i))
        map
      }
      println(indixes("Mississippi"))
      println("\nTo achieve a sorted order LinkedHashSet can be used: ")
      println(indixesLinked("Mississippi"))
    }
  }
  
  new Task("Task 2") {
    def solution() = {
      def indixes (s: String): scala.collection.immutable.Map[Char, List[Int]] = {
        var map = scala.collection.immutable.Map[Char, List[Int]]()
        for (i <- 0 until s.length) yield map += (s(i)) -> ( (map.getOrElse(s(i), Nil)) ::: (i :: Nil)) 
        map
      }
      
      println(indixes("Mississippi"))
    }
  }
  
  new Task("Task 3") {
    def solution() = {
      import scala.collection.mutable.LinkedList
     
      def removeZeros(l: LinkedList[Int]): LinkedList[Int] = l.filterNot(_ == 0)
      
      var list: LinkedList[Int] = LinkedList[Int](1, 0 , 1, 2, 3, 0, 4)
      println(list)
      println("Remove Zeros:")
      println(removeZeros(list))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def tast4 (s: Array[String], m: Map[String, Int]): Array[Int] = {
       s.flatMap(m.get(_))
      }
      val result = tast4(Array("Tom", "Fred", "Harry"), Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))
      println(result.mkString("Array(", ", ", ")"))
    }
    
  }
  new Task("Task 6") {
    def solution() = {
      val lst = List(1, 2, 3, 4, 5)
     
      println( (lst :\ List[Int]())(_ :: _))
      println( (List[Int]() /: lst)(_ :+ _)) 
      println( (List[Int]() /: lst) ((a, b) => b :: a))    
    }
  }
  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      
      val result = prices zip quantities map { ((x: Double, y: Int) => x * y).tupled }
      
      println(result)
    }
  }
}
