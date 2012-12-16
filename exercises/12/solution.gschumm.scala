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
    def values(fun: (Int) => Int, low: Int, high: Int) =
      for (i <- low to high) yield (i, fun(i))

    def solution() = {
      println(values(x => x * x, -5, 5))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val a = Array(34, 2, 6, 23, 511, 23)
      println(a.reduceLeft(math.max(_, _)))
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
    def largest(fun: (Int) => Int, inputs: Seq[Int]) = inputs.map(fun(_)).max

    def solution() = {
      println(largest(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 6") {
    def largestAt(fun: (Int) => Int, inputs: Seq[Int]) = inputs.maxBy(fun)

    def solution() = {
      println(largestAt(x => 10 * x - x * x, 1 to 10))
    }
  }

  new Task("Task 7") {
    def adjustToPair(fun: (Int, Int) => Int)(p: (Int, Int)) = fun(p._1, p._2)

    def solution() = {
      println(adjustToPair(_ * _)((6, 7)))
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a1 = Array("a", "ab", "abc")
      val a2 = Array(1, 2, 3)
      val a3 = Array(3, 2, 1)

      println(a1.corresponds(a2)(_.length == _))
      println(a1.corresponds(a3)(_.length == _))
    }
  }

  new Task("Task 9") {
    def corresponds[A, B](a: Array[A], b: Array[B], fun: (A, B) => Boolean) =
      (for (i <- a.indices) yield fun(a(i), b(i))).reduceLeft(_ && _)

    def solution() = {
      val a1 = Array("a", "ab", "abc")
      val a2 = Array(1, 2, 3)
      val a3 = Array(3, 2, 1)

      //println(corresponds(a1, a2, _.length == _))
      //println(corresponds(a1, a3, _.length == _))

      // Types of a and b in function aren't recognized.

      println(corresponds(a1, a2, (s: String, l: Int) => s.length == l))
      println(corresponds(a1, a3, (s: String, l: Int) => s.length == l))
    }
  }

  new Task("Task 10") {
    def unless(cond: Boolean)(block: => Unit) = if (!cond) block

    // By-name not necessary for condition because condition is evaluated only once.

    def solution() = {
      val x = 1
      
      unless(x == 1) { println("not 1") }
      unless(x == 2) { println("not 2") }
    }
  }

}
