object Solution extends App {
  Tasks.execute()
}

abstract class Task(val name: String) {
  Tasks.add(this)

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = tasks :+= t
  def execute() = tasks foreach { _.execute() }
  def execute(name: String) = (tasks filter { _.name == name } head).execute()
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      // http://docs.oracle.com/javase/6/docs/api/java/awt/Rectangle.html
      // http://docs.oracle.com/javase/6/docs/api/java/awt/geom/Ellipse2D.Double.html
      import java.awt.geom.RectangularShape
      import java.awt.geom.Ellipse2D

      trait RectangleLike extends RectangularShape {
        def translate(dx: Int, dy: Int) {
          this.setFrame(getX+dx, getY+dy, getWidth, getHeight)
        }
        def grow(h: Int, v: Int) {
          this.setFrame(getX-h, getY-v, getWidth+2*h, getHeight+2*v)
        }
      }

      val egg = new Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
	  egg.translate(10, -10)
	  assert(egg.getX==15 && egg.getY==0)
	  assert(egg.getWidth==20 && egg.getHeight==30)
	  egg.grow(10, 20)
	  assert(egg.getX==5 && egg.getY==(-20))
	  assert(egg.getWidth==40 && egg.getHeight==70)
    }
  }

  new Task("Task 2") {
    def solution() = {
      // http://docs.oracle.com/javase/6/docs/api/java/awt/Point.html
      import java.awt.Point
      import scala.math.Ordered

      class OrderedPoint(x: Int, y: Int) extends Point(x,y) with Ordered[OrderedPoint] {
        def compare(that: OrderedPoint): Int =
          if (x < that.x || x == that.x && y < that.y) -1
          else if (x == that.x && y == that.y) 0
          else 1
      }

      val p1 = new OrderedPoint(1,2)
      val p2 = new OrderedPoint(2,3)
      val p3 = new OrderedPoint(1,4)
      val p4 = new OrderedPoint(1,4)
      val l = Array(p3,p2,p1)
      assert(p1 < p2)
      assert(p2 > p3)
      assert(p3 == p4)
    }
  }

  new Task("Task 4") {
    def solution() = {
      object CryptoLogger {
        private def caesar(chr: Char, key: Int) =
          if (chr.isLower) shift(chr, key, 'a')
          else shift(chr, key, 'A')
        private def shift(c: Char, i: Int, start: Char) =
          (start + (c - start + i) % 26).toChar
        def encrypt(m: String, i: Int) = m map { caesar(_, i) }
        def decrypt(m: String, i: Int) = encrypt(m, 26 - i)
      }
      println(CryptoLogger.encrypt("ABC", 3))
    }
  }

  new Task("Task 6") {
    def solution() = {
      /*
    	 * Java unterstŸtzt keine Mehrfachvererbung. Um einer Klasse die
    	 * FŠhigkeiten zweier anderer Klassen zu verleihen mŸssen diese
    	 * daher in der selben Hierarchie vertikal angeordnet werden.
    	 * In Scala kšnnte man stattdessen Container als trait definieren,
    	 * JContainer von JComponent ableiten und dann Container als
    	 * mixin verwenden.
    	 */
    }
  }

  new Task("Task 10") {
    def solution() = {
      import java.io.InputStream
      abstract class IterableInputStream extends InputStream with Iterable[Byte] {
        def iterator = new Iterator[Byte] {
          def hasNext = available > 0
          def next = read.asInstanceOf[Byte]
        }
      }
    }
  }

}
