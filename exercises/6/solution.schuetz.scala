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

      // your solution for task 2 here
      
      abstract class UnitConversion() {
        def convert(value: Double): Double
      }
      
      object InchToCentimeters extends UnitConversion() {
        override def convert(value: Double) = {
          value*2.54
        }
      }
      
      object GallonsToLiters extends UnitConversion() {
        override def convert(value: Double) = {
          value*3.785
        }
      }
      
      object MilesToKilometers extends UnitConversion() {
        override def convert(value: Double) = {
          value*1.609
        }
      }
      
      println("1 inch are " + InchToCentimeters.convert(1) + " cm")
      println("1 gallon are " + GallonsToLiters.convert(1) + " l")
      println("1 mile are " + MilesToKilometers.convert(1) + " km")

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      
      class Point(val x: Integer, val y: Integer) {
      }
      
      object Point {
        def apply(x: Integer, y: Integer) = new Point(x,y)
      }

      val point1 = Point(3,4)
      println("point1 = (" + point1.x + "," + point1.y + ")")
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      
      object Reverse extends App {
        if (args.length > 0) {
          val argsR = args.reverse
          println(argsR.mkString(" "))
        }
      }
      
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      
      object CardSuits extends Enumeration {
        val Karo = Value("♦")
        val Herz = Value("♥")
        val Pik = Value("♠")
        val Kreuz = Value("♣")
      }
      
      println(CardSuits.Karo.toString())
      println(CardSuits.Herz.toString())
      println(CardSuits.Pik.toString())
      println(CardSuits.Kreuz.toString())

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

