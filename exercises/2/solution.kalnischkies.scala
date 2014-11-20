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
      // if-else would be boring…
      // it is depending on undefined behaviour through as nowhere is said
      // that the returned value is e.g. 1 and not 40.
      def signum(x: Int) = x.compare(0)
      println("signum(0) = " + signum(0))
      println("signum(42) = " + signum(42))
      println("signum(-42) = " + signum(-42))
    }
  }

  new Task("Task 2") {
    def solution() = {
      println("Unit = ()")
    }
  }

  new Task("Task 4") {
    def solution() = {
      10.to(0, -1).foreach(println)
    }
  }

  new Task("Task 5") {
    def solution() = {
      def countdown(n: Int) = n.to(0, -1).foreach(println)
      countdown(3)
    }
  }

  new Task("Task 10") {
    def solution() = {
      def mypow(x: BigInt, n: Int) : BigInt = {
        if (n == 0) 1
        else if (n < 0) 1 / mypow(x, -n)
        else if (n % 2 == 0) {
          val y = mypow(x, n/2)
          y * y
        } else x * mypow(x, n - 1)
      }
      println("3^3 = " + mypow(3,3))
      println("2^8 = " + mypow(2,8))
      println("0^0 = " + mypow(0,0))
      println("1^0 = " + mypow(1,0))
    }
  }

}
