import java.awt.Point
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.InputStream
import scala.collection.mutable.Set
import scala.collection.mutable.Map
import java.io.FileInputStream
object Solution extends App {

  // execute all tasks
  Tasks.execute();
 
  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name+":");
    solution();
    println("\n");
  }
}

trait Tasks {
	private var tasks = Seq[Task]();
	def add (t: Task) = { tasks :+= t }
	def execute () = { tasks.foreach( (t:Task) => { t.execute(); } )  }
	def execute (name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  
  new Task("Task 1"){
	  def solution() = {
    
  		import java.awt.Rectangle;
  			  		
  		trait RectangleLike {
  		  this: java.awt.geom.RectangularShape =>
  		    /**
  		     * see http://docs.oracle.com/javase/6/docs/api/java/awt/Rectangle.html#grow(int, int)
  		     */
	  		  def grow(h: Int, v: Int) : Unit = {
	  		    setFrame(getX() - h, getY() - v, getWidth() + 2*h, getHeight() + 2*v)
	  		  }
	  		  /**
	  		   * see http://docs.oracle.com/javase/6/docs/api/java/awt/Rectangle.html#translate(int, int)
	  		   */
	  		  def translate(dx: Int, dy: Int) : Unit = {
	  		    setFrame(getX() + dx, getY() + dy, getWidth(), getHeight())
	  		  }
	  		  override def toString = "X=" + getX() + ", Y=" + getY() + ", W=" + getWidth() + ", H=" + getHeight()
  		}
  		
			val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
			
			println("Original: " + egg);
			
			egg.translate(10, -10)
			egg.grow(10, 20)
			
			println("Translated & grown: " + egg);
	    
	  }
  }
  
  new Task("Task 2"){
	  def solution() = {

  		class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x, y) with scala.math.Ordered[Point] {
  		  def compare(other: java.awt.Point) : Int = {
					if (x < other.x || (x == other.x && y < other.y))
						-1
					else if (x > other.x || (x == other.x && y > other.y))
						1
					else
						0 
  		  }
  		}
  		
  		val p1 = new OrderedPoint(7,9);
  		val p2 = new OrderedPoint(5,9);
  		val p3 = new OrderedPoint(6,9);
  		
  		val listOfPoints = List[OrderedPoint](p1,p2,p3);
  		  		
  		// sorted
  		println(listOfPoints.sort((e1, e2) => (e1 compare e2) < 0));
	  }
  }
  
  new Task("Task 3"){
	  def solution() = {

	  		// nothing to code
	    
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {

	  		class CryptoLogger {
	  		  
	  		  def cipher: Int = 3; // default
	  		  
			    def log(msg: String) {
			      println( Caesar.encode(msg, cipher) )
			    }
			    			    
			    object Caesar {
					  private val alphaU='A' to 'Z'
					  private val alphaL='a' to 'z'
					 
					  def encode(text:String, key:Int)=text.map{
					    case c if alphaU.contains(c) => rot(alphaU, c, key)
					    case c if alphaL.contains(c) => rot(alphaL, c, key)
					    case c => c
					  }
					  def decode(text:String, key:Int)=encode(text,-key)
					  private def rot(a:IndexedSeq[Char], c:Char, key:Int)=a((c-a.head+key+a.size)%a.size)
					}
	  		}
	    
	  		val c1 = new CryptoLogger
	  		
	  		c1.log ("ABC"); // = DEF
	  		
	  		val c2 = new CryptoLogger {
	  		  override val cipher = -3; // override value
	  		}
	  		
	  		c2.log ("DEF"); // = ABC
	  }
  }
  
  new Task("Task 5"){
	  def solution() = {

			import scala.collection.mutable.Map
			import scala.collection.mutable.Set
			import java.beans.PropertyChangeListener
			import java.beans.PropertyChangeEvent
			
			trait PropertyChangeSupport {
			  var listeners = Map[String, Set[PropertyChangeListener]]()
			   
			  def addPropertyChangeListener(propertyName: String, listener: PropertyChangeListener) {
			    var newListeners = Set[PropertyChangeListener]();
			    
			    if(listeners.contains(propertyName))
			      listeners(propertyName) += listener
		      else 
		        listeners += (propertyName -> Set[PropertyChangeListener](listener))
			  }
			  
			  def firePropertyChanged(propertyName: String, oldValue: Any, newValue: Any) {
			    if (listeners.contains(propertyName)) {
			      val listenerSet = listeners(propertyName)
			      listenerSet.foreach( (l: PropertyChangeListener) => 
			        	l.propertyChange( new PropertyChangeEvent(this, propertyName, oldValue, newValue) )
			      )
			    }
			  }
			}
			
			class Listener extends PropertyChangeListener {
			  def propertyChange(evt: PropertyChangeEvent) {
			    println(evt)
			  }
			}
  
		  val point = new java.awt.Point(0, 0) with PropertyChangeSupport {
		    override def setLocation(x: Double, y: Double) {
		      firePropertyChanged("setLocation", (getX(), getY()), (x, y))
		      super.setLocation(x, y)
		    }
		  }
		  
		  val listener = new Listener
		  point.addPropertyChangeListener("setLocation", listener)
		  point.setLocation(1, 1)
						
	  }
  }
  
  new Task("Task 6"){
	  def solution() = {

	  		/*
	  		 *  In Java kann JContainer nicht von JComponet and Container gleichzeitig erben.
	  		 *  In Scala ginge dieses über Traits
	  		 *  	  		 *  
	  		 */
	    
	  }
  } 
   
  new Task("Task 7"){
	  def solution() = {
	  		// I made this example... it's awesome
	  }
  }
    
  new Task("Task 8"){
	  def solution() = {
	    /*
	     * i skipped the logic for buffering since the Decorator pattern (trait layering) is the main task
	     */
	    trait Buffered extends java.io.InputStream {

			  var buffer = new Array[Byte](16)
			  var pos = 0
			  var copied = 0
			  
			  override def read : Int = {  
			    if ((pos == 0) || (available >= 16 && pos >= 16)) {
			      copied = 16
			      pos = 0
			      super.read(buffer, 0, copied)
			    }
			    else if (available > 0 && available < 16) {
			      copied = available
			      pos = 0
			      super.read(buffer, 0, copied)
			    }
			    
			    var ret = -1
			    if (pos < copied) {
			      ret = buffer(pos)
			      pos += 1
			    }
			    ret
			  }
	    }
	    var inputStream = new java.io.InputStream with Buffered
	  }
  }
    
  new Task("Task 9"){
	  def solution() = {

			import java.io.FileInputStream
			import java.io.InputStream
			import java.io.File
	
			trait Logger {
			  def log(msg: String)
			}
			
			trait TimestampLogger extends Logger {
			  abstract override def log(msg: String) {
			    super.log(new java.util.Date() + " " + msg)
			  }
			}
			
			trait ConsoleLogger extends Logger {
			  override def log(msg: String) {
			    println(msg)
			  }
			}
			
	    trait Buffered extends Logger {			 
	      this: java.io.InputStream =>
	      
				  var buffer = new Array[Byte](16)
				  var pos = 0
				  var copied = 0
				  
				  override def read : Int = {  
				    if ((pos == 0) || (available >= 16 && pos >= 16)) {
				      copied = 16
				      pos = 0
				      read(buffer, 0, copied)
				      log("read " + buffer)
				    }
				    else if (available > 0 && available < 16) {
				      copied = available
				      pos = 0
				      read(buffer, 0, copied)
				      log("read " + buffer)
				    }
				    
				    var ret = -1
				    if (pos < copied) {
				      ret = buffer(pos)
				      pos += 1
				    }
				    ret
				  }
				  
	    }
	    
	    var inputStream = new java.io.InputStream with Buffered with ConsoleLogger with TimestampLogger
	    
	  }
  }
    
  new Task("Task 10"){
	  def solution() = {
	    
			class IterableInputStream(val is: InputStream) extends InputStream with Iterable[Int] {
			  
			  def iterator: Iterator[Int] = new Iterator[Int] {
			    private var i = 1

			    def hasNext: Boolean = { is.available > 0 }
			    
			    def next() : Int = {
			      val n = is.read
			      i += 1
			      n
			    }
			  }
			  
			  def read = {
			    is.read
			  }
			}	    
			
			var inputStream = new IterableInputStream(new FileInputStream("c:\\test.txt"));
			
	  }
  }
  
}
