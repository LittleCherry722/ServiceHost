object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  // Tasks.execute("Task 1");
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
      def values(fun: (Int) => Int, low: Int, high: Int) =
        for (i <- low to high) yield (i, fun(i))

      println("values(x => x * x, -5, 5) should be:" +
        "\n(-5, 25), (-4, 16), (-3, 9). . ., (5, 25)\n is:\n%s"
        .format(values(x => x * x, -5, 5)))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def largestElement(seq: Seq[Int]) =
        seq.reduceLeft[Int]((x: Int, y: Int) => if (x > y) x else y)

      val a = for (i <- 0 to 10) yield scala.util.Random.nextInt(100)

      println("Largest element of %s is %d".format(a, largestElement(a)))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def fac(x: Int) = if (x <= 1) 1 else (2 to x).reduceLeft[Int](_ * _)

      val n = 6
      println("%d! = %d".format(n, fac(n)))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def fac(x: Int) = (2 to x).foldLeft[Int](1)(_ * _)

      val n = 6
      println("%d! = %d".format(n, fac(n)))
    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]) =
        fun(inputs.reduceLeft[Int]((largest: Int, current: Int) =>
          if (fun(largest) > fun(current)) largest else current))

      println("largest(x => 10 * x - x * x, 1 to 10) should be 25 is %d"
        .format(largest(x => 10 * x - x * x, 1 to 10)))
    }
  }

  new Task("Task 6") {
    def solution() = {
      def largestAt(fun: (Int) => Int, inputs: Seq[Int]) =
        inputs.indexOf(inputs.reduceLeft[Int]((largest: Int, current: Int) =>
          if (fun(largest) > fun(current)) largest else current)) + 1

      println("largestAt(x => 10 * x - x * x, 1 to 10) should be 5 is %d"
        .format(largestAt(x => 10 * x - x * x, 1 to 10)))
    }
  }

  new Task("Task 7") {
    def solution() = {
      def adjustToPair(fun: (Int, Int) => Int): (((Int, Int)) => Int) =
        ((pair: (Int, Int)) => fun(pair._1, pair._2))

      println("adjustToPair(_ * _)((6, 7)) should be 42 is %d"
        .format(adjustToPair(_ * _)((6, 7))))
    }
  }

  new Task("Task 8") {
    def solution() = {
      val a = Array("Hello", "World")
      val c = Array("abc", "defgh")
      val b = Array(5, 5)

      println("[%s] corresponds [%s] = %s"
        .format(a.mkString(", "), b.mkString(", "),
          a.corresponds(b)(_.length() == _)))
      println("[%s] corresponds [%s] = %s"
        .format(c.mkString(", "), b.mkString(", "),
          c.corresponds(b)(_.length() == _)))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def corresponds[A, B](a: Array[A],
                            b: Array[B],
                            f: (A, B) => Boolean): Boolean =
        a.corresponds(b)(f(_, _))

      val a = Array("Hello", "World")
      val c = Array("abc", "defgh")
      val b = Array(5, 5)

      val ab = corresponds(a, b, (s: String, i: Int) => s.length() == i)
      val cb = corresponds(c, b, (s: String, i: Int) => s.length() == i)
      println("[%s] corresponds [%s] = %s"
        .format(a.mkString(", "), b.mkString(", "), ab))
      println("[%s] corresponds [%s] = %s"
        .format(c.mkString(", "), b.mkString(", "), cb))

      println("Problem: Man kann keine Wildcards mehr benutzen, sondern" +
        "muss bei der Funktiond den Typ angeben")
      println("(s: String, i: Int) => s.length() == i statt (_.length() == _)")
    }
  }

  new Task("Task 10") {
    def solution() = {

      def unless(condition: => Boolean)(
        ifbody: => Any)(elsebody: => Any) = {
        if (condition) {
          elsebody
        } else {
          ifbody
        }
      }

      val b = true
      val i = 19
      val e = 10

      println("unless (%s) { %s } { %s } -> %s"
        .format(b, i, e, unless(b) { i } { e }))
    }
  }

}