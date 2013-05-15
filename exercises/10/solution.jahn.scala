import java.awt.geom.Ellipse2D
import java.awt.{Point, Rectangle}
import java.io.{ByteArrayInputStream, InputStream}
import scala.util.logging.{ConsoleLogger, Logged}

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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      val egg = new Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)

      val rec = new Rectangle(5, 10, 20, 30)
      rec.translate(10, -10)
      rec.grow(10, 20)

      println("Ellipse: (" + egg.getX() + "," + egg.getY() + ") width:" + egg.getWidth() + " height:" + egg.getHeight())
      println("Rectangle: (" + rec.getX() + "," + rec.getY() + ") width:" + rec.getWidth() + " height:" + rec.getHeight())
    }

    trait RectangleLike {
      def getHeight(): Double

      def getWidth(): Double

      def getX(): Double

      def getY(): Double

      def setFrame(x: Double, y: Double, w: Double, h: Double)

      def translate(dx: Double, dy: Double) {
        val x = getX() + dx
        val y = getY() + dy
        val width = getWidth()
        val height = getHeight()
        setFrame(x, y, width, height)
      }

      def grow(h: Double, v: Double) {
        val x = getX() - h
        val y = getY() - v
        val width = getWidth() + 2 * h
        val height = getHeight() + 2 * v
        setFrame(x, y, width, height)
      }
    }

  }

  new Task("Task 2") {
    def solution() = {
      val point1 = new OrderedPoint(1, 3)
      val point2 = new OrderedPoint(1, 5)
      val point3 = new OrderedPoint(3, 6)

      println(point1 < point2)
      println(point2 < point1)
      println(point2 < point3)
      println(point1 == point1)
    }

    class OrderedPoint(x: Int, y: Int) extends Point(x, y) with Ordered[Point] {
      def compare(that: Point): Int = if (this.x != that.x) this.x - that.x else this.y - that.y
    }

  }

  new Task("Task 4") {
    def solution() = {
      val foo = new Foo with ConsoleLogger with CryptoLogger

      foo.bar()

      foo.key = -3

      foo.bar()
    }

    trait CryptoLogger extends Logged {
      var key = 3

      override def log(msg: String) { super.log(encrypt(msg)) }

      private def encrypt(msg: String): String = msg.map(c => (c + key).toChar)
    }

    class Foo extends Logged {
      def bar() {
        log("Hello")
      }
    }

  }

  new Task("Task 6") {
    def solution() = {
      // This is not possible in Java, because JContainer would extend from two classes: JComponent and Container.
      // In Scala you can extend JContainer from JComponent and mix in a Container trait.
    }
  }

  new Task("Task 10") {
    def solution() = {

      val stream = new IterableInputStream {
        private val source = new ByteArrayInputStream("Hello".getBytes)

        def read(): Int = source.read()
      }

      stream.iterator.foreach(b => print(b.toChar))
      println()
    }

    abstract class IterableInputStream extends InputStream with Iterable[Byte] {
      stream =>

      def iterator: Iterator[Byte] = new Iterator[Byte] {
        var nextByte = stream.read()

        def hasNext: Boolean = nextByte != -1

        def next(): Byte = {
          val current = nextByte
          nextByte = stream.read()
          current.toByte
        }
      }
    }

  }

}
