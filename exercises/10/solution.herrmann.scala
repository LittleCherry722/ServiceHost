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



object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      import java.awt.geom.Ellipse2D
    	val egg = new Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
    	egg.translate(10, -10)
    	egg.grow(10, 20)
    	
    	trait RectangleLike {
        def getX(): Double
        def getY(): Double
        def getHeight(): Double
        def getWidth(): Double
        def setFrame(x: Double, y: Double, w: Double, h: Double)
        
        def translate(x: Double, y: Double) {
        	val newX = getX() + x
        	val newY = getY() + y
        	val width = getWidth()
        	val height = getHeight()
        	setFrame(newX, newY, width, height)
        }
        
        def grow(w: Double, h: Double) {
        	val newX = getX() - w
        	val newY = getY() - h
        	val newWidth = getWidth() + 2 * w
        	val newHeight = getHeight() + 2 * h
        	setFrame(newX, newY, newWidth, newHeight)
        }
      	}      
    			
    }
  }

  new Task("Task 2") {
    import java.awt.Point
    def solution() = {
    	class OrderedPoint(x: Int, y: Int) extends Point(x, y) with Ordered[Point] {

    		def compare(p: Point): Int = if ((this.x < p.x) || (this.x == p.x && this.y < p.y)) -1
    		else if (this.x == p.x && this.y == p.y) 0
    		else 1
    	}      

    }
  }

   new Task("Task 4") {
    import scala.util.logging.{ConsoleLogger, Logged}
    def solution() = {
    	class CryptoLogger extends Logged {
    	  var key = 3
    	  
    	  def this(k: Int){
    	    this()
    	    this.key = k
    	  }
    		
    		override def log(msg: String) { super.log(encrypt(msg)) }
    		
    		private def encrypt(msg: String): String = msg.map(c => (c + key).toChar)
    }      

    }
  }

  new Task("Task 6") {
    def solution() = {
// In Java a class can not inherit from two super classes(JComponent and Container)
// ???     

    }
  }

  new Task("Task 10") {
    import java.io.{ByteArrayInputStream, InputStream}
    def solution() = {
      class IterableInputStream extends InputStream with Iterable[Byte] {
    	
      }
    }

  }

}
