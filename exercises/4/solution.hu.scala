import scala.Array.canBuildFrom
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap

object Chapter4 extends App {
  Tasks4.execute()
}

abstract class Task4(val name: String) {
  Tasks4 add this
  def solution()
  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks4 {
  private var tasks4 = Seq[Task4]()
  def add(t: Task4) = { tasks4 :+= t }
  def execute() = { tasks4.foreach((t: Task4) => { t.execute() }) }
  def execute(name: String) = { tasks4.filter(_.name == name).head.execute() }
}

object Tasks4 extends Tasks4 {
  new Task4("task 4-1") {
    def solution() = {
      val gizmos = Map("Phone" -> 200, "Computer" -> 345, "X-box" -> 189, "Mp3" -> 100)
      val newGizmosPrice = for ((k, v) <- gizmos) yield (k, v * 0.9)
      for ((k, v) <- newGizmosPrice) print(k, v + " ")
    }
  }

  new Task4("task 4-6") {
    def solution() = {
      val week = scala.collection.mutable.LinkedHashMap[String, Int]()
      week += ("Monday" -> Calendar.MONDAY)
      week += ("Tuesday" -> Calendar.THURSDAY)
      week += ("Wednesday" -> Calendar.WEDNESDAY)
      week += ("Thursday" -> Calendar.THURSDAY)
      week += ("Friday" -> Calendar.FRIDAY)
      week += ("Saturday" -> Calendar.SATURDAY)
      week += ("Sunday" -> Calendar.SUNDAY)
      for ((k, v) <- week) print((k, v))
    }
  }
  new Task4("task 4-7") {
    def solution() = {
      val props: scala.collection.Map[String, String] = System.getProperties()
      val keyLength = for (k <- props.keys) yield k.length()
      val t = keyLength.max
      for ((k, v) <- props) {
        println(k + " " * (t - k.length()) + "|  " + v)
      }
    }
  }

  new Task4("task 4-8") {
    def solution() = {
      val number = Array(1, 4, 2, -4, 90, 0, -65, -2, 31)
      minmax(number)
    }
    def minmax(values: Array[Int]) = {
      val t = (values.min, values.max)
      print(t)
    }
  }

  new Task4("task 4-9") {
    def solution() {
      val number = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      lteqgt(number, 0)
    }
    def lteqgt(values: Array[Int], v: Int) = {
      var mix = 0
      var max = 0
      var equal = 0
      for (elem <- values) {
        if (elem > v) max += 1
        else if (elem == v) equal += 1
        else mix += 1
      }
      val counts = (mix, equal, max)
      print(counts)
    }

  }

}