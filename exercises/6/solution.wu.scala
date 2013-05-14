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
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      println(InchesToCentimeters.convert(12))
      println(GallonsToLiters.convert(2))
      println(MilesToKilometers.convert(4))

    }

    abstract class UnitConversion() {
      def convert(n: Double): Double
    }

    object InchesToCentimeters extends UnitConversion {
      override def convert(n: Double): Double = {
        2.54 * n
      }
    }

    object GallonsToLiters extends UnitConversion {
      override def convert(n: Double): Double = {
        3.78541 * n
      }
    }

    object MilesToKilometers extends UnitConversion {
      override def convert(n: Double): Double = {
        1.60934 * n
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val myPoint = Point(4, 5)
      println("(" + myPoint.x + "," + myPoint.y + ")")

    }

    class Point(var x: Int, var y: Int) {

    }

    object Point {
      def apply(x: Int, y: Int) = {
        new Point(x, y)
      }
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }

    object Reverse extends App {

      for (i <- (0 until args.length).reverse)
        print(args(i) + " ")
    }

  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      println(Cards.Clubs)
      println(Cards.Spades)
      println(Cards.Diamonds)
      println(Cards.Hearts)

    }
    object Cards extends Enumeration {
      val Clubs = Value("\u2663")
      val Diamonds = Value("\u2666")
      val Hearts = Value("\u2665")
      val Spades = Value("\u2660")
    }
  }

}