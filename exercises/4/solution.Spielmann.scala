import java.util.Calendar._
import scala.collection.mutable._

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
      val gizmos = Map("Keyboard" -> 100.0, "monitor" -> 500.0, "mouse" -> 123.0)
      println("Map before:" + gizmos)
      var gizmosDiscounted = for((k,v) <- gizmos) yield (k, v * 0.9)
      println("Map after:" + gizmosDiscounted)
    }
  }
  
  new Task("Task 2"){
	  def solution() = {

	  		// your solution for task 2 here
	    
	  }
  }
  
  new Task("Task 3"){
	  def solution() = {

	  		// your solution for task 3 here
	    
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {

	  		// your solution for task 4 here
	    
	  }
  }
  
  new Task("Task 5"){
	  def solution() = {

	  		// your solution for task 5 here
	    
	  }
  }
  
  new Task("Task 6"){
    def solution() = {
      var m = LinkedHashMap(
      "Monday" -> MONDAY,
      "Tuesday" -> TUESDAY,
      "Wednesday" -> WEDNESDAY,
      "Thursday" -> THURSDAY,
      "Friday" -> FRIDAY,
      "Saturday" -> SATURDAY,
      "Sunday" -> SUNDAY)
      for (elem <- m) println(elem)
    }
  } 
   
  new Task("Task 7"){
    def solution() = {
      // Expand Strings with whitespaces to match the length-parameter
      def expandKeyString(key : String, length : Int) : String = {
        var whitespaces = Array.fill(length - key.length)(' ')
        key + new String(whitespaces)
      }
      
      // get properties
      var properties = System.getProperties()
      var keys = properties.keys()
      
      // build a map with keys and values of the properties
      var propertiesMap = new HashMap[String, String]()
      while (keys.hasMoreElements()) {
        var key = keys.nextElement().toString()
        propertiesMap.put(key, properties.getProperty(key))
      }
      
      // find the longes key in the propertiesMap
      var longestStringLength = propertiesMap.keys.foldLeft[String]("")((a,b) => if (a.length > b.length) a else b).length
      
      // expand every property-key so that all have the same length and print them
      for ((k,v) <- propertiesMap) {
        println(expandKeyString(k, longestStringLength) + " | " + v)
      }
    }
  }
    
  new Task("Task 8"){
    def solution() = {
      def minmax(values: Array[Int]) : Tuple2[Int, Int] = {
        if (values.length == 0)
          throw new IllegalArgumentException("The Array has to contain at least one element")
        else {
          var min = values(0)
          var max = values(0)
          for(elem <- values) {
            if (elem < min) min = elem
            else {
              if (elem > max) max = elem
            }
          }
          (min, max)
        }
      }
      var a = Array(41,123,1,5243,5243,1,2,3,4,5)
      print("minmax of ")
      a.map(x => print(x + " "))
      println(" = " + minmax(a))
    }
  }
    
  new Task("Task 9"){
    def solution() = {
      def lteqgt(values: Array[Int], v: Int) : Tuple3[Int, Int, Int] = {
        var lt = 0
        var eq = 0
        var gt = 0
        for(elem <- values) {
          if (elem < v) lt += 1
          else {
            if (elem > v) gt += 1
            else eq += 1
          }
        }
        (lt, eq, gt)
      }
      var a = Array(41,123,1,5243,5243,1,2,3,4,5)
      print("lteqgt of ")
      a.map(x => print(x + " "))
      println("with (v := 5) = " + lteqgt(a, 5))
    }
  }
    
  new Task("Task 10"){
	  def solution() = {

	  		// your solution for task 10 here
	    
	  }
  }
  
}
