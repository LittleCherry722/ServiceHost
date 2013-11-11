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
      def signum (x: Int): Int = {
        if(x < 0) -x else if(x == 0) 0 else x
      }

      println("signum(-2):" + signum(-2));
      println("signum(0):" + signum(0));
      println("signum(3):" + signum(3));
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("The value is () and its type is Unit");
    }
  }

  new Task("Task 4") {
    def solution() = {
      for(i <- (0 to 10).reverse) {
        println(i)
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      def countdown(n: Int) : Unit = {
        for(i <- (0 to n).reverse) {
          println(i)
        }
      }

      println("countdown 5: ")
      countdown(5)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def pow (x: Double, n: Int): Double = {
        if (n == 0) 1.0
        else if (n < 0) (1.0 / pow(x, -n))
        else if (n % 2 == 0) {val y = pow(x, n/2); y * y}
        else x * pow(x, n - 1)
      }

      println("1^1: " + pow(1,1));
      println("2^2: " + pow(2,2));
      println("2^3: " + pow(2,3));
      println("2^-3: " + pow(2,-3));
      println("2.5^2: " + pow(2.5,2));
    }
  }

}
