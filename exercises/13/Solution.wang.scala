package test
import scala.collection.mutable.LinkedList
object Solution extends App {
  Tasks.execute()
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

object Tasks extends Tasks {
  new Task("chapter13_1") {
    def solution() = {     
      def indexes(s: String): scala.collection.mutable.Map[Char, scala.collection.mutable.SortedSet[Int]] = {
        var position = scala.collection.mutable.Map[Char, scala.collection.mutable.SortedSet[Int]]()
        for (i <- 0 until s.length()) {
          if (position.contains(s(i))) position(s(i)) += i
          else position += (s(i) -> scala.collection.mutable.SortedSet(i))
        }
        position
      }
      print(indexes("Mississippi"))
    }
  }

  new Task("chapter13_2") {
    def solution() = {
      def indexes(s: String): scala.collection.immutable.Map[Char, scala.collection.mutable.ListBuffer[Int]] = {
        var position = scala.collection.immutable.Map[Char, scala.collection.mutable.ListBuffer[Int]]()
        for (i <- 0 until s.length()) {
          if (position.contains(s(i))) position(s(i)).+= (i)
          else position += (s(i) -> scala.collection.mutable.ListBuffer(i))
        }
        position
      }
      print(indexes("Mississippi"))
    }
  }
  
  new Task("chapter13_3") {
    def solution() = {
      def nonZero(num: List[Int]) : List[Int]= {
        num.filter(_ != 0)      
      }      
      print(nonZero(List(1,0,0,3,0,0,0,0,2)))
    }
  }
  
  new Task("chapter13_4") {
    def solution() = {     
      def coll (str: Array[String], map: Map[String, Int]) : Array[Int] = {
        str.flatMap(map.get(_))
      }
     
      val s = Array("Tom", "Fred","Harry")
      val map = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5, "Fred" -> 6)    
      print(coll(s, map).mkString(","))
    }
  }
  
  new Task("chapter13_6") {
    def solution() = {
      val integersList = List(1,2,4,5,6)     
      println((integersList :\ List[Int]())(_ :: _))     
      println((List[Int]() /: integersList )((a,b) => b :: a))
    }
  }
  
  new Task("chapter13_7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10,2,1)    
      println((prices zip quantities) map (Function.tupled(_ * _)))
    }
  }
}