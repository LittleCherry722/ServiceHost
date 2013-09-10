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
The signum of a number is 1 if the number is positive, –1 if it is negative, and
0 if it is zero. Write a function that computes this value.
*/
def signum(x: Int) = if (x > 0) 1 else { if (x < 0) -1 else 0} 
	    
	  }
  }
  
  new Task("Task 2"){
	  def solution() = {
/*
What is the value of an empty block expression {}? What is its type?
*/
// The Value of {} is (), its type is Unit
	    
	  }
  }
  
  new Task("Task 3"){
	  def solution() = {
/*
Write a Scala equivalent for the Java loop
for (int i = 10; i >= 0; i--) System.out.println(i);
*/
for (i <- 10 to 0 by -1) println(i)
	    
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {
/*
Write a procedure countdown(n: Int) that prints the numbers from n to 0.
*/
def countdown(n: Int) { 
	for (i <- n to 0 by -1) println(i)
}
	    
	  }
  }
  
  new Task("Task 5"){
	  def solution() = {
/*
Write a function that computes x^n, where n is an integer. Use the following
recursive definition:
• x^n = y^2 if n is even and positive, where y = x^(n / 2).
• x^n = x· x^(n – 1) if n is odd and positive.
• x^0 = 1.
• x^n = 1 / x^(–n) if n is negative.
Don’t use a return statement.
*/
def function(x: Int, n: Int) = 
	if (n == 0) 1
	else { if ((n % 2 == 1) && (n > 0)) (x * function(x, (n - 1)))
		else { if (n < 0) function(x, (n * (-1))
			else { val y: Int = function(x, (n / 2));
				function(y, 2)
			}
		}
	}
	    
	  }
  }
}
