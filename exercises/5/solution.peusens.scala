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

      // your solution for task 1 here
      class Counter {
        private var value = 0 // You must initialize the field
        def increment() { if(value + 1 != Int.MaxValue){ value += 1 } } // Methods are public by default
        def current() = value
      }

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      class BankAccount {
        private var deposit = 0;
        
        def deposit(amount: Int) { deposit = deposit + amount}
        def withdraw(amount: Int) { deposit = deposit - amount}
      }

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
       class Time{
        private var hours = 0
        private var minutes = 0
        
        def this(hrs: Int, min: Int){
          this()
          this.hours = hrs
          this.minutes = min
        }
        def before(other: Time) = {
          if(hours < other.hours){
            true
          }
          else if(minutes < other.minutes){
            true
          }
          else{
            false
          }
        }
      }
      
      var time1 = new Time(12,30)
      var time2 = new Time(12,10)
      
      println(time1.before(time2))
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      class Time{
        private var minutes = 0
        
        def this(hrs: Int, min: Int){
          this()
          this.minutes = hrs*60 + min -1
        }
        def before(other: Time) = {
          if(minutes < other.minutes){
            true
          }
          else{
            false
          }
        }
      }
      
      println("start");
      var time1 = new Time(12,30)
      var time2 = new Time(13,10)
      
      println(time1.before(time2))

    }
  }


  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      class Person (val age_init: Int) {
        private var privateAge = age_init
        if(privateAge < 0) { privateAge = 0}
      
        def age: Int = privateAge
        def age_(newValue: Int): Unit = {
        if (newValue > privateAge) privateAge = newValue;
      }
    }
    
      
    }
  }


  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
      class Car(val manufacturer: String, val modelName: String, val modelYear: Int = -1, var licensePlate: String = "") {
    	  //... stuff to do here
      }
    
      println("As we know it from C++, we can set default values directly in the primary constructor. If the field is not given while class instantiation, the default value will be used")
    

    }
  }



  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      class Employee() {
        var name: String = "John Q. Public"
        var salary: Double = 0.0
      
        def this(name: String, salary: Double) {
          this()
          this.name = name
          this.salary = salary
        }
      }

    }
  }

}
