
package chapter10

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.awt.Point
import scala.math.Ordered
import java.io.InputStream

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
      val egg = new Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println("before translation: x = " + egg.x + ", y = " + egg.y)
      egg.translate(10, -10)
      println("after translation: x = " + egg.x + ", y = " + egg.y)

      println("before growth: width = " + egg.width + ", height = " + egg.height)
      egg.grow(10, 20)
      println("after growth: width = " + egg.width + ", height = " + egg.height)

    }
    trait RectangleLike {
      self: Ellipse2D.Double =>
      def translate(x: Int, y: Int) {
        this.x += x
        this.y += y
      }

      def grow(w: Int, h: Int) {
        this.width += w
        this.height += h
      }
    }
  }
  new Task("Task 2") {
    def solution() = {

      class OrderedPoint(x: Int, y: Int) extends Point with Ordered[Point] {

        def compare(that: Point): Int = {
          if (this.x < that.x) return 1
          println("Methodaccess = " + that.toString + " direct access: (x, y) = (" + that.x + ", " + that.y + ")")
          if (this.x == that.x && y == that.y) return 0
          if (this.y < that.y) 1 else -1
        }

        override def toString: String = "Point(" + this.x + ", " + this.y + ")"

      }
      val p1 = new OrderedPoint(1, 3)
      val p2 = new OrderedPoint(2, 3)
      val p3 = new OrderedPoint(1, 2)

      println("1. Test: " + p1.toString + " < " + p2.toString + ": " + (p1.compare(p2)))
      println("2. Test: " + p1.toString + " < " + p3.toString + ": " + (p1 compare p3))
    }

  }

  new Task("Task 4") {
    def solution() = {
      trait CryptoLogger {
        var k: Int
        def log(msg: String) = println(encode(msg))

        def encode(msg: String): String = {
          for (c <- msg) yield k match {
            case k if k > 0 => if (c isLower) ('a' + (c - 'a' + k) % 26).toChar else ('A' + (c - 'A' + k) % 26).toChar
            case k if k < 0 => if (c isLower) ('a' + (c - 'a' + (26 + k)) % 26).toChar else ('A' + (c - 'A' + (26 + k)) % 26).toChar
          }
        }

      }
      class MyLogger(shift: Int = 3) extends CryptoLogger {
        var k = shift
      }

      val logger = new MyLogger
      val logger2 = new MyLogger(-3)
      logger.log("HalLo")
      logger2.log("HalLo")
    }
  }
  
  new Task("Task 6") {
    def solution() = {
      println("In that case JContainter would have to be a subclass of Container and JComponent which is just not possible. In scala you could mix it up with traits.")
    }
  }

  new Task("Task 10") {
    def solution() = {
      class IterableInputStream extends InputStream with Iterable[Byte] {
        def read() = 0
        def iterator = null
      }
    }
  }
}
