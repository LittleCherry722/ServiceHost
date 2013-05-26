import scala.collection.mutable.ArrayBuffer
import scala.util.logging.Logged

object Solution extends App {
	Tasks.execute();
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
	def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

object Tasks extends Tasks {
	new Task("Task 1") {
		def solution() = {
			trait RectangleLike extends java.awt.geom.Ellipse2D.Double {
				def translate(dx: Int, dy: Int) {
					x += dx
					y += dy
				}
				def grow(dx: Int, dy: Int) {
					width += dx
					height += dy
				}
			}
			val egg = new java.awt.geom.Ellipse2D.Double(5, 10, 20, 30) with RectangleLike
			egg.translate(10, -10)
			egg.grow(10, 20)
		}
	}

	new Task("Task 2") {
		def solution() = {
			import java.awt.Point
			class OrderedPoint(x: Int, y: Int) extends Point(x, y) with scala.math.Ordered[Point] {
				override def compare(that: Point): Int = {
					if (this.x != that.x) {
						this.x - that.x
					} else {
						this.y - that.y
					}
				}
			}
		}
	}

	new Task("Task 4") {
		def solution() = {
			class CryptoLogger extends Logged {
				val key = 3
				override def log(msg: String) {
					super.log(msg.map(c => (c + key).toChar))
				}
			}
		}
	}

	new Task("Task 6") {
		def solution() = {
			val answer = "In Java, a class cannot extend two other classes. In Scala we could do it with traits."
		}
	}

	new Task("Task 10") {
		def solution() = {
			class IterableInputStream extends java.io.InputStream with Iterable[Byte] {
				def read(): Int = 0
				def iterator: Iterator[Byte] = null
			}
		}
	}
}