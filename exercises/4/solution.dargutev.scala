import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap

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
    	val gizmos:Map[String,Int]=Map[String,Int]("item1"->10,"item2"->100,"item3"->1000);
    	println(discount(gizmos));

    }
  }
  def discount(map:Map[String,Int]) : Map[String,Double] ={
    for((k,v) <- map) yield (k,0.9*v)
  }

  new Task("Task 6") {
    def solution() = {
      val days=scala.collection.mutable.LinkedHashMap("Monday"->Calendar.MONDAY,"Tuesday"->Calendar.TUESDAY,"Wednesday"->Calendar.WEDNESDAY,"Thursday"->Calendar.THURSDAY,"Friday"->Calendar.FRIDAY,"Saturday"->Calendar.SATURDAY,"Sunday"->Calendar.SUNDAY);
      for((k,v)<-days) println(k)
      
    }
  }

  new Task("Task 7") {
    def solution() = {
      val props: scala.collection.Map[String, String] = System.getProperties()
      val maxLength=props.maxBy(_._1.length())
      println("longest property: "+maxLength._1)
      for((k,v)<-props) println(k+"\t"+"| "+v)
    		  
    }
  }

  new Task("Task 8") {
    def solution() = {
    	println(minmax(Array(4,2,6,8,2,65,8,1,56,4,2,66,3,77)));

    }
  }
  def minmax(values:Array[Int]):(Int,Int)= {
    (values.min,values.max)
  }
  new Task("Task 9") {
    def solution() = {
    	println(lteqgt(Array(4,2,6,8,2,65,8,1,56,4,2,66,3,77),8))

    }
  }
  def lteqgt(values:Array[Int], v:Int):(Int,Int,Int) = {
    val ltv=values.filter(_<v).length
    val gtv=values.filter(_>v).length
    val eqv=values.filter(_==v).length
    (ltv,eqv,gtv)
  }
  
   
}
