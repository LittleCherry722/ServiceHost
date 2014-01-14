
package chapter6

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length

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
      abstract class UnitConversions {
        def convert(in: Double): Double
      }

      object InchesToCentimeter extends UnitConversions {
        override def convert(in: Double): Double = {
          2.54 * in
        }
      }
      object GallonsToLiters extends UnitConversions {
        override def convert(in: Double): Double = {
          3.78541178 * in
        }
      }
      object MilesToKilometers extends UnitConversions {
        override def convert(in: Double): Double = {
          1.609344 * in
        }
      }
      println("2 Inches in Centimeter: " + InchesToCentimeter.convert(2))
      println("3.2 Gallons in Liter: " + GallonsToLiters.convert(3.2))
      println("1.2 Miles in Kilometer: " + MilesToKilometers.convert(1.2))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Point(x: Double, y: Double) {
        println("Point (" + x + ", " + y + ") created.")
        def pointX = x
        def pointY = y
      }
      object Point {
        def apply(x: Double, y: Double) = new Point(x, y)
      }

      val point = Point(3, 4)
      println("Point: x = " + point.pointX + " , y = " + point.pointY)

    }
  }
  new Task("Task 5") {
    def solution() = {
      object Reverse extends App {
        if (args.length > 0)
          args.reverse.foreach(s => print(s + " "))
      }
      Reverse.main(Array("Hello","World","!"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      object CardSymbols extends Enumeration {
        type CardSymbols = Value
        val CLUBS = Value("\u2663")
        val SPADES = Value("\u2660")
        val HEARTS = Value("\u2665")
        val DIAMONDS = Value("\u2666")
      }
      println("Clubs: " + CardSymbols.CLUBS.toString())
      println("Spades: " + CardSymbols.SPADES.toString())
      println("Hearts: " + CardSymbols.HEARTS.toString())
      println("Diamonds: " + CardSymbols.DIAMONDS.toString())
    }
  }
}
