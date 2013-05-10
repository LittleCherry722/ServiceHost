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
/*
Set up a map of prices for a number of gizmos that you covet. Then produce
a second map with the same keys and the prices at a 10 percent discount.
*/
var prices = scala.collection.mutable.Map[Int, String]
var newPrices = prices
for((k, v) <- newPrices) k * 0.9
	    
	  }
  }

new Task("Task 2"){
	  def solution() = {
/*
Define a linked hash map that maps "Monday" to java.util.Calendar.MONDAY, and
similarly for the other weekdays. Demonstrate that the elements are visited
in insertion order.
*/
val days = scala.collection.mutable.HashMap(
	"Monday" -> java.util.Calendar.MONDAY,
	"Tuesday" -> java.util.Calendar.TUESDAY
	"Wednesday" -> java.util.Calendar.WEDNESDAY
	"Thursday" -> java.util.Calendar.THURSDAY
	"Friday" -> java.util.Calendar.FRIDAY
	"Saturday" -> java.util.Calendar.SATURDAY
	"Sunday" -> java.util.Calendar.SUNDAY
)
	    
	  }
  }

new Task("Task 3"){
	  def solution() = {
/*Print a table of all Java properties, like this:
java.runtime.name	|	Java(TM) SE Runtime Environment
sun.boot.library.path	|	/home/apps/jdk1.6.0_21/jre/lib/i386
java.vm.version		|	17.0-b16
java.vm.vendor		|	Sun Microsystems Inc.
java.vendor.url		|	http://java.sun.com/
path.separator		|	:
java.vm.name		|	Java HotSpot(TM) Server VM
You need to find the length of the longest key before you can print the table.
Es wird der Aufruf System.getProperties() gebraucht.
*/
//???
	    
	  }
  }

new Task("Task 4"){
	  def solution() = {
/*
Write a function minmax(values: Array[Int]) that returns a pair containing the
smallest and largest values in the array.
*/
def minmax(values: Array[Int]) =
{
	var x = (values(0), values(0))
	for (i <- 1 until values.length){
		x._1 = if (values(i) < x._1) values(i) else x._1
		x._2 = if (values(i) > x._2) values(i) else x._2
	}
}
	    
	  }
  }

new Task("Task 5"){
	  def solution() = {
/*
Write a function lteqgt(values: Array[Int], v: Int) that returns a triple containing
the counts of values less than v, equal to v, and greater than v.
*/
def lteqgt(values: Array[Int], v: Int) =
{
	var x = (0, 0, 0)
	for(i <- 0 until values.length){
		x._1 = if (values(i) < v) x._1 + 1 else x._1
		x._2 = if (values(i) == v) x._2 + 1 else x._2
		x._3 = if (values(i) > v) x._3 + 1 else x._3
	}
}
	    
	  }
  }
  
}
