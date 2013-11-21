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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
    	def signum(num: Int) = {num / num.abs}
    	println(signum(-4))
    }
  }

  new Task("Task 2") {
    def solution() = {
      println({})
    }
  }
  
  new Task("Task 4") {
    def solution() = {
      for (i <- 10.to(0,-1)) println(i)
    }
  }

  new Task("Task 5") {
    def solution() = {
      def countdown(n: Int) = {
        for (i <- n.to(0,-1)) println(i)
      }
      countdown(4)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def exp(x: Int, n: Int): Int = {
        if (n % 2 == 0 && n > 0) {
          // using exp(exp(x,n/2),2) would lead to infinite recursion
          val tmp = exp(x,n/2)
          tmp * tmp
        } else if (n > 0) {
          x * exp(x,n-1)
        } else if (n == 0) {
          1
        } else {
          1 / exp(x,-n)
        }
      }
      println(exp(2,5))
    }
  }
}

