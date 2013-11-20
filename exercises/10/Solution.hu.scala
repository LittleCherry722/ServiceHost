/**
 * Please don't run this file directly. If you want to run it,
 * please create individual files.
 */

// task chapter10_1 begin

import java.awt.geom.Ellipse2D
trait RectangleLike {
  this: Ellipse2D.Double =>
  def translate(x: Double, y: Double) = {
    this.x = x
    this.y = y
  }
  def grow(h: Double, w: Double) = {
    this.height += h
    this.width += w
  }
}
object Chapter10_1 extends App {
  val box = new Ellipse2D.Double(10, 10, 20, 20) with RectangleLike
  println("x = " + box.getX + " y = " + box.getY)
  box.translate(10, -10)
  println("x = " + box.getX + " y = " + box.getY)
  box.grow(10, 20)
  println("h = " + box.getHeight() + " w = " + box.getWidth())
}
//task chapter10_1 end

//task chapter10_2 begin

import scala.Array.canBuildFrom
class OrderedPoint extends java.awt.Point with scala.math.Ordered[OrderedPoint] {
  def compare(that: OrderedPoint): Int = {
    if ((this.x < that.x) || (this.x == that.x && this.y < that.y)) -1
    else if (this.x == that.x && this.y == that.y) 0
    else 1
  }
}
object Chapter10_2 extends App {
  val firstPoint = new OrderedPoint
  firstPoint.x = 19
  firstPoint.y = 18
  val secondPoint = new OrderedPoint
  secondPoint.x = -10
  secondPoint.y = -20
  print(firstPoint.compare(secondPoint))
}
//task chapter10_2 end

//task chapter10_4 begin

trait CryptoLogger {
  def caesarCipher(s: String, key: Int = 3): String
}
class Logger extends CryptoLogger {
  def caesarCipher(s: String, key: Int): String = {
    for (elem <- s)
      yield if (key > 0) {
      if (elem.toUpper > 90 - key) //If elem is X, Y, Z or x,y,z
        (elem.toInt + key - 26).toChar
      else (elem.toInt + key).toChar
    } else {
      if (elem.toUpper < 65 - key) //If elem is A,B,C or a,b,c
        (elem.toInt + key + 26).toChar
      else (elem.toInt + key).toChar
    }
  }
}
object Chapter10_4 extends App {
  val str = "HelloWorld"
  val firstLogger = new Logger
  val secondLogger = new Logger
  println(str)
  println("key is 3: " + firstLogger.caesarCipher(str))
  print("key is -3: " + secondLogger.caesarCipher(str, -3))
}
//task chapter10_4 end

//task chapter10_6 begin

object Chapter10_6 extends App {
  print("Java doesn't allow a class to " +
    "inherit from muliple superclass. " +
    "Scala can use trait to execute this design.")
}
//task chapter10_6 end

//task chapter10_10 begin

import java.io.InputStream

class IterableInputStream extends java.io.InputStream with Iterable[Byte]{
  def read(): Int = 0 
  def iterator:Iterator[Byte] = null
}
object Chapter10_10 extends App {
	val artical = new IterableInputStream
	artical.read
	artical.iterator
}
//task chapter10_10 end