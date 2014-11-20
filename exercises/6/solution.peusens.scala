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

      // your solution for task 2 here
      abstract class UnitConversion {
        def apply(value: Double): Double
      }
    
      object InchesToCentimeters extends UnitConversion {
        def apply(value: Double): Double = value * 0.393
      }
    
      object GallonsToLiters extends UnitConversion {
        def apply(value: Double): Double = value * 3.785
      }
     
      object MilesToKilometers extends UnitConversion {
        def apply(value: Double): Double =  value * 1.609
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      class Point(val init_x: Int, val init_y: Int){
        private var x = init_x
        private var y = init_y
      }
    
      object Point {
        def apply(init_x: Int, init_y: Int) = {
          new Point(init_x, init_y)
        }
      }

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      object ReverseCommandLineArguments extends App {
        if (args.length > 0){
          val args_reversed = args.reverse
          println(args_reversed.mkString(" "))
        }
        else
          println("No Arguments available!")
        }

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      object PlayingCards extends Enumeration {
        type PlayingCards = Value
        val Kreuz = Value("♣")
        val Karo = Value("♦")
        val Herz = Value("♥")
        val Pic = Value("♠")
      }


    }
  }

}
