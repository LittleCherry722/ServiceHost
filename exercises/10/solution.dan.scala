import scala.collection.JavaConverters._

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
      //http://stackoverflow.com/questions/9169500/enhance-java-classes-using-traits-how-to-declare-inside-trait-the-java-fields
      trait RectangleLike {
        self: java.awt.geom.RectangularShape =>
        def translate(dx: Double, dy: Double) {
          setFrame(getX + dx, getY + dy, getWidth, getHeight)
        }
        def grow(h: Double, v: Double) {
          setFrame(getX, getY, getWidth * v, getHeight * h)
        }
      }

      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      println(egg.getX() + " " + egg.getY())
      egg.grow(10, 20)
      println(egg.getHeight() + " " + egg.getWidth())

    }
  }

  new Task("Task 2") {
    def solution() = {
      class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x, y) with scala.math.Ordered[OrderedPoint] {

        def compare(p: OrderedPoint) = {
          if (x < p.x)
            -1
          else if (x == p.x) {
            if (y < p.y)
              -1
            else
              1
          } else 1
        }
        override def toString() = "[" + x + ";" + y + "]"
      }
      val x1 = new OrderedPoint(0, 1)
      val x2 = new OrderedPoint(1, 0)
      val x3 = new OrderedPoint(1, 1)
      val x4 = new OrderedPoint(2, 0)
      val x = Array(x3, x1, x4, x2)
      scala.util.Sorting.quickSort(x)
      println(x(0))
      println(x(1))
      println(x(2))
      println(x(3))
    }
  }

  new Task("Task 4") {
    def solution() = {
      trait Logger {
        def log(msg: String)
      }

      trait CryptoLogger extends Logger {
        val key: Int = 3;
        def log(msg: String) {
          //caesar
          val encoded = for (i <- msg.toCharArray())
            yield ((i + key - 'a') % 26 + 'a').toChar
          println(encoded.mkString)
        }
      }
      class Empty()
      val cLogger = new Empty with CryptoLogger
      cLogger.log("test")
      val cLogger2 = new Empty with CryptoLogger {
        override val key: Int = -3
      }
      cLogger2.log("whvw")
      //user key = -3
    }
  }

  new Task("Task 6") {
    def solution() = {
      val msg = "JContainer can't extends both JComponent and Container in Java"
      println(msg)
      class Component

      class JComponent extends Component
      trait Container extends Component

      class JButton extends JComponent

      class JContainer extends JComponent with Container
      class JPanel extends JContainer
    }
  }

  new Task("Task 10") {
    def solution() = {
      class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        def next: Byte = read().toByte
        def hasNext: Boolean = available() == 0
      }
    }
  }

}
