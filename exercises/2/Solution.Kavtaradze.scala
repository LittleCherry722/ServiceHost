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
    	def signum(x: Int) = { 
		   if(x<0) -1 else if(x==0) 0 else 1
           }
     
     
	   println("signum(-35):" + signum(-35))
	   println("signum(0):" + signum(0))
	   println("signum(35):" + signum(35))

    }
  }

  new Task("Task 2") {
    def solution() = {

    	print("Value is () and type is Unit")

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
    	for (i <- 10 until 0 by -1){
		   println(i)
	   }
    }
  }

  new Task("Task 5") {
    def solution() = {
    	def countdown(n : Int) = {for (i <- n until 0 by -1){
		   	println(i)
	   		}
	   	}
	  print("Input Number: ")
	  val a = readInt()
	  print(countdown(a))

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

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      def xtopowern(x: Double, n : Int): Double = {
	   def y = xtopowern(x,n/2)
	   if(n==0) 1
	   else if(n%2==0 && n>0) y*y
	   else if(n%2==1 && n>0) x * xtopowern(x, n-1)
	   else (1 / xtopowern(x, -n))
	   
	   }
	   	
	  
	  print(xtopowern(3,-1))


    }
  }

}
