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
      println(InchesToCentimeter.convert(2))

    }
    abstract class UnitConversion {
      def convert(amount: Double): Double
    }

    object InchesToCentimeter extends UnitConversion {
      def convert(inches: Double) = inches * 2.54
    }
    object GallonsToLiter extends UnitConversion {
      def convert(gallons: Double) = gallons * 3.7854
    }
    object MilesToKilometer extends UnitConversion {
      def convert(miles: Double) = miles * 1.6093
    }
  }

  new Task("Task 4") {
    def solution() = {
      val point = Point(3, 5)
      println(point.x + "|" + point.y)

    }
    class Point(var x: Double, var y: Double)

    object Point {
      
      def apply(x: Double, y: Double) = new Point(x, y)
    }
  }

  new Task("Task 5") {
    def solution() = {
      ArgumentsReversed.main(Array("This", "is", "a", "test"))
    }
    object ArgumentsReversed extends App {
      println(args.reverse.mkString(" "))
    }
  }

  new Task("Task 6") {
    def solution() = {
      println(Suits.values.mkString(" "))
    }
    object Suits extends Enumeration {
      val clubs = Value("♣")
      val diamonds = Value("♦")
      val hearts = Value("♥")
      val spades = Value("♠")
    }
  }

}
