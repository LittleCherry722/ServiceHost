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

      // your solution for task 1 here
      def signum(x: Int) = if (x > 0) 1 else if (x < 0) -1 else 0
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      println("Type: Unit")
      println("Value: ()")

    }
  }


  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      def countdown(n: Int) = {
	    for(i <- 0 to n){
		  println(n-i)
		}
	  }
    }
  }



  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      def pow(x: Int, n: Int) = {
        
        if (n == 0){
          1
        }
        else if (n < 0){
          1 / pow(x, -n)
        }
        else {
          x * pow(x, n-1)
        }
      }
  }

}
