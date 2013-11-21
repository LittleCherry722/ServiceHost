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

    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount {
        private var privateBalance = 0
        def balance = privateBalance
        def deposit(value: Int) { privateBalance += value }
        def withdraw(value: Int) { privateBalance -= value }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Time(val hours: Int, val minutes: Int) {
        def before(other: Time) = other.hours > hours || (other.hours == hours && other.minutes > minutes)
      }
      val t1 = new Time(12,31)
      val t2 = new Time(23,14)
      println(t1)
      println(t2)
      println(t1.before(t2))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Time(val totalMinutes: Int) {
        def this(hours: Int, minutes: Int) {
          this(minutes + 60 * hours)
        }
        
        def hours = totalMinutes / 60
        def before(other: Time) = other.totalMinutes > totalMinutes
      }
      val t1 = new Time(12,31)
      val t2 = new Time(23,14)
      println(t1)
      println(t2)
      println(t1.before(t2))
    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(var age: Int) {
        if (age < 0) age = 0
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      class Car(val manufacturer: String, val model: String, val year: Int, var licensePlate: String) {
        // Choosen as primary constructor because it has all fields
      
        def this(manufacturer: String, model: String, year: Int) {
          this(manufacturer, model, year, "")
        }
        def this(manufacturer: String, model: String, licensePlate: String) {
          this(manufacturer, model, -1, licensePlate)
        }
        def this(manufacturer: String, model: String) {
          this(manufacturer, model, -1)
        }
      }
    }
  }

  new Task("Task 10") {
    def solution() = {
      class Employee() {
        var name: String = "John Q. Public"
        var salary: Double = 0.0
        
        def this(name: String, salary: Double) {
          this()
          this.name = name
          this.salary = salary
        }
        
        // I prefer the other constructor as primary constructor, as it saves much typework
      }
    }
  }

}
