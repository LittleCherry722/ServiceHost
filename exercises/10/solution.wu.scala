import java.awt.Point
import java.io._
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

      // your solution for task 1 here
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)
      println(egg.getX())
      println(egg.getY())
      println(egg.getWidth())
      println(egg.getHeight())

    }

    trait RectangleLike extends java.awt.geom.Ellipse2D.Double {
      def translate(x: Int, y: Int) {
        this.x += x
        this.y += y
      }
      def grow(width: Int, height: Int) {
        this.width += width
        this.height += height
      }
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val p1 = new OrderedPoint(1, 2)
      val p2 = new OrderedPoint(3, 2)
      println(p1 < p2)

    }

    class OrderedPoint(x: Int, y: Int) extends Point(x, y) with Ordered[Point] {
      def compare(that: Point) = {
        if (this.x <= that.x && this.y < that.y)
          -1
        else if (this.x == that.x && this.y == that.y)
          0
        else
          1
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val t = new Test()
      println(t.encode("This is a TEST!"))

    }

    class Test extends CrypToLogger {
      override val key = 1
    }

    trait CrypToLogger {
      val key = -3
      def encode(message: String) = {
        var s = ""
        for (c <- message) {
          if (('a' to 'z').contains(c)) {
            var shift1 = c + (key % 26)
            s = s + (if (shift1 < 'a') shift1 + 26 else if (shift1 > 'z') shift1 - 26 else shift1).toChar
          } else if (('A' to 'Z').contains(c)) {
            var shift2 = c + (key % 26)
            s = s + (if (shift2 < 'A') shift2 + 26 else if (shift2 > 'Z') shift2 - 26 else shift2).toChar
          } else
            s = s + c

        }
        s
      }
    }
  }

  new Task("Task 6") {
    def solution() = {

      println("The design won't work in java, because java doesn't support multiple inheritance of classes,\ni.e. jcontainer can't extend jcomponent and container both at the same time.\nIn scala we can make component and container traits, that way it'll work")

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      val iis = new IterableInputStream(new FileInputStream("src/test/text.txt"))

      for (b <- iis)
        println(b.toChar)

      iis.close

    }

    class IterableInputStream(val is: InputStream) extends InputStream with Iterable[Int] {

      def iterator: Iterator[Int] = new Iterator[Int] {
        def hasNext = is.available() > 0
        def next = is.read()
      }

      def read() = is.read()

    }
  }

}
