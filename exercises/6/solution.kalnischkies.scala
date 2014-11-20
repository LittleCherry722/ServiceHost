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
      object Conversions {
        def inchesToCentimeters(inches : Double) = inches * 2.540
        def gallonsToLiters(gallons : Double) = gallons * 3.78541178
        def milesToKilometers(miles : Double) = miles * 1.609
      }
      println("inches: " + Conversions.inchesToCentimeters(15.4))
      println("gallons: " + Conversions.gallonsToLiters(25))
      println("miles: " + Conversions.milesToKilometers(80))
    }
  }

  new Task("Task 2") {
    def solution() = {
      abstract class UnitConversion {
        def apply(value: Double) : Double
      }
      object InchesToCentimeters extends UnitConversion {
        def apply(value: Double) = value * 2.540
      }
      object GallonsToLiters extends UnitConversion {
        def apply(value: Double) = value * 3.78541178
      }
      object MilesToKilometers extends UnitConversion {
        def apply(value: Double) = value * 1.609
      }
      println("inches: " + InchesToCentimeters(15.4))
      println("gallons: " + GallonsToLiters(25))
      println("miles: " + MilesToKilometers(80))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(val x: Int, val y: Int) {}
      object Point {
        def apply(x: Int, y: Int) = new Point(x, y)
      }
      val p = Point(3, 4)
      println("Point: (" + p.x + "," + p.y + ")")
    }
  }

  new Task("Task 6") {
    def solution() = {
      object Suite extends Enumeration {
        val Cross = Value("♣")
        val Karo = Value("♦")
        val Heart = Value("♥")
        val Spade = Value("♠")
      }
      println("Cross: " + Suite.Cross + " Karo: " + Suite.Karo + " Heart: " + Suite.Heart + " Spade: " + Suite.Spade)
    }
  }
}
