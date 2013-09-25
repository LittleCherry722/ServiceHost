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
      def signum(n : Int) : Int = {
        if (n > 0) 1 else (if (n == 0) 0 else -1)
      }
      ((-2) to 2).map(x => ("signum of " + x + " is "+ signum(x))).map(println)
    }
  }
  
  new Task("Task 2"){
    def solution() = {
      // The type of {} is Unit with value ()
      println("The type of {} is Unit with value ()")
    }
  }
  
  new Task("Task 3"){
	  def solution() = {

	  		// your solution for task 3 here
	    
	  }
  }
  
  new Task("Task 4"){
    def solution() = {
      for(i <- 10.to(0, -1)) { println(i) }
    }
  }
  
  new Task("Task 5"){
    def solution() = {
      def countdown(n : Int) : Unit = {
        if (n < 0)
          throw new IllegalArgumentException("Number has to be greater than 0")
        else
          print(n)
          if (n > 0) { countdown(n - 1) }
      }
      println("Countdown from 5:")
      countdown(5)
    }
  }
  
  new Task("Task 6"){
	  def solution() = {

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
      def sqr(x : Double) : Double = {
        x * x
      }
      def pow(x : Double, n : Int) : Double = {
        if (n == 0) 1
        else {
          if (n > 0) {
            if (n % 2 == 0)
              sqr(pow(x, n / 2))
            else
              x * pow(x, n - 1)
          }
          else
            1 / pow(x, -n)
        }
      }
    }
  }
  
}
