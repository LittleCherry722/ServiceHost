import scala.collection.JavaConversions.propertiesAsScalaMap

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
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
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

      // your solution for task 1 here
      val myCounter = new Counter
      myCounter.increment() // value reach max
      println(myCounter.current)
      myCounter.increment() // reset to 0
      println(myCounter.current)

    }

    class Counter {
      private var value = Int.MaxValue - 1
      def increment() { if (value < Int.MaxValue) value += 1 else value = 0 }
      def current() = value
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val myBankAccount = new BankAccount
      myBankAccount.deposit(100)
      println(myBankAccount.getBalance)
      myBankAccount.withdraw(50)
      println(myBankAccount.getBalance)

    }

    class BankAccount {
      private var balance = 0.0

      def deposit(value: Double) {
        if (value > 0) balance += value
      }

      def withdraw(value: Double) {
        if (balance >= value) balance -= value
      }

      def getBalance = balance

    }

  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      val myTime1 = new Time(2, 15)
      println("time 1 is " + myTime1.getHours + " hours " + myTime1.getMinutes + " minutes")
      val myTime2 = new Time(2, 16)
      println("time 2 is " + myTime2.getHours + " hours " + myTime2.getMinutes + " minutes")
      println("is time 1 before time 2 ? " + myTime1.before(myTime2))

    }

    class Time() {
      private var hours: Int = 0
      private var minutes: Int = 0

      def this(hours: Int, minutes: Int) {
        this()
        if (0 <= hours && hours < 24) this.hours = hours
        if (0 <= minutes && minutes < 60) this.minutes = minutes
      }

      // getters
      def getHours = hours
      def getMinutes = minutes

      def before(other: Time): Boolean = {
        if (this.hours < other.hours) true
        else if (this.hours == other.hours && this.minutes < other.minutes)
          true
        else false
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val myTime1 = new Time(2, 15)
      println("time 1 is " + myTime1.getMinutes + " minutes")
      val myTime2 = new Time(2, 16)
      println("time 2 is " + myTime2.getMinutes + " minutes")
      println("is time 1 before time 2 ? " + myTime1.before(myTime2))

    }

    class Time() {
      private var minutes: Int = 0

      def this(hours: Int, minutes: Int) {
        this()
        if (0 <= hours && hours < 24 && 0 <= minutes && minutes < 60)
          this.minutes = hours * 60 + minutes
      }

      // getter
      def getMinutes = minutes

      def before(other: Time): Boolean = {
        if (this.minutes < other.minutes) true
        else false

      }
    }

  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      val myPerson = new Person(-5)
      println("age is " + myPerson.showAge)

    }

    class Person(private var age: Int) {
      if (age < 0) age = 0 else this.age = age
      def showAge = age
    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
      val car1 = new Car("BMW", "X1")
      println("Manufacturer: " + car1.showManu + " | Model: " + car1.showModel + " | Year: " +
          car1.showYear + " | License: " + car1.license)
      val car2 = new Car("BMW", "X1", 2012)
      println("Manufacturer: " + car2.showManu + " | Model: " + car2.showModel + " | Year: " +
          car2.showYear + " | License: " + car2.license)
      val car3 = new Car("BMW", "X1","SBA5526")
      println("Manufacturer: " + car3.showManu + " | Model: " + car3.showModel + " | Year: " +
          car3.showYear + " | License: " + car3.license)
      val car4 = new Car("BMW", "X1", 2012, "SBA5526")
      println("Manufacturer: " + car4.showManu + " | Model: " + car4.showModel + " | Year: " +
          car4.showYear + " | License: " + car4.license)

    }

    class Car(private var manu: String = "", private var model: String = "") {
      private var year: Int = -1
      var license: String = ""
      def showManu = manu
      def showModel = model
      def showYear = year
      def this(manu: String, model: String, year: Int) {
        this(manu, model)
        this.year = year
      }
      def this(manu: String, model: String, license: String) {
        this(manu, model)
        this.license = license
      }
      def this(manu: String, model: String, year: Int, license: String) {
        this(manu, model)
        this.year = year
        this.license = license
      }
    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      println("The form in the book is better, because it's shorter and more flexible")

    }
    
    class Employee() {
      var name : String = "John Q. Public"
      var salary : Double = 0.0
      def this(name: String, salary : Double) {
        this()
        this.name = name
        this.salary = 0.0
      }
    }
  }

}