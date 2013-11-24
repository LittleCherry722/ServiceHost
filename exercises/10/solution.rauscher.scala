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
      trait RectangleLike {
        self: java.awt.geom.RectangularShape =>

        def translate(x: Int, y: Int) = setFrame(getX-x, getY-y, getWidth, getHeight)
        def grow(x: Int, y: Int) = setFrame(getX, getY, getWidth + x, getHeight + y)
      }
      
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x,y) with scala.math.Ordered[java.awt.Point] {
        def compare(that: java.awt.Point): Int = if (this.x == that.x) this.y - that.y else this.x - that.x
      }
      val p1 = new OrderedPoint(3,5)
      val p2 = new OrderedPoint(4,2)
      val p3 = new OrderedPoint(3,4)
      println(p1 compare p2)
      println(p2 compare p3)
      println(p3 compare p1)
    }
  }

  new Task("Task 4") {
    def solution() = {
      class CryptoLogger(val key: Int) {
        def this() {
          this(3)
        }
        def encrypt(input: String) = (for (char <- input) yield (char + key).toChar)
      }
      val crypted = new CryptoLogger().encrypt("Hallo Welt")
      println(crypted)
      val clear = new CryptoLogger(-3).encrypt(crypted)
      println(clear)
    }
  }

  new Task("Task 6") {
    def solution() = {
      // Java does not support multiple parentclasses or adding external functionality to a finished class.
      // In scala, we can combine two objects using the "with"-keyword
    }
  }

  new Task("Task 10") {
    def solution() = {
      class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        def read(): Int = ???
        def iterator: Iterator[Byte] = ???
      }
    }
  }

}
