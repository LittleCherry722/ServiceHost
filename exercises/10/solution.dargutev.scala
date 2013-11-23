import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap
import java.awt.geom.Rectangle2D
import java.awt.Point


object Solution extends App {
  for (a <- args.reverse) {
    print(a)
    print(" ")
  }
  println()

  // execute all tasks
  Tasks.execute()

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
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
      egg.translate(10 - 10)
      egg.grow(10, 20)
    }
  }
  
  
  trait RectangleLike {
  def translate(a: Int) {

  }
  def grow(a: Int, b: Int) {

  }
}

  new Task("Task 2") {
    def solution() = {
      val p = new OrderedPoint(1, 2)
      val b = new OrderedPoint(2, 3)
      println(p.compare(b))

    }
  }
  
  class OrderedPoint(var p: Int, var q: Int) extends java.awt.Point with Ordered[Point] {
  def compare(b: Point): Int = {
    if (p < b.x) -1
    else if (p == b.x) {
      if (q < b.y) -1
      else if (q == b.y) 0
      else 1
    } else 1
  }
}


  new Task("Task 4") {
    def solution() = {
      val s = new SavingAccount()
      val b = new SavingAccount(-5)
      s.withdraw(30);
      b.withdraw(54)
    }
  }

  trait Logger {
    def log(msg: String) {
      println(msg)
    }
  }
  abstract class Account {
    def withdraw(amount: Int)
  }
  class SavingAccount(val key:Int = 3) extends Account with CryptoLogger {
    def withdraw(amount: Int) {
      log("withdrawn" + amount)
    }
  }
  trait CryptoLogger extends Logger {
    val key:Int;
   override def log(msg: String) {
      super.log("enrypted with key " + key)
    }
  }

  new Task("Task 6") {
    def solution() = {
      // it is not possible because we have multiple inheritance in the case with JContainer
    }
  }

  trait Component {

  }
  trait JComponent extends Component {
  }
  trait Container extends Component {

  }
  trait JContainer extends Container with JComponent {
  }
  class JPanel extends JContainer {

  }
  class JButton extends JComponent {
  }
  
  
   new Task("Task 10") {
    def solution() = {
    }
  }
   class IterableInputStream extends java.io.InputStream with Iterable[Byte]{
     def read():Int={
       0
     }
     def iterator():Iterator[Byte]={
       null
     }
   }
}
