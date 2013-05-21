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
        def getX(): Double
        def getY(): Double
        def getWidth(): Double
        def getHeight(): Double
        def setFrame(x: Double, y: Double, w: Double, h: Double)

        def translate(dx: Double, dy: Double) {
          this.setFrame(this.getX + dx, this.getY + dy, this.getWidth, this.getHeight)
        }

        def grow(h: Double, v: Double) {
          this.setFrame(this.getX - h, this.getY - v, this.getWidth + 2 * h, this.getHeight + 2 * v);
        }

      }

      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println("" + egg.getX + " - " + egg.getY() + " - " + egg.getWidth() + " - " + egg.getHeight());
      egg.translate(10, -10)
      println("" + egg.getX + " - " + egg.getY() + " - " + egg.getWidth() + " - " + egg.getHeight());
      egg.grow(10, 20)
      println("" + egg.getX + " - " + egg.getY() + " - " + egg.getWidth() + " - " + egg.getHeight());

    }
  }

  new Task("Task 2") {
    def solution() = {
      class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x, y) with scala.math.Ordered[java.awt.Point] {
        def compare(that: java.awt.Point) = {
          if (this.x < that.x) {
            -1
          } else if (this.x == that.x) {
            if (this.y < that.y) {
              -1
            } else if (this.y == that.y) {
              0
            } else {
              1
            }
          } else {
            1
          }
        }
      }

      val p1 = new OrderedPoint(10, 10)
      val p2 = new OrderedPoint(11, 9)
      val p3 = new OrderedPoint(10, 11)
      val p4 = new OrderedPoint(10, 10)

      println("" + (p1 < p2) + " " + (p1 < p3) + " " + (p1 < p4))
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      class CryptoLogger extends scala.util.logging.Logged with scala.util.logging.ConsoleLogger {
        var key = 3

        override def log(msg: String) {
          super.log(msg.map(c => (c + key).toChar))
        }
      }

      val l1 = new CryptoLogger
      l1.log("This is a log message")
      l1.key = -3
      l1.log("This is a log message")
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // In Java, interfaces could have only abstract methods and fields, so Container and JComponent must be
      // classes in Java. JContainer cannot inherit from two classes. In Scala, trait can have concrete methods 
      // and fields, so we can make Container as a trait and then mix it to JComponent.

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
      abstract class InterableInputStream extends java.io.InputStream with Iterable[Byte] {
        def iterator = new Iterator[Byte] {
          def hasNext = {
            available() > 0
          }

          def next = {
            read.asInstanceOf[Byte]
          }
        }
      }

    }
  }

}
