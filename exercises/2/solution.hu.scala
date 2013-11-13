import scala.Array.canBuildFrom

object Chapter2 extends App {
  Tasks2.execute()
}

abstract class Task2(val name: String) {
  Tasks2 add this
  def solution()
  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks2 {
  private var tasks2 = Seq[Task2]()
  def add(t: Task2) = { tasks2 :+= t }
  def execute() = { tasks2.foreach((t: Task2) => { t.execute() }) }
  def execute(name: String) = { tasks2.filter(_.name == name).head.execute() }
}

object Tasks2 extends Tasks2 {

  new Task2("Task 2-1") {
    def solution() = {
      println("Input a number: ")
      var number = readInt()
      print(signum(number))
    }
    def signum(x: Int) = { if (x > 0) 1 else if (x == 0) 0 else -1 }
  }

  new Task2("Task 2-2") {
    def solution() = { print("the value of an empty block expression {} is (), its type is Unit.") }
  }

  new Task2("Task 2-4") {
    def solution() = fac(10)
    def fac(i: Int) = {
      for (j <- 0 to i) {
        print(i - j + " ")
      }
    }
  }

  new Task2("Task 2-5") {
    def solution() = {
      print("Input a number: ")
      var t = readInt()
      countdown(t)
    }
    def countdown(n: Int) = {
      for (i <- 0 to n reverse)
        print(i + " ")
    }
  }

  new Task2("Task 2-10") {
    def solution() = {
      print("Input x : ")
      var a = readDouble()
      print("Input n: ")
      var b = readInt()
      print(exp(a, b))
    }
    def exp(x: Double, n: Int): Double = {
      if (n == 0) 1
      else if (n > 0 && n % 2 == 0) exp(x, n / 2) * exp(x, n / 2)
      else if (n > 0 && n % 2 == 1) x * exp(x, n - 1)
      else 1 / exp(x, -n)
    }
  }
}