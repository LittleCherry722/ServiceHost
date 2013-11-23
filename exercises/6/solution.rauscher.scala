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

  new Task("Task 2") {
    def solution() = {
      abstract class UnitConversation {}
      object InchesToCentimeters extends UnitConversation {
        def apply(inches: Int) = inches * 2.54
      }
      object GallonsToLiters extends UnitConversation {
        def apply(gallons: Int) = gallons * 3.5
      }
      object MilesToKilometers extends UnitConversation {
        def apply(miles: Int) = miles * 1.2
      }
      
      println(InchesToCentimeters(4))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(val x: Int, val y: Int) {
        println("New Point: " + x + "/" + y)
      }
      object Point {
        def apply(x: Int, y: Int) = new Point(x, y)
      }
      new Point(3,5)
      Point(2,3)
    }
  }

  new Task("Task 5") {
    def solution() = {
      // See Reverse-object below
    }
  }

  new Task("Task 6") {
    def solution() = {
      object PlayingCardColor extends Enumeration {
        type PlayingCardColor = Value
        // German names here, my internet is too bad for a dictionary
        val Kreuz = Value("♣")
        val Karo = Value("♦")
        val Herz = Value("♥")
        val Pik = Value("♠")
      }
    }
  }

}

object Reverse extends App {
  println(args.reverse.mkString(""," ",""))
}
