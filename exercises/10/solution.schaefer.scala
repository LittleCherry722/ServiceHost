import java.awt.Point
import scala.util.logging.Logged

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
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println(egg.getCenterX())
      egg.translate(10, -10)
      println(egg.getCenterX())
      egg.grow(10, 20)
      println(egg.getCenterX())
    }

    trait RectangleLike {
      def getCenterX(): Double

      def getX(): Double
      def getY(): Double
      def getWidth(): Double
      def getHeight(): Double
      def setFrame(x: Double, y: Double, width: Double, height: Double)

      def translate(x: Double, y: Double) {
        setFrame(getX() + x, getY() + y, getWidth(), getHeight())
      }

      def grow(width: Double, height: Double) {
        setFrame(getX(), getY(), getWidth() + width, getHeight() + height)
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      val point1 = new OrderedPoint(4, 2)
      val point2 = new OrderedPoint(3, 2)
      val point3 = new OrderedPoint(4, 1)
      println(point1 < point2)
      println(point2 < point1)
      println(point3 < point1)
    }

    class OrderedPoint(x: Int, y: Int) extends Point(x, y) with Ordered[Point] {
      def compare(other: Point): Int = {
        if (this.x < other.x || (this.x == other.x && this.y < other.y)) -1
        else if (this.x == other.x && this.y == other.y) 0
        else 1
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      var logger = new CryptoLogger()
      logger.log("abc")
      logger = new CryptoLogger(-3)
      logger.log("def")
    }

    class CryptoLogger extends Logged {
      private var key_ = 3

      def this(key: Int) {
        this()
        key_ = key
      }

      override def log(message: String) {
        val encryptedMessage = encrypt(message)
        super.log(encryptedMessage)
        //for testing purposes only
        println(encryptedMessage)
      }

      private def encrypt(message: String): String = {
        val chars = new Array[Char](message.length())
        message.map(x => (x.toInt + key_).toChar)

      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      println("In the better design, JContainer extends Container AND JComponent.")
      println("This isn't possible in Java since each class can only extend ONE single class.")
      println("Scala offers a better solution here because it offers the following possibility:")
      println("JContainer extends JComponent with Container")
      println("..given that Container is a trait")
    }
  }

  new Task("Task 10") {
    def solution() = {
      //not sure if it works like that
    }
    class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
      def read(): Int = readInt()

      def iterator = new Iterator[Byte] {
        def hasNext() = available() != 0

        def next() = readByte()
      }
    }
  }

}
