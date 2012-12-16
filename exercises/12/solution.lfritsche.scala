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
      val a = values(x => x * x, -5, 5)
      println(a.mkString(" "))
    }

    def values(fun: (Int) => Int, low: Int, high: Int) = {
      for (i <- low to high) yield (i, fun(i))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val a = Array(11, 2, 5, 10, 3, 1, 5, 8)
      println(a.reduceLeft(_ max _))

    }
  }

  new Task("Task 3") {
    def solution() = {
      println(factorial(0))
      println(factorial(1))
      println(factorial(2))
      println(factorial(3))
      println(factorial(4))
    }

    def factorial(i: Int) = {
      if (i == 0)
        0
      else {
        val a = (1 to i)
        a.reduceLeft(_ * _)
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      println(factorial(0))
      println(factorial(1))
      println(factorial(2))
      println(factorial(3))
      println(factorial(4))
    }

    def factorial(i: Int) = {
      val a = (1 to i)
      a.foldLeft(1)(_ * _)
    }
  }

  new Task("Task 5") {
    def solution() = {
      println(largest(x => 10 * x - x * x, 1 to 10))
    }

    def largest(fun: (Int) => Int, inputs: Seq[Int]) = {
      val a = inputs.map(fun)
      a.reduceLeft(_ max _)
    }
  }

  new Task("Task 6") {
    def solution() = {
      println(largestAt(x => 10 * x - x * x, 1 to 10))
    }

    def largestAt(fun: (Int) => Int, inputs: Seq[Int]) = {
      val a = inputs.map(fun)
      inputs.filter(fun(_) == a.reduceLeft(_ max _)).first
    }
  }

  new Task("Task 7") {
    def solution() = {
      val a = Array((6, 7), (2, 5), (3, 7))
      println(a.map(adjustToPair(_ * _)).sum)
    }

    def adjustToPair(fun: (Int, Int) => Int)(pair: (Int, Int)) = {
      fun(pair._1, pair._2)
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a: Array[String] = Array("Bla", "Blubb", "Bäääh")
      val b: Array[Int] = Array(3, 5, 5)
      val c: Array[Int] = Array(3, 4, 5)

      println(a.corresponds(b)(compareLength))
      println(a.corresponds(c)(compareLength))
    }

    def compareLength(s: String, i: Int) = {
      s.length() == i
    }
  }

  new Task("Task 9") {
    def solution() = {
      val a: Array[String] = Array("Bla", "Blubb", "Bäääh")
      val b: Array[Int] = Array(3, 5, 5)
      val c: Array[Int] = Array(3, 4, 5)

      println(a.corresponds(b)(compareLength(_, _)))
      println(a.corresponds(c)(compareLength(_, _)))
    }

    def compareLength(s: String, i: Int) = {
      s.length() == i
    }
  }

  new Task("Task 10") {
    def solution() = {
      
    }

    class Unless(body: => Unit) {
      
    }

  }

}