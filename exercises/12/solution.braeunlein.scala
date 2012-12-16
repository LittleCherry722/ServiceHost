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
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

      def values(fun: (Int) => Int, low: Int, high: Int) = {
        if (low < high) {
          for (x <- low to high) yield { (x, fun(x)) }
        }
      }
      println(values(x => x * x, -5, 5))

    }
  }

  new Task("Task 2") {
    def solution() = {

      var a = Array(1, 2, 7, 4, 5, 3)
      println(a.reduceLeft(_ max _))

    }
  }

  new Task("Task 3") {
    def fact(n: Int) = if (n == 0) 1 else (1 to n).reduceLeft(_ * _)

    def solution() = {
      println(fact(5))
    }
  }

  new Task("Task 4") {
    def fact(n: Int) = (1 to n).foldLeft(1)(_ * _)

    def solution() = {
      println(fact(5))
    }
  }

  new Task("Task 5") {
    def solution() = {

      def largest(fun: (Int) => Int, inputs: Seq[Int]) = { inputs.map(fun).max }
      println(largest(x => 10 * x - x * x, 1 to 10))

    }
  }

  new Task("Task 6") {
    def solution() = {

      def largestAt(fun: (Int) => Int, inputs: Seq[Int]) = (inputs zip (inputs map fun)).maxBy(_._2)._1
      println(largestAt(x => 10 * x - x * x, 1 to 10))

    }
  }

  new Task("Task 7") {
    def solution() = {

      def adjustToPair(f: (Int, Int) => Int) = (x: (Int, Int)) => f(x._1, x._2)
      println(adjustToPair(_ * _)((6, 7)))

    }
  }

  new Task("Task 8") {
    def solution() = {

      var a = Array("1", "22", "333")
      var b = Array(1, 2, 3)
      println(a.corresponds(b)(_.length == _))

    }
  }

  new Task("Task 9") {
    def solution() = {

      def myCorresponds(a: Array[String], b: Array[Int], f: (String, Int) => Boolean) = {
        var result = true
        if (a.length == b.length) {
          for (i <- 0 until a.length) {
            if (!f(a(i), b(i))) {
              result = false
            }
          }
        } else result = false
        result
      }
      
      println(myCorresponds(Array("1", "22", "333"), Array(1, 2, 3), (_.length == _)))
      // Der andere Aufruf ist nicht möglich, da die Methoda 3 Parameter erwartet
    }
  }

  new Task("Task 10") {
    def solution() = {

      def unless(condition: => Boolean) (block: => Unit) = {
        if(! condition) {
          block
        }
      }
      
      var x = false
      unless(x) {println("!")}
      
      // Man braucht Currying, damit man den auszuführenden Code Block in {} übergeben kann

    }
  }

}
