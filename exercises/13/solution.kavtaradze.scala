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

      import java.util.TreeMap
      import scala.collection.mutable._
      import collection.JavaConversions._  
  
      def indexes(str: String) = {
    
    	  val map: TreeMap[Char, Set[Int]] = new TreeMap[Char, Set[Int]]
    			  for (i <- 0 until str.size) {
    				  if (map.contains(str(i))) {
    					  map(str(i)) += i
    				  }
    				  else {
    					  map(str(i)) = Set(i)
    				  }
    			  }
    
      	map
      }
  
      println(indexes("Mississippi"))

    }
  }

  new Task("Task 2") {
    def solution() = {

      def indexes(str: String) = {
    
    	  str.map(c => (c, (for (i <- 0 until str.size) yield {
    		  if (c == str(i)) {i} 
    		  else 
    			  None
    	  	}).toSet - None))
      }
  
      println(indexes("Mississippi"))

    }
  }

  new Task("Task 3") {
    def solution() = {

      import scala.collection.mutable.LinkedList
    
      val List = LinkedList(0, 1, 2, 0, 3, 4, 5, 0, 6, 7, 8, 0, 0, 9)
  
      println(List.filter { _ != 0 })

    }
  }

  new Task("Task 4") {
    def solution() = {

      def mapping(name: Array[String], value: Map[String, Int]) = {
    	  name.map { s => value.get(s) } flatMap { i => i }
      }
  
      val array = mapping(Array("Tom", "Fred", "Harry"),
    	Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))
    
      println(array.mkString(" "))

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

}
