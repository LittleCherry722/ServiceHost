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
  
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10, -10)
      egg.grow(10, 20)
      
      println(egg.getX())
      println(egg.getY())
      println(egg.getWidth())
      println(egg.getHeight())

    }
  }

  new Task("Task 2") {
    def solution() = {

      import java.awt.Point
	  import scala.math.Ordered
  
	  class OrderedPoint(x: Int, y: Int) extends Point(x, y) with Ordered[Point] {
	   def compare(that: Point): Int = {
		   if (this.x > that.x || (this.x == that.x && this.y > that.y)) 1
	       else if (this.x < that.x || (this.x == that.x && this.y < that.y)) -1
	       else 0
	   	}
       }
	   val a = new OrderedPoint(3, 6)
	   val b = new OrderedPoint(6, 9)
	   val c = new OrderedPoint(9, 3)
	     
	   println(a.compare(b))
	   println(b.compare(c))
	   print(c.compare(a))

    }
  }


  new Task("Task 4") {
    def solution() = {

     class CryptoLogger(val key: Int) {
        def encode(msg: String) = (for (c <- msg) yield (c + key).toChar )
      }
      
 
 	 val FirstMsg = new CryptoLogger(3).encode("Secret Message")
     val SecondMsg = new CryptoLogger(-3).encode("Secret Message")
      
     println(FirstMsg)
     println(SecondMsg)

    }
  }

 

  new Task("Task 6") {
    def solution() = {

      print("Java Does not Allow Multiple inheritance")

    }
  }


  new Task("Task 10") {
    def solution() = {

      import java.io.InputStream
      import java.io.FileInputStream

      trait IterableInputStream extends InputStream with Iterable[Byte] {
  
   	def iterator = new Iterator[Byte] {
	   def hasNext = if (available > 0) true 
		   			else false
   
	   def next = read().asInstanceOf[Byte]
  	}
      }
	
    }
  }

}
