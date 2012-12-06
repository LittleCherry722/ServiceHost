class CH13 {
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
	def execute (name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  
  new Task("Task 1"){
	  def solution() = {
	    def indexes(str: String): scala.collection.SortedMap[Char,Set[Int]]  = {
	      val index = str.zipWithIndex
	      var map = scala.collection.SortedMap[Char, Set[Int]]()
	      for(i <- 0 until index.length)
	        map += (index(i)._1 -> (map.getOrElse(index(i)._1, Set[Int]()) +  i))
	    map
	    }
	  		// your solution for task 1 here
	    
	  }
  }
  
  new Task("Task 2"){
	  def solution() = {
	    def indexes(str: String): scala.collection.immutable.Map[Char,List[Int]]  = {
	      val index = str.zipWithIndex
	      var map = scala.collection.immutable.Map[Char, List[Int]]()
	      for(i <- 0 until index.length)
	        map + (index(i)._1 -> (map.getOrElse(index(i)._1, List[Int]()).::(i)))
	      map
	    }
	  		// your solution for task 2 here
	    
	  }
  }
  
  new Task("Task 3"){
	  def solution() = {
	    def dropZero(list : scala.collection.mutable.LinkedList[Int]) =list.dropWhile(_ == 0)
	  		// your solution for task 3 here
	    
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {
		  def combine(strings: Array[String], map:  Map[String, Int]) =
		    map.keySet.intersect(strings.toSet).map(map.get(_).get )
		    // your solution for task 4 here
	    
	  }
  }
  
  new Task("Task 5"){
	  def solution() = {
	    def makeString(seq: Seq[Any], separator:String) = 
	      seq.map( _.toString ).reduceLeft( _ + separator + _ )
	  		// your solution for task 5 here
	    
	  }
  }
  
  new Task("Task 6"){
	  def solution() = {
	    val lst = List(1,2,3,4,5,6)
	    (lst :\ List[Int] ()){ (act,rev) => rev :+ act}
	  		// your solution for task 6 here
	    
	  }
  } 
   
  new Task("Task 7"){
	  def solution() = {

	  		// your solution for task 7 here
	    
	  }
  }
    
  new Task("Task 8"){
	  def solution() = {

	  		// your solution for task 8 here
	    
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

}