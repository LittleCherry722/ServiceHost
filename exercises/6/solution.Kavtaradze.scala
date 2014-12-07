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

      abstract class UnitConversion {
	  def convert(value: Double) : Double
  }

  object InchesToCentimeters extends UnitConversion {
	  def convert(value: Double) = {
		  value * 2.5
	  }
  }

  object GallonsToLiters extends UnitConversion {
	  def convert(value: Double) = {
		  value * 4.5 
	  }
  }
 
  object MilesToKilometers extends UnitConversion {
	  def convert(value: Double) = {
		  value * 1.6
	  } 
  }


  println(InchesToCentimeters.convert(12))
  println(GallonsToLiters.convert(10))
  println(MilesToKilometers.convert(10))

    }
  }


  new Task("Task 4") {
    def solution() = {

      class Point(var x: Int, var y: Int) {
    	 println(x + " " + y)
     }

     object Point {
    	 def apply(x: Int, y: Int) = new Point(x, y)
     }

     val point = Point(3, 4) 


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

      object Cards extends Enumeration {
    val Clubs = Value("♣")
    val Diamonds = Value("♦")
    val Hearts = Value("♥")
    val Spades = Value("♠")
  }
  println(Cards.values.toString)

    }
  }

 

}
