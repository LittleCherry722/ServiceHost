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

  new Task("Task 1") {
    def solution() = {
    // (3 + 4) -> 5
    // (3 -> 4 ) + 5
    }
  }
  
  
class Money(val dollars:Int, val cents:Int){
  def +(m:Money):Money={
    new Money(dollars+m.dollars+((cents+m.cents)/100),(m.cents+cents)%100)
  }
  // same with the rest operators, but with different computations... 
}

  new Task("Task 4") {
    def solution() = {
     val m=new Money(1,75)
     val n=new Money(0,80)
     val p=n+m
     println(p.dollars+","+p.cents)
    }
  }
  
class BitSequence(bits:Array[Byte]){
  // skipped the calculations part again... the point is to learn what update and apply are for
   def update(pos:Int, value:Byte){
    bits(pos)=value
  }
  def apply(pos:Int) = bits(pos)
  
}

  new Task("Task 7") {
    def solution() = {
      val b=new BitSequence(Array(1,23,3,4))
      val s=b(2)
      println("demonstrate apply: " +s)
      b(2)=50
      println("demonstrate update: " +b(2))
    }
  }

}
