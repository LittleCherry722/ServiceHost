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
Write a loop that swaps adjacent elements of an array of integers. For example,
Array(1, 2, 3, 4, 5) becomes Array(2, 1, 4, 3, 5).
*/
for (i <- 0 until (a.lenght, 2) {
	val x: Int = a(i * 2)
	a(i * 2) = a(i * 2 + 1)
	a(i * 2 + 1) = x
} 
	    
	  }
  } 
   
  new Task("Task 2"){
	  def solution() = {
/*
Repeat the preceding assignment, but produce a new array with the swapped
values. Use for/yield.
*/
val result = for(elem <- a if a % 2 == 0) yield {
	val x: Int = elem
	elem = a(a.indexOf(elem) + 1)
	a(a.indexOf(elem) + 1) = x
}
	    
	  }
  }
    
  new Task("Task 3"){
	  def solution() = {
/*
Given an array of integers, produce a new array that contains all positive
values of the original array, in their original order, followed by all values that
are zero or negative, in their original order.
*/
//eingangsarray ist a
val b = ArrayBuffer[Int]()
for (i <- a){
	if (a(i) > 0) b += a(i)
}
for (i <- a){
	if (a(i) == 0) b += a(i)
}
for (i <- a){
	if (a(i) < 0) b += a(i)
}
	    
	  }
  }
    
  new Task("Task 4"){
	  def solution() = {
/*
Write a code snippet that produces all values from an array with duplicates
removed. (Hint: Look at Scaladoc.)
*/
// Das unsortierte Array sei a
a.distinct()
	    
	  }
  }
    
  new Task("Task 5"){
	  def solution() = {
/*
Rewrite the example at the end of Section 3.4, “Transforming Arrays,” on
page 34 using the drop method for dropping the index of the first match. Look
the method up in Scaladoc.
*/
val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
for (j <- indexes) a.drop(j)
	    
	  }
  }


  
}
