
package chapter5

import scala.collection.mutable.ArrayBuffer
import sun.security.util.Length
import scala.math.BigInt

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
        private var value: BigInt = 0
        def increment() {
          value += 1
        }
        def current() = value
        def maxv() {
          value = Int.MaxValue
        }
      }
      var counter = new Counter
      counter.maxv
      counter.increment
      println(counter.current)

    }
  }

  new Task("Task 2") {
    def solution() = {
      class Bankaccount {
        private var balance: Double = 0
        def deposit(amount: Double) {
          println("Deposit: " + amount)
          println("Old Balance =  " + balance)
          balance += amount
          println("New Balance = " + balance + "\n")
        }
        def withdraw(amount: Double) {
          println("Withdraw: " + amount)
          println("Old Balance =  " + balance)
          balance -= amount
          println("New Balance = " + balance + "\n")
        }
        def currentbalance = balance
      }
      val myBankaccount = new Bankaccount
      myBankaccount.deposit(5000)
      myBankaccount.withdraw(1000)
    }
  }
  new Task("Task 3") {
    def solution() = {
      class Time {
        private var hours = 0
        private var minutes = 0

        def this(hrs: Int, min: Int) {
          this()
          if (hrs > 23 || hrs < 0) println("Illegal Time: Hours should be between 0 and 23") else hours = hrs
          if (min > 59 || min < 0) println("Illegal Time: Minutes should be between 0 and 59") else minutes = min
        }

        def before(other: Time): Boolean = {
          if (other.hours == hours) (other.minutes < minutes) else (other.hours < hours)
        }
        def currenthours = hours
        def currentminutes = minutes
      }

      val mytime = new Time(13, 37)
      val othertime = new Time(12, 11)
      println("Current Time: " + mytime.currenthours + ":" + mytime.currentminutes)
      println("Is " + othertime.currenthours + ":" + othertime.currentminutes + " before " + mytime.currenthours + ":" + mytime.currentminutes)
      println(mytime.before(othertime))

    }
  }
  new Task("Task 4") {
    def solution() = {
      class Time {
        private var time = 0

        def this(hrs: Int, min: Int) {
          this()
          if (hrs > 23 || hrs < 0) println("Illegal Time: Hours should be between 0 and 23") else time = hrs * 60
          if (min > 59 || min < 0) println("Illegal Time: Minutes should be between 0 and 59") else time += min
        }

        def before(other: Time): Boolean = {
          (other.time < time)
        }

        def currenthours = time / 60
        def currentminutes = time % 60
      }

      val mytime = new Time(13, 37)
      val othertime = new Time(12, 11)
      println("Current Time: " + mytime.currenthours + ":" + mytime.currentminutes)
      println("Is " + othertime.currenthours + ":" + othertime.currentminutes + " before " + mytime.currenthours + ":" + mytime.currentminutes)
      println(mytime.before(othertime))

    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(name: String, var age: Int) {
        if (age < 0) age = 0
        println("Person " + name + " (" + age + ")" + " created.")
      }
      val myperson = new Person("Arthur", -26)
    }
  }
  new Task("Task 8") {
    def solution() = {
      class Car(val manufacturer: String, val model: String) {
        private var year = -1
        var license = ""

        def this(manufacturer: String, model: String, year: Int) {
          this(manufacturer, model)
          this.year = year
        }

        def this(manufacturer: String, model: String, license: String) {
          this(manufacturer, model)
          this.license = license
        }
        def this(manufacturer: String, model: String, year: Int, license: String) {
          this(manufacturer, model, year)
          this.license = license
        }

        def ofyear = year
      }

      val mycar = new Car("BMW", "535d", 2013, "AB-CD 1234")
      println("My Car: " + mycar.manufacturer + " " + mycar.model + " of " + mycar.ofyear + " registered as \"" + mycar.license + "\"")
      println("\nPrimary Constructor contains manufacturer and model since those are the minimum requirement to create a car and is needed by any constructor of class Car")
    }
  }
  new Task("Task 10") {
    def solution() = {
      class Employee(){
        private var name = "John Q. Public"
        var salary = 0.0
        
        def this (name: String, salary: Double){
          this()
          this.name = name
          this.salary = salary          
        }
      }
      println("Name was val, therefore it is wether the name passed to the constructor or \"John Q. Public\". To provide this behavior the field name needs to be a private var." +  
    		  " Thus it is constructed as \"John Q. Public\" as default or with the name passed to the auxiliary constuctor. Further it is not writable from the outside and can't be changed by other (non-Employee) Objects")
      println("Salary was var, therfore it was read-write. Just var will do.")
    		  
    }
  }
}
