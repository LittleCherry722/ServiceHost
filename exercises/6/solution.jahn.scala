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

  new Task("Task 2") {
    def solution() = {

    }

    class UnitConversion(val factor: Double) {
      def convert(value: Double) = value * factor
    }

    object InchesToCentimeter extends UnitConversion(2.54)

    object GallonsToLiter extends UnitConversion(3.78541)

    object MilesToKilometer extends UnitConversion(1.60934)

  }

  new Task("Task 4") {
    def solution() = {
      val p = Point(3, 4)
      println(p.x + "-" + p.y)
    }

    class Point(var x: Int, var y: Int)

    object Point {
      def apply(x: Int, y: Int) = new Point(x, y)
    }

  }

  new Task("Task 5") {
    def solution() = {
      ArgsPrinter.main(Array("Hello", "World"))
    }

    object ArgsPrinter extends App {
      println(args.reverse.mkString(" "))
    }

  }

  new Task("Task 6") {
    def solution() = {
      println(PlayingCard.Diamonds)
    }

    object PlayingCard extends Enumeration {
      val Spades = Value("♠")
      val Hearts = Value("♥")
      val Diamonds = Value("♦")
      val Clubs = Value("♣")
    }

  }
}
