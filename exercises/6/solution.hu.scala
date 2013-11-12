import scala.Array.canBuildFrom

object Chapter6 extends App {
  Tasks6.execute()
}

abstract class Task6(val name: String) {
  Tasks6 add this
  def solution()
  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks6 {
  private var tasks6 = Seq[Task6]()
  def add(t: Task6) = { tasks6 :+= t }
  def execute() = { tasks6.foreach((t: Task6) => { t.execute() }) }
  def execute(name: String) = { tasks6.filter(_.name == name).head.execute() }
}
object Tasks6 extends Tasks6 {
  new Task6("task 6-2") {
    def solution() = {
      class UnitConversion {
        def inchesToCentimeters() {}
        def gallonsToLiters() {}
        def milesToKilometers() {}
      }

      object InchesToCentimeters extends UnitConversion {
        override def inchesToCentimeters() {}
      }
      object GallonsToLiters extends UnitConversion {
        override def gallonsToLiters() {}
      }
      object MilesToKilometers extends UnitConversion {
        override def milesToKilometers() {}
      }
    }
  }

  new Task6("task 6-4") {
    def solution() = {
      class Point(val x: Int, val y: Int) {
        def newPoint = (x + " , " + y).toString
      }
      object Point extends App {
        def apply(x: Int, y: Int) = {
          new Point(x, y)
        }
        val point = Point(0, 0)
        print(point.newPoint)
      }
    }
  }

  new Task6("task 6-5") {
    def solution() = {
      object HelloWorld extends App {
        for (elem <- args reverse)
          print(elem + " ")
      }
    }
  }

//  new Task6("task 6-6") {
//    def solution() = {
//      object Card extends Enumeration with App {
//        val M = Value("♣")
//        val T = Value("♠")
//        val H = Value("♥")
//        val F = Value("♦")
//
//        println(Card.M)
//        println(Card.T)
//        println(Card.H)
//        println(Card.F)
//      }
//    }
//  }

}