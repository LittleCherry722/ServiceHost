import scala.collection.mutable.ArrayBuffer

object Solution extends App {

  // execute all tasks
  Tasks.execute();
 
  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name+":");
    solution();
    println("\n");
  }
}

class Tasks {
	private var tasks = Seq[Task]();
	def add (t: Task) = { tasks :+= t }
	def execute () = { tasks.foreach( (t:Task) => { t.execute(); } )  }
	def execute (name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  
  new Task("Task 1"){
	  def solution() = {
	    
	  		// your solution for task 1 here
	    
	  }
  }
  
  new Task("Task 2"){
    def solution() = {
      var a = Array(1,2,3,4,5)
      print("Array before: ")
      a.map(x => print(x + " "))
      println()
      
      for (i <- 0 until (a.length -1 , 2)) {
        var tmp = a(i + 1)
        a(i + 1) = a(i)
        a(i) = tmp
      }
      
      print("Array after:  ")
      a.map(x => print(x + " "))
      println()
    }
  }
  
  new Task("Task 3"){
    def solution() = {
      var a = Array(1,2,3,4,5)
      print("Array before: ")
      a.map(x => print(x + " "))
      println()
      
      var b = for (i <- 0 until a.length) yield if (i == a.length - 1) a(i) else { if (i % 2 == 0) a(i+1) else a(i-1) }
      
      print("Array after:  ")
      b.map(x => print(x + " "))
      println()
    }
  }
  
  new Task("Task 4"){
    def solution() = {
      var a = Array(1,2,3,-4,5,0)
      print("Array before: ")
      a.map(x => print(x + " "))
      println()
      
      var b = a.filter(_>0) ++ a.filter(_<=0)
      
      print("Array after:  ")
      b.map(x => print(x + " "))
      println()
    }
  }
  
  new Task("Task 5"){
	  def solution() = {

	  		// your solution for task 5 here
	    
	  }
  }
  
  new Task("Task 6"){
	  def solution() = {

	  		// your solution for task 6 here
	    
	  }
  } 
   
  new Task("Task 7"){
    def solution() = {
      var a = Array(1,2,2,2,3,5,6,3,3,4,5)
      print("Array before: ")
      a.map(x => print(x + " "))
      println()
      
      var b = a.distinct
      
      print("Array after:  ")
      b.map(x => print(x + " "))
      println()
    }
  }
    
  new Task("Task 8"){
    def solution() = {
      var a = ArrayBuffer(6,1,2,2,2,-3,5,6,-3,3,-4,5)
      print("Array before: ")
      a.map(x => print(x + " "))
      println()
      
      val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
      for (i <- indexes.drop(1).reverse) a.remove(i)
      
      print("Array after:  ")
      a.map(x => print(x + " "))
      println()
    }
  }
    
  new Task("Task 9"){
	  def solution() = {

	  		// your solution for task 9 here
	    
	  }
  }
    
  new Task("Task 10"){
	  def solution() = {

	  		// your solution for task 10 here
	    
	  }
  }
  
}
