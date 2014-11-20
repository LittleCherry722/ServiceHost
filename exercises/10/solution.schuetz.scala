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
      trait RectangleLike {
        this: java.awt.geom.Ellipse2D =>
        def translate(x: Double, y: Double) {
          setFrame(getX() + x, getY() + y, getWidth(), getHeight())
        }
        
        def grow(w: Double, h: Double) {
          setFrame(getX(), getY(), getWidth() + w, getHeight() + h)
        }
        
      }
      
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)
      
      println(egg.getX()  + ", " + egg.getY()  + ", " + egg.getWidth()  + ", " + egg.getHeight())

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      
      class OrderedPoint(x: Integer, y: Integer) extends java.awt.Point(x, y) with scala.math.Ordered[OrderedPoint]{
        override def compare(point: OrderedPoint): Int = {
          if ((x < point.x) || ((x == point.x) && y < point.y) )
            1
          else
            -1
        }
      }
      
      val op1 = new OrderedPoint(2,3)
      val op2 = new OrderedPoint(2,5)
      println (op1.compare(op2))

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      trait CryptoLogger {
        val key: Integer = 3
        def encrypt(message: String) = {
          var crypto = "" 
          for (char <- message.toCharArray()) {
            if (('a' to 'z').contains(char)) crypto += ((char + key - 'a') % 26 + 'a').toChar
            else if (('A' to 'Z').contains(char)) crypto += ((char + key - 'A') % 26 + 'A').toChar
          }
            
          crypto.mkString
        }
      }
      
      class StdCrypto() extends CryptoLogger
      class CustomCrypto(shift: Integer) extends CryptoLogger {
        override val key = shift
      }
      
      val stdCrypto = new StdCrypto()
      val customCrypto = new CustomCrypto(5)
      println("Hallo")
      println(stdCrypto.encrypt("Hallo"))
      println(customCrypto.encrypt("Hallo"))
      
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here

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

      // your solution for task 10 here
      class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        override def read(): Int = {0}
        override def iterator: Iterator[Byte] = {null}
        
      }

    }
  }

}
