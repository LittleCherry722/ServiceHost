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
      trait RectangleLike {
        self: java.awt.geom.RectangularShape =>
        def translate(dx: Int, dy: Int) {
          setFrame(getX + dx, getY + dy, getX + getWidth, getY + getHeight)
        }
        def grow(h: Int, v: Int) {
          setFrame(getX - h, getY - h, getX + getWidth + h, getY + getHeight + v)
        }
      }
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)
    }
  }

  new Task("Task 2") {
    def solution() = {
      import java.awt.Point
      class OrderedPoint(x: Int, y: Int) extends Point(x, y) with scala.math.Ordered[Point] {
        override def compare(that: java.awt.Point): Int = {
          if (x < that.x)
            -1
          else if (x == that.x && y < that.y)
            -1
          else if (x == that.x && y == that.y)
            0
          else
            1
        }
      }

      object OrderedPoint {
        def apply(x: Int, y: Int): OrderedPoint = {
          new OrderedPoint(x, y)
        }
      }

      assert(OrderedPoint(1,2) < OrderedPoint(1,3))
      assert(OrderedPoint(1,1) == OrderedPoint(1,1))
      assert(OrderedPoint(2,-1) > OrderedPoint(1,1))
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      trait Logger {
        var lastLog: String = ""
        def log(msg: String) {
          lastLog = msg
          println(msg)
        }
      }

      trait CryptoLogger extends Logger {
        val key = 3
        private lazy val saneKey = {
          val mod = key %  26
          if (mod >= 0) mod else 26 + mod
        }
        def cipher(plaintext: String): String = {
          plaintext.map(c => if (c == ' ') c else (((c.toLower - 'a' + saneKey) % 26) + 'a').toChar)
        }
        override def log(msg: String) = super.log(cipher(msg))
      }

      class TestLogger extends CryptoLogger

      val defaultLogger = new TestLogger
      val specialLogger = new TestLogger {
        override val key = -3
      }
      defaultLogger.log("the quick brown fox jumps over the lazy dog")
      specialLogger.log("the quick brown fox jumps over the lazy dog")
      defaultLogger.log(specialLogger.lastLog)
      assert(defaultLogger.lastLog == "the quick brown fox jumps over the lazy dog")
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      println("in java, multiple inheritance is not supported.")
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
      class UselessIterator extends java.io.InputStream with Iterable[Byte] {
        override def read(): Int = 0
        def next: Byte = 0
        def hasNext: Boolean  = false
        override def iterator = null
      }
    }
  }

}
