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
      abstract class UnitConversion {
        val factor: Double
        def apply(param: Double): Double = {
          factor * param
        }
      }
      object InchesToCentimeters extends UnitConversion {
        val factor: Double = 2.54
      }
      object GallonsToLiters extends UnitConversion {
        val factor: Double = 3.78541178
      }
      object MilesToKilometers extends UnitConversion {
        val factor: Double = 1.609344
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
      class Point(val x: Double, val y: Double) { }

      object Point {
        def apply(x: Double, y: Double): Point = {
          new Point(x, y)
        }
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      object Reverse extends App {
        if (args.length > 0) {
          println(args.reverse.mkString(" "))
        }
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      object Cards extends Enumeration {
        val hearts = Value(0, "♥")
        val diamonds = Value(1, "♦")
        val spades = Value(2, "♠")
        val clubs = Value(3, "♣")
      }
      println("the four playing card suits are: " +
        Cards.hearts.toString + " " +
        Cards.diamonds.toString + " " +
        Cards.spades.toString + " " +
        Cards.clubs.toString)
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
