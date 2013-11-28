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
    trait RectangleLike extends java.awt.geom.Ellipse2D {
      def translate(dx: Int, dy: Int): Unit = {
        this.setFrame(this.getX + dx, this.getY + dy, this.getWidth, this.getHeight)
      }
      
      def grow(h: Int, v: Int): Unit = {
        this.setFrame(this.getX - h, this.getY - v, this.getWidth + 2*h, this.getHeight + 2*v)
      }
      
      def mkString(): String = {
        "x="+this.getX+";y="+this.getY+";w="+this.getWidth+";h="+this.getHeight
      }
    }
    
    def solution() = {
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      println("original: "+egg.mkString)
      egg.translate(10, -10)
      println("translate: "+egg.mkString)
      egg.grow(10, 20)
      println("grow: "+egg.mkString)
    }
  }

  new Task("Task 2") {
    class OrderedPoint(val orderedX: Int, val orderedY: Int) extends java.awt.Point(orderedX, orderedY) with scala.math.Ordered[java.awt.Point] {
      
      def compare(that: java.awt.Point): Int = {
        this.compare(this.asInstanceOf[java.awt.Point], that.asInstanceOf[java.awt.Point])
      }
      
      
      def compare(that: OrderedPoint): Int = {
        this.compare(that.asInstanceOf[java.awt.Point])
      }
      
      def compare(self: java.awt.Point, that: java.awt.Point): Int = {
        if (this.getX < that.getX || (this.getX == that.getX && this.getY < that.getY)) {
          -1
        }
        else if (this.getX == that.getX && this.getY == that.getY) {
          0
        }
        else {
          1
        }
      }
      /*
      def <(that: java.awt.Point): Boolean = {
        this.compare(that) < 0
      }
      */
    }
    
    def solution() = {
      import java.awt.Point
      
      val a1 = new OrderedPoint(-1, -1)
      println("a1: " + a1);
      
      val a2 = new OrderedPoint(2, -1)
      println("a2: " + a2);
      
      val a3 = new OrderedPoint(-2, 1)
      println("a3: " + a3);
      
      println("a1 compare a2: " + a1.compare(a2))
      println("a1 < a2:" + (a1 < a2))
      
      println("a1 compare a3: " + a1.compare(a3))
      println("a1 < a3:" + (a1 < a3))
      
      // TODO: this fails
      val x = Array(new OrderedPoint(-1, -1), new OrderedPoint(0, 0), new OrderedPoint(1, 1))
      //scala.util.Sorting.quickSort(x)
      println("x: "+x.mkString)
    }
  }

  new Task("Task 4") {
    class CryptoLogger(val key: Int = 3) {
      private val buffer = new StringBuffer
      private def encrypt(msg: String) = for (c <- msg) yield (c+key).toChar
      def log(msg: String) = this.buffer.append(this.encrypt(msg+"\n"))
      override def toString = this.buffer.toString
    }
    def solution() = {
      val logger0 = new CryptoLogger(0)
      val logger1 = new CryptoLogger
      val logger2 = new CryptoLogger(-3)
      
      logger0.log("start..")
      logger0.log("Hallo verschlüsselte Welt")
      println("logger0: " + logger0)
      
      logger1.log("start..")
      logger1.log("Hallo verschlüsselte Welt")
      println("logger1: " + logger1)
      
      logger2.log(logger1.toString)
      println("logger2: " + logger2)
    }
  }

  new Task("Task 6") {
    def solution() = {

      println("As Container extends Component, JContainer should extend JComponent, but also Container. However, a Java class can only extend one other class.")
      println("In Scala you could choose JComponent to be a trait and define JContainer 'with' it.")

    }
  }

  new Task("Task 10") {
    class IterableInputStreamIterator(val stream: java.io.InputStream) extends Iterator[Byte] {
      def next: Byte = {stream.read().asInstanceOf[Byte]}
      def hasNext: Boolean = stream.available > 0
    }
    
    trait IterableInputStream extends java.io.InputStream with Iterable[Byte] {
      def iterator(): Iterator[Byte] = new IterableInputStreamIterator(this)
    }

    def solution() = {
      class IterableFileInputStream(filename: String) extends java.io.FileInputStream(filename) with IterableInputStream;

      val file: IterableInputStream = new IterableFileInputStream("solution.wolski.scala")
      // uncomment next line for demonstration
      //for (byte <- file) print(byte.toChar);


      // this would not work
      //val file = new java.io.FileInputStream("solution.wolski.scala")
      //for (byte <- file) print(byte.toChar);
    }
  }

}
