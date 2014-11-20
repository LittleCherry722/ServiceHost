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
        def setFrame(x: Double, y: Double, w: Double, h: Double)
        def getX(): Double
        def getY(): Double
        def getWidth(): Double
        def getHeight(): Double
        def translate(x: Double, y: Double) { setFrame(getX + x, getY + y, getWidth, getHeight) }
        def grow(w: Double, h: Double) { setFrame(getX, getY, getWidth * w, getHeight * h) }
        override def toString = "(" + getX + "," + getY + "," + getWidth + "," + getHeight + ")"
      }
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println(egg)
      egg.translate(10, -10)
      println(egg)
      egg.grow(10, 20)
      println(egg)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x,y) with scala.math.Ordered[java.awt.Point] {
        def compare(that: java.awt.Point) = {
          val cx = x.compare(that.x)
          if (cx == 0)
            y.compare(that.y)
          else
            cx
        }
        override def toString = "(" + getX + "," + getY + ")"
      }
      val p1 = new OrderedPoint(1, 3)
      val p2 = new OrderedPoint(5, 2)
      val p3 = new OrderedPoint(5, 6)
      println("" + p1 + " < " + p2 + " -> " + (p1 < p2))
      println("" + p2 + " < " + p1 + " -> " + (p2 < p1))
      println("" + p2 + " < " + p3 + " -> " + (p2 < p3))
      println("" + p3 + " < " + p2 + " -> " + (p3 < p2))
    }
  }

  new Task("Task 4") {
    def solution() = {
      import scala.util.logging.Logged
      trait Logger extends Logged {
        override def log(msg: String) = println(msg)
      }
      trait CryptoLogger extends Logger {
        val n = 3
        override def log(msg: String) = super.log(msg.toCharArray.map(x => (if (x == ' ') ' ' else ((((x - 'A') + (26 - n)) % 26) + 'A').toChar)).mkString)
      }
      class ShowLine extends Logged {
        def now = log("THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG  QEB NRFZH YOLTK CLU GRJMP LSBO QEB IXWV ALD")
      }
      val sl1 = new ShowLine
      sl1.now // prints nothing
      val sl2 = new ShowLine with Logger
      sl2.now
      val sl3 = new ShowLine with CryptoLogger
      sl3.now
      val sl4 = new { override val n = -3 } with ShowLine with CryptoLogger
      sl4.now
    }
  }

  new Task("Task 6") {
    def solution() = {
      println("Multiple inherence (JContainer) isn't possible in Java")
      trait Component {}
      trait JComponent extends Component {}
      trait Container extends Component {}
      trait JContainer extends Container with JComponent {}
      class JPanel extends JContainer {}
      class JButton extends JComponent {}
      // this is silly…
    }
  }

  new Task("Task 10") {
    def solution() = {
      class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        // the task doesn't say it should do something useful…
        // in fact, it doesn't even say it has to compile so the first line
        // should be enough already… (copy-paste comment from task 6)
        def iterator = null
        def read = 0
      }
    }
  }
}
