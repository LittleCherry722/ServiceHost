import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap
import java.awt.geom.Rectangle2D
import java.awt.Point

object Solution extends App {

  // execute all tasks
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

/* insert your solutions below */

object Tasks extends Tasks {

  def values(fun: (Int) => Int, low: Int, high: Int) = {
    for (i <- low until high + 1)
      yield (i, fun(i))
  }

  new Task("Task 1") {
    def solution() = {

      println(values(x => x * x, -5, 5))
    }
  }

  def max(a: Int, b: Int): Int = {
    if (a < b) b
    else a
  }
  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 2, 4, 2, 1, 1, 1, 1).reduceLeft(max(_, _))
      println(a)
    }
  }

  def fact(n: Int): Int = {
    if (n == 0 || n == 1) 1
    else (1 to n).reduceLeft(_ * _)
  }

  new Task("Task 3") {
    def solution() = {

      println(fact(4))
    }
  }

  def fact2(n: Int): Int = {
    (1 to n).foldLeft(1)(_ * _)
  }
  new Task("Task 4") {
    def solution() = {

      println(fact2(4))
    }
  }
  
  def largest(fun: (Int)=>Int, inputs:Seq[Int]):Int={
    inputs.map(fun(_)).reduceLeft(max(_,_))
  }
  
  new Task("Task 5") {
    def solution() = {

      println(largest(x=>10*x-x*x,1 to 10))
    }
  }
  
  def unless(cond:Boolean)(then: => Any)(els: => Any){
    if(!cond) then
    else els
  }
 new Task("Task 10") {
    def solution() = {
    	// first parameter doesnt need to be call by name. I use currying but dont necessarilly need to
      unless(5<10)(println(5))(println(10))
    }
  }
}
