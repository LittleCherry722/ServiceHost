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



object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
    	def values(fun: (Int) => Int, low: Int, high: Int) = { (low to high) map(x => (x, fun(x)))}
      
    }
  }

  new Task("Task 2") {
    def solution() = {
    	val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
    	var max = a.reduceLeft((x, y) => if (x > y) x else y)      

    }
  }

  new Task("Task 3") {
    def solution() = {
    	def factorial(n: Int) = {
    		if(n < 1){

    			if (n == 0){
    				1
    			} else{
    			   /*???*/ 
    			}
    		} else {
    		  (1 to n) reduceLeft ((x, y) => x * y)
    		}
    	}

    }
  }

  new Task("Task 4") {
    def solution() = {
    	def factorial(n: Int) = {
    		require(n >= 0)
    		(1 to n).foldLeft(1)((x, y) => x * y)
    	}      

    }
  }

  new Task("Task 5") {
    def solution() = {
    	def largest(fun: (Int) => Int, inputs: Seq[Int]) = inputs.map(fun).max
      
    }
  }

  new Task("Task 10") {
    def solution() = {
    	//???

    }
  }

}