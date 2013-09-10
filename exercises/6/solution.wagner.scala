import java.io.PrintStream

object Solution extends App {
  Tasks.execute()
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
      abstract class UnitConversion {
        def convert(value: Int): Double
      }
      object InchesToCentimeter extends UnitConversion {
        def convert(value: Int) = value * 2.54
      }
      object GallonsToLiter extends UnitConversion {
        def convert(value: Int) = value * 3.78541
      }
      object MilesToKilometer extends UnitConversion {
        def convert(value: Int) = value * 1.60934
      }
      println("5in to cm -> " + InchesToCentimeter.convert(5))
      println("5gal to l -> " + GallonsToLiter.convert(5))
      println("5mi to m -> " + MilesToKilometer.convert(5))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(x: Int, y: Int) {
        override def toString = s"Point($x,$y)"
      }
      object Point {
        def apply(x: Int, y: Int) = new Point(x, y)
      }
      println(Point(3, 4))
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
    	object CardSuits extends Enumeration {
    	  val Clubs = Value(0x2663.toChar.toString)
    	  val Diamond = Value(0x2666.toChar.toString)
    	  val Heart = Value(0x2665.toChar.toString)
    	  val Spade = Value(0x2660.toChar.toString)
    	  override def toString = s"$Clubs, $Diamond, $Heart, $Spade"
    	}
    	print(CardSuits)
    }
  }

}
