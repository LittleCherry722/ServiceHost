import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer
import java.util.LinkedHashMap
import java.util.Calendar
import scala.collection.JavaConversions.propertiesAsScalaMap

object Solution extends App {

  // execute all tasks
  Tasks.execute()

  // execute only a single one
  //TaskManager.execute("Task 1")
}

class Counter {
  private var value = 0 // You must initialize the field
  def increment() { if (value != Int.MaxValue) value += 1 } // Methods are public by default
  def current() = value
}

class BankAccount {
  private var balance: Double = 0
  def deposit(n: Double) { balance += n }
  def withdraw(n: Double) { balance -= n }
  def getBalance = balance
}

class Time(private val hour: Int, private val min: Int) {
  private val minutes = hour * 60 + min
  def getHour = hour
  def getMin = minutes
  def before(t: Time): Boolean = {
    minutes < t.getMin
  }
}
class Person(private var privateAge: Int) {
  if (privateAge < 0) privateAge = 0
  def age = privateAge
  def age_=(newValue: Int) {
    if (newValue > privateAge) privateAge = newValue;
  }
}

class Car(private val manufacturer: String, private val model: String, private val year: Int, var plate: String) {
  def this(manufacturer: String, model: String) {
    this(manufacturer, model, -1, "")
  }
  def this(manufacturer: String, model: String, year: Int) {
    this(manufacturer, model, year, "")
  }
  def this(manufacturer: String, model: String, plate: String) {
    this(manufacturer, model, -1, plate)
  }
  def getManufacturer = manufacturer
  def getModel = model
  def getYear = year

}


//employee version with default primary constructor ... I prefer this one because it is much neater

class Employee(val name: String="John Q. Public", var salary: Double=0.0) {
}


// employee version with explicit parameters

//class Employee() {
//  private var Name:String = "John Q. Public";
//  var Salary:Double = 0.0
//  def this(name:String, salary:Double){
//    this()
//    Name=name
//    Salary=salary
//  }
//  def getName = Name
//}

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

  new Task("Task 2") {
    def solution() = {
      val acc = new BankAccount
      acc.deposit(100)
      println(acc.getBalance);
      acc.withdraw(20)
      println(acc.getBalance);

    }
  }

  new Task("Task 3 and 4") {
    def solution() = {
      val time = new Time(23, 50);
      println(time.before(new Time(23, 40)))
      println(time.before(new Time(23, 59)))
      println(time.before(new Time(23, 50)))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val p = new Person(-1)
      println(p.age)
    }
  }

  new Task("Task 8") {
    def solution() = {
      // the primary constructor is the one where all parameters must be supplied
      // because the parameters supplied in the primary constructor become the class fields
    }
  }

  

}
