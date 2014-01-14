import scala.collection.JavaConverters._

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
        private var value = 0 // You must initialize the field
        def increment() { if (value < Integer.MAX_VALUE) value += 1 } // Methods are public by default
        def current() = value
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount {
        private var balance = 0.0
        def deposit(amount: Double) { if (amount > 0) balance += amount }
        def withdraw(amount: Double) { if (amount > 0 && balance >= amount) balance -= amount }
        def current() = balance
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Time(val hrs: Int, val min: Int) {
        require(hrs >= 0 && hrs <= 24
          && min >= 0 && min <= 60)
        def before(other: Time) = {
          if (hrs > other.hrs)
            false
          else if (hrs == other.hrs) (min < other.min)
          else true
        }
        override def toString() = { hrs + "h" + min }
      }

      val nineH = new Time(9, 20)
      val eightH = new Time(8, 44)
      println(nineH)
      println(eightH)
      print("eightH.before(nineH) :" + eightH.before(nineH))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Time(hrs: Int, min: Int) {
        require(hrs >= 0 && hrs <= 24
          && min >= 0 && min <= 60)
        val mins = hrs * 60 + min; //the number of minutes since midnight
        def before(other: Time) = {
          mins < other.mins;
        }
        override def toString() = { mins.toString }
      }
      val nineH = new Time(9, 20)
      val eightH = new Time(8, 44)
      println(nineH)
      println(eightH)
      print("eightH.before(nineH) :" + eightH.before(nineH))
    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(var age: Int) { // This is Java
        if (age <= 0) {
          age = 0;
          println("Negative Age reset to 0")
        }
      }
      val person1 = new Person(-1);
      println(person1.age);
    }

  }

  new Task("Task 8") {
    def solution() = {
      class Car(val manufacturer: String, val model: String, val mYear: Int = -1, var licenseNr: String = "") {

      }
      println("this constructor proviate all possible parameter to be called from others constructors," +
        "and conditions can also be specified")
    }
  }

  new Task("Task 10") {
    def solution() = {
      class Employee(val name: String = "John Q. Public", var salary: Double = 0.0) {
      }
    }
  }
}
