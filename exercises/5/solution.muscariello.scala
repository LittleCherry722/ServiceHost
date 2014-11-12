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
      class Counter {
        import scala.math.min
        private var value = 0
        def increment() { value += (if (current == Int.MaxValue) 0 else 1) }
        def current = value
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount {
        private var privateBalance = 0.0
        def balance = privateBalance
        def withdraw(amount: Double) {
          privateBalance -= amount
        }
        def deposit(amount: Double) {
          privateBalance += amount
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Time(val hours: Int = 0, val minutes: Int = 0) {
        def before(other: Time): Boolean = {
          if (hours < other.hours) true
          else if (hours == other.hours && minutes < other.minutes) true else false
        }
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Time(hrs: Int = 0, mins: Int = 0) {
        private val minutesSinceMidnight = hrs * 60 + mins
        def minutes: Int = {
          minutesSinceMidnight % 60
        }
        def hours: Int = {
          minutesSinceMidnight / 60
        }
        def before(other: Time): Boolean = {
          minutesSinceMidnight < other.minutesSinceMidnight
        }
      }
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(agep: Int = 0) {
        val age = if (agep < 0) 0 else agep
      }
      val person = new Person(-5)
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {
      class Car(val manufacturer: String, val model: String, val modelYear: Int, var licensePlate: String) {
        def this(manufacturer: String, model: String) {
          this(manufacturer, model, -1, "")
        }
        def this(manufacturer: String, model: String, modelYear: Int) {
          this(manufacturer, model, modelYear, "")
        }
        def this(manufacturer: String, model: String, licensePlate: String) {
          this(manufacturer, model, -1, licensePlate)
        }
      }
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      class Employee(nameParam: String, salaryParam: Double) {
        val name = nameParam
        var salary = salaryParam
        def this() { this("John Q. Public", 0.0) }
      }
    }
  }

}
