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
      val map = Map("Pencil" -> 4, "Phone" -> 250, "Water" -> 2)
      println(map)
      val cheaperMap = for ((k, v) <- map) yield (k -> v * 0.9)
      println(cheaperMap)
    }
  }

  new Task("Task 6") {
    def solution() = {
    }
  }

  new Task("Task 7") {
    def solution() = {
    }
  }

  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]) = {
        (values.min, values.max)
      }
      val values = Array(45,43,3,52)
      println(values.mkString("<",",",">"))
      println(minmax(values))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int) = {
        (values.count(_ < v), values.count(_ == v), values.count(_ > v))
      }
      val values = Array(45,43,3,52)
      println(values.mkString("<",",",">"))
      println(lteqgt(values,43))
    }
  }

}
