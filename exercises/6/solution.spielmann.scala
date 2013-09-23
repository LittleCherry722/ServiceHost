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
    }
  }

  new Task("Task 2") {
    def solution() = {
      abstract class UnitConversion {
        def convert(x : Double) : Double
      }
      class InchesToCentimeters extends UnitConversion {
        def convert(x : Double) : Double = {
          x * 2.54
        }
      }
      class GallonsToLitersImperial extends UnitConversion {
        def convert(x : Double) : Double = {
          x * 4.54609
        }
      }
      class GallonsToLitersUS extends UnitConversion {
        def convert(x : Double) : Double = {
          x * 3.785411784
        }
      }
      class MilesToKilometers extends UnitConversion {
        def convert(x : Double) : Double = {
          x * 1.609344
        }
      }
      println("var itc = new InchesToCentimeters()")
      println("var gtli = new GallonsToLitersImperial()")
      println("var gtlu = new GallonsToLitersUS()")
      println("var mtk = new MilesToKilometers()")
      var itc = new InchesToCentimeters()
      var gtli = new GallonsToLitersImperial()
      var gtlu = new GallonsToLitersUS()
      var mtk = new MilesToKilometers()
      println("class of itc: " + itc.getClass() + ", superclass of itc: " + itc.getClass().getSuperclass())
      println("class of gtli: " + gtli.getClass() + ", superclass of gtli: " + gtli.getClass().getSuperclass())
      println("class of gtlu: " + gtlu.getClass() + ", superclass of gtlu: " + gtlu.getClass().getSuperclass())
      println("class of mtk: " + mtk.getClass() + ", superclass of mtk: " + mtk.getClass().getSuperclass())
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(var x : Double, var y : Double) {
      }
      object Point {
        def apply(x : Double, y : Double) = new Point(x,y)
      }
      var p = Point(1,2)
      println("var p = Point(1,2)")
      println("p.x = " + p.x + ", p.y = " + p.y)
    }
  }

  new Task("Task 5") {
    def solution() = {
      object Reverse extends App {
        for(arg <- args.reverseIterator) print(arg + " ")
      }
      println("Reverse.main(Array(\"hallo\", \"welt\"))")
      Reverse.main(Array("hallo", "welt"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      object Cards extends Enumeration {
        val Club = Value("♣")
        val Diamond = Value("♦")
        val Heart = Value("♥")
        val Spade = Value("♠")
      }
      println("Cards.Club " + Cards.Club)
      println("Cards.Diamond " + Cards.Diamond)
      println("Cards.Heart " + Cards.Heart)
      println("Cards.Spade " + Cards.Spade)
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
