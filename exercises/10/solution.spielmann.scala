import java.awt._
import java.awt.geom._

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
      trait RectangleLike extends RectangularShape {
        def getX: Double
        def getY: Double
        def getWidth: Double
        def getHeight: Double
        def setFrame(x: Double, y: Double, w: Double, h: Double)
        def translate(dx: Int, dy: Int) {
          setFrame(getX + dx, getY + dy, getWidth, getHeight)
        }
        def grow(h: Int, v: Int) {
          setFrame(getX - h, getY - v, getWidth + (2 * h), getHeight + (2 * v))
        }
      }
      
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println("bounds of egg: " + egg.getBounds)
      egg.translate(10, -10)
      println("egg.translate(10, -10)")
      println("bounds of egg: " + egg.getBounds)
      egg.grow(10, 20)
      println("egg.grow(10, 20)")
      println("bounds of egg: " + egg.getBounds)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class OrderedPoint(x: Int, y: Int) extends Point(x, y) with scala.math.Ordered[Point] {
        def compare(that: Point) = {
          if (this.x < that.x || this.x == that.x && this.y < that.y)
            (-1)
          else {
            if (this.x == that.x && this.y == that.y)
              0
            else
              1
          }
        }
      }
      println("var p1 = new OrderedPoint(1,1)")
      println("var p2 = new OrderedPoint(1,0)")
      println("var p3 = new OrderedPoint(0,1)")
      var p1 = new OrderedPoint(1,1)
      var p2 = new OrderedPoint(1,0)
      var p3 = new OrderedPoint(0,1)
      println("p1 < p1" + (p1 < p1))
      println("p1 > p1" + (p1 > p1))
      println("p1 == p1" + (p1 == p1))
      println("p1 < p2" + (p1 < p2))
      println("p1 > p2" + (p1 > p2))
      println("p1 == p2" + (p1 == p2))
      println("p1 < p3" + (p1 < p3))
      println("p1 > p3" + (p1 > p3))
      println("p1 == p3" + (p1 == p3))
    }
  }

  new Task("Task 3") {
    def solution() = {
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Logger {
        def log(msg: String) {
          println("log: " + msg)
        }
      }
      class CryptoLogger(val key: Int = 3) extends Logger{
        override def log(msg: String) {
          super.log(msg.map(x => (x.toInt + key).toChar))
        }
      }
      var s = "abc Hallo Welt!"
      val clPlus3 = new CryptoLogger()
      val clMinus3 = new CryptoLogger(-3)
      println("var s = \"abc Hallo Welt!\"")
      println("val clPlus3 = new CryptoLogger()")
      println("val clMinus3 = new CryptoLogger(-3)")
      clPlus3.log(s)
      clMinus3.log(s)
    }
  }

  new Task("Task 5") {
    def solution() = {
    }
  }

  new Task("Task 6") {
    def solution() = {
      println("jContainer inherits from Container and jComponent in figure 10-4. In java a class can only" +
      		"inherit from one class. Container could be a trait in scala wich can then be mixed into jContainer.")
    }
  }

  new Task("Task 7") {
    def solution() = {
    }
  }

  new Task("Task 8") {
    def solution() = {
    }
  }

  new Task("Task 9") {
    def solution() = {
    }
  }

  new Task("Task 10") {
    def solution() = {
      abstract class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        isIt =>
        class InputStreamIterator extends Iterator[Byte] {
          def hasNext = {
            isIt.available() > 0
          }
          def next() = {
            isIt.read().toByte
          }
        }
        def iterator = new InputStreamIterator()
      }
    }
  }

}
