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
    abstract class UnitConversion {
      def apply(value: Double): Double
    }
    
    object InchesToCentimeters extends UnitConversion {
      def apply(value: Double): Double = {
        value / 2.7 // geschätzter Wert
      }
    }
    
    object GallonsToLiters extends UnitConversion {
      def apply(value: Double): Double = {
        value * 3.7 // geschätzter Wert
      }
    }
    
    object MilesToKilometers extends UnitConversion {
      def apply(value: Double): Double = {
        value * 1.6 // geschätzter Wert
      }
    }
    
    def solution() = {

      val miles: Double = 1.5 // distance in miles
      val gallonPerMile: Double = 0.7
      
      val kilometers: Double = MilesToKilometers(miles)
      val literPerMile: Double = GallonsToLiters(gallonPerMile)
      
      println("miles: " + miles);
      println("kilometers: " + kilometers);
      
      println("gallonPerMile: " + gallonPerMile);
      println("literPerMile: " + literPerMile);

    }
  }

  new Task("Task 4") {
    class Point(val x: Int, val y: Int){
      override def toString(): String = {
        "("+x+"|"+y+")"
      }
    }
    object Point {
      def apply(x: Int, y: Int): Point = {
        new Point(x, y)
      }
    }
    
    def solution() = {

      val a: Point = Point(1, 2)
      println("a: " + a);

    }
  }

  new Task("Task 5") {
    def solution() = {

      println("please run:")
      println("scalac solution.wolski.task5.scala")
      println("scala Reverse [arg0 arg1 ... argn]")
      

    }
  }

  new Task("Task 6") {
    object CardColor extends Enumeration {
      type CardColor= Value
      val Kreuz = Value("♣")
      val Karo = Value("♦")
      val Herz = Value("♥")
      val Pik = Value("♠")
    }
    
    def solution() = {

      for(c <- CardColor.values) println(c.id + ": " + c);

      import CardColor._
      val k: CardColor = CardColor.Kreuz
      println("Kreuz: " + k);
    }
  }

}
