import scala.Array.canBuildFrom

object Chapter3 extends App {
  Tasks3.execute()
}

abstract class Task3(val name: String) {
  Tasks3 add this
  def solution()
  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks3 {
  private var tasks3 = Seq[Task3]()
  def add(t: Task3) = { tasks3 :+= t }
  def execute() = { tasks3.foreach((t: Task3) => { t.execute() }) }
  def execute(name: String) = { tasks3.filter(_.name == name).head.execute() }
}
object Tasks3 extends Tasks3 {
  new Task3("Task 3-2") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      swap(a)
      for (i <- a) print(i + " ")
    }
    def swap(n: Array[Int]) = {
      for (i <- 1 until (n.length, 2)) {
        var t = 0
        t = n(i - 1)
        n(i - 1) = n(i)
        n(i) = t
      }
    }
  }

  new Task3("Task 3-3") {
    def solution() = {
      val a = Array(1, 2, 3, 4, 5)
      val result = for (i <- 0 until a.length)
        yield if (i % 2 == 0 && i + 1 < a.length) a(i + 1)
      else if (i % 2 == 0 && i + 1 >= a.length) a(i)
      else a(i - 1)
      for (elem <- result) print(elem + " ")
    }
  }

  new Task3("Task 3-4") {
    def solution() = {
      val original = Array(25, 89, -12, -34, 67, 0, 0, 78, -2, 28)
      val resultp = for (elem <- original if elem > 0) yield elem
      val resultn = for (elem <- original if elem <= 0) yield elem
      val result = resultp.++(resultn)
      for (i <- result) print(i + " ")

    }
  }

  new Task3("Task 3-7") {
    def solution() = {
      val originalArray = Array(11, 3, 5, -3, 0, 0, -21, 3, 40, -1, 5)
      val a = originalArray.toBuffer
      val newArray = a.distinct.toArray
      for (elem <- newArray) print(elem + " ")
    }
  }

  new Task3("Task 3-8") {
    def solution() = {
      val oldArray = Array(-34, -90, -10, -1, 2, -3, 4, -5, 6, -7, 8, 0)
      var newArray = oldArray.toBuffer
      var n = oldArray.length
      var isFirst = true
      var m = 0
      for (i <- 0 until n) {
        if (newArray(i) >= 0) {
          isFirst = false
        } else if (isFirst) {
          m += 1
        } else {
          m += 1
          var t = oldArray(i)
          newArray.remove(i)
          newArray.insert(0, t)
        }
      }
      for (elem <- newArray.toArray.drop(m)) print(elem + " ")

    }
  }
}