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
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    import java.beans.PropertyChangeListener

    trait RectangleLike {
      this: java.awt.geom.RectangularShape =>

      def translate(dx: Double, dy: Double) {
        setFrame(getX() + dx, getY() + dy, getWidth(), getHeight())
      }

      def grow(dx: Double, dy: Double) {
        setFrame(getX() - dx / 2, getY() - dy / 2, getWidth() + dx,
          getHeight() + dy)
      }

      override def toString(): String =
        "x = " + getX() + ", y = " + getY() +
          ", width = " + getWidth() + ", height = " + getHeight()
    }

    def solution() = {
      println("Aufgabe 10.1")
      val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with 
        RectangleLike
      println(egg)
      egg.translate(10, -10)
      println(egg)
      egg.grow(10, 20)
      println(egg)
    }
  }

  new Task("Task 2") {
    import java.awt.Point
    class OrderedPoint(x: Int, y: Int) extends java.awt.Point(x, y)
      with scala.math.Ordered[Point] {

      def compare(other: Point): Int = {
        if (x < other.x || (x == other.x && y < other.y))
          -1
        else if (x > other.x || (x == other.x && y > other.y))
          1
        else
          0
      }

      override def toString(): String = "Point(" + x + ", " + y + ")"
    }

    def solution() = {
      val p1 = new OrderedPoint(5, 3)
      val p2 = new OrderedPoint(-4, 2)
      val p3 = new OrderedPoint(5, 6)
      val pList = List(p1, p2, p3)
      println(pList.sortWith(_ < _))
    }
  }

  new Task("Task 3") {
    def solution() = {
      // TODO
    }
  }

  new Task("Task 4") {
    trait Logger {
      def log(msg: String)
    }

    class CryptLogger(key: Int = 3, onlyLetters: Boolean = false) extends 
      Logger {

      def this(onlyLetters: Boolean) = this(3, onlyLetters)

      def log(msg: String) {
        println(encode(msg))
      }

      def encode(msg: String): String = {
        var encoding = ""

        if (onlyLetters) {
          val alphabet = 'a' to 'z'
          // we only care about lower case here
          for (c <- msg.toLowerCase()) {
            val p = alphabet.indexOf(c) + key
            if (p < 0)
              encoding += alphabet(p + alphabet.length)
            else
              encoding += alphabet(p)
          }
        } else {
          for (c <- msg) encoding += (c + key).toChar
        }

        encoding
      }
    }

    def solution() = {
      val logger = new CryptLogger
      val loggerString = new CryptLogger(true)
      val loggerDifferent = new CryptLogger(-3)
      val loggerDifferentString = new CryptLogger(-3, true)
      logger.log("Super Secret Message")
      loggerString.log("SuperSecretMessage")
      loggerDifferent.log("Super Secret Message")
      loggerDifferentString.log("SuperSecretMessage")
    }
  }

  new Task("Task 5") {
    import java.beans.PropertyChangeListener
    import java.beans.PropertyEditorSupport
    import java.beans.PropertyChangeEvent

    trait PropertyChangeSupport {
      var listeners: Map[String, Set[PropertyChangeListener]] = Map()

      def addPropertyChangeListener(propertyName: String,
        listener: PropertyChangeListener) {
        var currentListeners: Set[PropertyChangeListener] =
          listeners.getOrElse(propertyName, Set[PropertyChangeListener]())
        currentListeners += listener
        listeners += (propertyName -> currentListeners)
      }

      def addPropertyChangeListener(listener: PropertyChangeListener) {
        var currentListeners: Set[PropertyChangeListener] =
          listeners.getOrElse("allProperties", Set[PropertyChangeListener]())
        currentListeners += listener
        listeners += ("allProperties" -> currentListeners)
      }

      def firePropertyChanged(event: PropertyChangeEvent) {
        if (listeners.contains("allProperties")) {
          val currentListeners = listeners("allProperties")
          for (listener <- currentListeners)
            listener.propertyChange(event)
        }
      }

      def firePropertyChanged(propertyName: String, oldValue: Int, 
          newValue: Int) {
        if (listeners.contains(propertyName)) {
          val currentListeners = listeners(propertyName)
          for (listener <- currentListeners)
            listener.propertyChange(
              new PropertyChangeEvent(this, propertyName, oldValue, newValue))
        }
      }

      def firePropertyChanged(propertyName: String, oldValue: Boolean,
        newValue: Boolean) {
        if (listeners.contains(propertyName)) {
          val currentListeners = listeners(propertyName)
          for (listener <- currentListeners)
            listener.propertyChange(
              new PropertyChangeEvent(this, propertyName, oldValue, newValue))
        }
      }

      def firePropertyChanged(propertyName: String, oldValue: Any,
        newValue: Any) {
        if (listeners.contains(propertyName)) {
          val currentListeners = listeners(propertyName)
          for (listener <- currentListeners)
            listener.propertyChange(
              new PropertyChangeEvent(this, propertyName, oldValue, newValue))
        }
      }
    }

    class Listener extends PropertyChangeListener {
      def propertyChange(event: PropertyChangeEvent) {
        println("Property changed! " + event.getPropertyName() + ": " +
          event.getOldValue() + " --> " + event.getNewValue())
      }
    }

    class MyListener extends PropertyChangeListener {
      def propertyChange(event: PropertyChangeEvent) {
        println("Whoa something happend here!")
      }
    }

    def solution() = {
      val p = new java.awt.Point(0, 0) with PropertyChangeSupport {
        override def setLocation(newX: Int, newY: Int) {
          firePropertyChanged("setLocation", (x, y), (newX, newY))
          super.setLocation(newX, newY)
        }

        override def toString(): String = "(" + x + "," + y + ")"
      }
      p.addPropertyChangeListener("setLocation", new Listener)
      p.setLocation(2, 1)
      p.addPropertyChangeListener("setLocation", new MyListener)
      p.setLocation(8, 0)
    }
  }

  new Task("Task 6") {
    object zehnsechs {
      val dieAntwort =
        "Java unterstützt keine Mehrfachvererbung, deswegen ist möglich " +
        "JContainer sowohl von JComponent als auch von Container " +
        "erben zu lassen. \nIn Scala könnte man Container und Component als " +
        "Traits schreiben und JContainer mit beiden erweitern."
    }

    def solution() = {
      println(zehnsechs.dieAntwort)
    }
  }

  new Task("Task 7") {
    abstract class Dragon(val name: String) {
      def greeting: String
      def color: String

      def sayHi() { println(greeting) }

      def introduce() { print("My color is " + color + ". ") }
    }

    trait Flying {
      def fly() { println(" I can fly!") }
    }

    // Green dragons can always fly
    class GreenDragon(override val name: String) extends Dragon(name) with 
      Flying {
      val color = "green"

      val greeting = "Hi, my name is " + name
    }

    trait SpecialAbility {
      this: Dragon =>

      def specialAbility: String

      def tellMore()
    }

    // Pink dragons can't fly, but have the special ability to always feel pretty
    class PinkDragon(override val name: String) extends Dragon(name) with 
      SpecialAbility {
      val color = "pink"
      val specialAbility = " I'm feeling super pretty!"

      val greeting = "Hey, I'm " + name

      override def introduce() { print("") }

      override def tellMore() {
        super.introduce()
        print(specialAbility)
      }
    }

    def solution() = {
      val d1 = new GreenDragon("Alex")
      val d2 = new PinkDragon("Valerie")
      val d3 = new Dragon("Spike") with Flying with SpecialAbility {
        val greeting = "What do you want? I'm " + name + " the almighty!"
        val color = "black"
        val specialAbility = "burn your castle down"

        override def fly() { println("I will find you everywhere.") }

        def tellMore() { print("I'm going to " + specialAbility + "!!! ") }
      }

      println("Dragon 1 is speaking to you:")
      d1.sayHi
      d1.introduce
      d1.fly
      println()

      println("Dragon 2 is speaking to you:")
      d2.sayHi
      d2.introduce
      d2.tellMore
      println("\n")

      println("Dragon 3 is speaking to you:")
      d3.sayHi
      d3.introduce
      d3.fly
      d3.tellMore
      println()
    }
  }

  new Task("Task 8") {
    import java.io.FileInputStream
    import java.io.InputStream
    import java.io.File

    trait Buffering {
      this: FileInputStream =>

      var size = 50
      var buffer = new Array[Byte](size)
      var currentPosition = 0
      var readIn = 0

      override def read(): Int = {
        var returnVal = -1

        // load into buffer
        if (currentPosition == 0 ||
          (currentPosition >= size && available() >= size)) {
          readIn = size
          currentPosition = 0
          read(buffer, currentPosition, readIn)
        } else if (available() > 0 && available() < size) {
          readIn = available()
          currentPosition = 0
          read(buffer, currentPosition, readIn)
        }

        // if buffer is not empty return from buffer
        if (currentPosition < readIn) {
          returnVal = buffer(currentPosition)
          currentPosition += 1
        }

        returnVal
      }
    }

    def solution() = {
      val data = new FileInputStream("/home/sandra/erw_euklid.m") with 
        Buffering

      var pos: Int = 0
      while ({ pos = data.read; pos != -1 }) {
        print(pos.toChar)
      }
      data.close()
    }
  }

  new Task("Task 9") {
    import java.io.FileInputStream
    import java.io.InputStream
    import java.io.File

    trait Logged {
      def log(msg: String) {}
    }

    trait ConsoleLogger extends Logged {
      override def log(msg: String) {
        println(msg)
      }
    }

    trait TimestampLogger extends Logged {
      trait TimestampLogger extends Logger {
        abstract override def log(msg: String) {
          super.log(new java.util.Date() + " " + msg)
        }
      }
    }

    trait Bufferable extends Logged {
      this: FileInputStream =>

      var size = 50
      var buffer = new Array[Byte](size)
      var currentPosition = 0
      var readIn = 0

      override def read(): Int = {
        var returnVal = -1

        // load into buffer
        if (currentPosition == 0 ||
          (currentPosition >= size && available() >= size)) {
          readIn = size
          currentPosition = 0
          read(buffer, currentPosition, readIn)
          log("Read " + readIn + " characters into the buffer.")
        } else if (available() > 0 && available() < size) {
          readIn = available()
          currentPosition = 0
          read(buffer, currentPosition, readIn)
          log("End of Stream. Read last " + readIn + " characters into the buffer.")
        }

        // if buffer is not empty return from buffer
        if (currentPosition < readIn) {
          returnVal = buffer(currentPosition)
          currentPosition += 1
        }

        returnVal
      }
    }

    def solution() = {
      val data2 = new FileInputStream("/home/sandra/erw_euklid.m") with 
        Bufferable with ConsoleLogger with TimestampLogger

      var pos2 = 0
      while ({ pos2 = data2.read; pos2 != -1 }) {}
      data2.close()
    }
  }

  new Task("Task 10") {
    import java.io.FileInputStream
    import java.io.InputStream
    import java.io.File

    class IterableInputStream(val stream: InputStream)
      extends InputStream with Iterable[Byte] {

      def iterator: Iterator[Byte] = new Iterator[Byte] {
        def hasNext(): Boolean = stream.available() > 0

        def next(): Byte = read().toByte
      }

      def read(): Int = stream.read()
    }

    def solution() = {
      val data3 =
        new IterableInputStream(new FileInputStream("/home/sandra/erw_euklid.m"))

      for (byte <- data3) print(byte.toChar)

      data3.close()
    }
  }

}
