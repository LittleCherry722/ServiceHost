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

      trait RectangleLike{
    	  
        self: java.awt.geom.RectangularShape =>
        def translate(dx: Double, dy: Double) {
          setFrame(getX + dx, getY + dy, getWidth, getHeight)
        }
        def grow(h: Double, v: Double) {
          setFrame(getX, getY, getWidth * v, getHeight * h)
        }
      }
    	  
      val rec = new java.awt.geom.Ellipse2D.Double(0, 5, 10, 15) with RectangleLike
      println(rec.getX()+" - "+rec.getY())
        
      rec.translate(5, -5)
      println(rec.getX()+" - "+rec.getY())
        
      rec.grow(5, 5)
      println(rec.getX()+" - "+rec.getY())
    	  
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
        class OrderedPoint(x: Int, y: Int) extends java.awt.Point with scala.math.Ordered[OrderedPoint]{
    	  
          def compare(p: OrderedPoint): Int = {
            if (this.x < p.x){
              return 1
            }
            else if (this.x == p.x && y < p.y){
              return 1
            }
            else{
              -1 
            }
          }
    	}
    	
    	val p1 = new OrderedPoint(0, 1)
        val p2 = new OrderedPoint(2, 3)
      
        println("compare: " + p1.compare(p2))
    }
  }


  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      trait CryptoLogger {
        val key: Int = 3;
        
        def log(msg: String) {
          for (c <- msg) yield (c+key).toChar
          val msg_char = msg.toCharArray();
          val msg_char_encoded = for (i <- msg_char) yield (i + key).toChar
          
          println(msg_char_encoded.mkString)
        }
      }
      
      class temp(msg: String) extends CryptoLogger{
    	  log(msg)
      }
      
      var cl = new temp("abcdefg")

    }
  }


  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
    	println("Figure 10-4 shows an example of diamond inheritance simular to figure 10-1.")
    	println("Diamond inheritance is not allowed in java and neither in scala. But it could be implemented with multiple traits.")
    }
  }



  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
        def next: Byte = this.read().asInstanceOf[Byte]
        def hasNext: Boolean = this.available >= 1
        def read(): Int = 0 
        def iterator:Iterator[Byte] = null
      }

    }
  }

}
