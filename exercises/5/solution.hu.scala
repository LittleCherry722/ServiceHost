import scala.Array.canBuildFrom

object Chapter5 extends App {
  Tasks5.execute()
}

abstract class Task5(val name: String) {
  Tasks5 add this
  def solution()
  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks5 {
  private var tasks5 = Seq[Task5]()
  def add(t: Task5) = { tasks5 :+= t }
  def execute() = { tasks5.foreach((t: Task5) => { t.execute() }) }
  def execute(name: String) = { tasks5.filter(_.name == name).head.execute() }
}

object Tasks5 extends Tasks5 {
  new Task5("task 5-1") {
    def solution() = {
      class Counter {
        private var value = Int.MaxValue
        def increment() { if (value < Int.MaxValue) value + 1 else value }
        def current() = value
      }

      val myCounter = new Counter
      print(myCounter.current())
    }
  }

  new Task5("task 5-2") {
    def solution() = {
      class BankAccount {
        var money = 0
        private var property = 100
        def deposit() = {
          property += money
          val balance = property
          print("Your currently balance is " + balance + " $")
        }
        def withdraw() = {
          if (property >= money) {
            property -= money
            val balance = property
            print("Your currently balance is " + balance + " $")
          } else print("Insufficient balance! Your currently balance is " + property + " $")
        }
      }
      val myCount = new BankAccount
      print("please input deposit or withdraw ?")
      val choice = readLine()
      print("please input money:")
      myCount.money = readInt()
      if (choice.equals("deposit")) myCount.deposit()
      else myCount.withdraw()
    }
  }

  new Task5("task 5-3") {
    def solution() = {
      class Time(val hours: Int, val minutes: Int) {
        def before(other: Time): Boolean = {
          hours < other.hours || (hours == other.hours && minutes < other.minutes)
        }
        def timeRepresentation = { hours + ":" + minutes }
      }
      val currentTime = new Time(10, 20)
      println(currentTime.before(new Time(11, 30)))
      print(currentTime.timeRepresentation)
    }
  }

  new Task5("task 5-4") {
    def solution() = {
      class Time(val hours: Int, val minutes: Int) {
        def before(other: Time): Boolean = {
          hours < other.hours || (hours == other.hours && minutes < other.minutes)
        }
        def timeRepresentation = { hours * 60 + minutes }
      }
      val newTime = new Time(12, 20)
      println(newTime.before(new Time(11, 30)))
      print(newTime.timeRepresentation + "  minutes")
    }
  }

  new Task5("task 5-6") {
    def solution() = {
      class Person(private var age: Int) {
        age = if (age < 0) 0 else age
        def newAge = age
      }

      val child = new Person(3)
      print(child.newAge)

    }
  }

  new Task5("task 5-8") {
    def solution() = {
      class Car(manufacturer: String, modelName: String, modelYear: Int = -1, licensePlate: String = "") {
      }
    }
  }

  new Task5("task 5-10") {
    def solution() = {
      class Employee { //It is very easy.
        val name: String = "John Q.public"
        var salary: Double = 0.0
      }
    }
  }
}