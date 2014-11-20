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
      val m = Map("bubblegum" -> 1.99, "car" -> 20000.0, "boat" -> 50000.0, "house" -> 100000.0)
      val m2 = for ((key, value) <- m) yield (key, value * 0.9)
      for((k,v) <- m) println(k+" -> "+v)
      for((k,v) <- m2) println(k+" -> "+v)
    }
  }

  new Task("Task 6") {
    def solution() = {
        import java.util.Calendar._
        val m = scala.collection.mutable.LinkedHashMap("Monday" -> MONDAY)
        m += ("Tuesday" -> TUESDAY)
        m += ("Wednesday" -> WEDNESDAY)
        m += ("Thursday" -> THURSDAY)
        m += ("Friday" -> FRIDAY)
        m += ("Saturday" -> SATURDAY)
        m += ("Sunday" -> SUNDAY)
        for((k,v) <- m) println(k+" -> "+v)
    }
  }

  new Task("Task 7") {
    def solution() = {
      import scala.collection.JavaConversions.propertiesAsScalaMap
      val props: scala.collection.Map[String,String] = System.getProperties()
      val longest = props.keySet.maxBy(_.length).length + 5
      for ((k,v) <- props) println(k + " " * (longest - k.length) + " | " + v)
    }
  }

  new Task("Task 8") {
    def solution() = {
      // def minmax(values: Array[Int]) = { (values.min, values.max) }
      // but lets pretend we work on really big arrays:
      def minmax(values: Array[Int]) = {
        var min = scala.Int.MaxValue
        var max = scala.Int.MinValue
        for (a <- values) {
          min = math.min(min, a)
          max = math.max(max, a)
        }
        (min, max)
      }
      println(minmax(Array(5, 4, 2, 3, 9, 1, 0)).toString)
    }
  }

  new Task("Task 9") {
    def solution() = {
      // similar to 8, we could do 3x count() for a short solution
      def lteqgt(values: Array[Int], v: Int) = {
        var ltv, eqv, gtv = 0
        for (a <- values) {
          if (a == v) eqv += 1
          else if (a < v) ltv += 1
          else gtv += 1
        }
        (ltv, eqv, gtv)
      }
      println(lteqgt(Array(5, 4, 2, 3, 9, 1, 0, 11, 5, 7), 5).toString)
    }
  }
}
