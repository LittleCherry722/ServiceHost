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

      // your solution for task 1 here

    }
  }

  new Task("Task 2") {
    def solution() = {
      abstract class UnitConversion(val description: String) {
        def convert(x: Double): Double
      }

      object InchesToCentimeters extends UnitConversion("Convert inches to centimeters") {
        override def convert(x: Double) = 2.54 * x
      }

      object GallonsToLiters extends UnitConversion("Convert gallons to liters") {
        override def convert(x: Double) = 3.78541178 * x
      }

      object MilesToKilometers extends UnitConversion("Convert miles to kilometers") {
        override def convert(x: Double) = 1.609344 * x
      }
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(val x: Double, val y: Double) {}

      object Point {
        def apply(x: Double, y: Double) = new Point(x, y)
      }

      val p = Point(10, 15)
      println("(" + p.x + "|" + p.y + ")")

    }
  }

  new Task("Task 5") {
    def solution() = {
      object Reverse extends App {
        println(args.reverse.mkString(" "))
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      object Card extends Enumeration {
        val Clubs = Value("\u2618")
        val Diamonds = Value("\u2666")
        val Hearts = Value("\u2665")
        val Spades = Value("\u2660")
      }

      println(Card.Clubs)
      println(Card.Diamonds)
      println(Card.Hearts)
      println(Card.Spades)
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
