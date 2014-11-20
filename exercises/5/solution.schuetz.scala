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
    	  private var value = Int.MaxValue 
    	  def increment() { value = value % Int.MaxValue + 1 }
    	  def current = value
    	}
    	
    	val counter = new Counter()
    	println(counter.current)
    	counter.increment()
    	println(counter.current)
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
    	class BankAccount {
    	  private var balance: Double = 0.0
    	  def deposit(amount: Double) { balance += amount}
    	  def withdraw(amount:  Double) {balance -= amount}
    	  def currentBalance = balance
    	}
    	
    	val account = new BankAccount()
    	account.deposit(5000)
    	println(account.currentBalance)
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
    	class Time() {
    	  private var hrs = 0
    	  private var min = 0
    	  
    	  def this(hrs: Integer, min: Integer) {
    	    this()
    	    this.hrs = hrs
    	    this.min = min
    	  }
    	  
    	  def before(other: Time):Boolean = {
    	    if (hrs < other.hrs ) true
    	    else if (hrs == other.hrs && min < other.min ) true
    	    else false
    	  }
    	}
    	
    	val time1 = new Time(2,35)
    	val time2 = new Time(3,51)
    	val time3 = new Time(1,15)
    	
    	println(time1.before(time2))
    	println(time1.before(time3))
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
    	class Time() {
    	  var minSinceMidnight = 0
    	  
    	  def this(hrs: Integer, min: Integer) {
    	    this()
    	    minSinceMidnight  = 60 * hrs + min
    	  }
    	  
    	  def before(other: Time):Boolean = {
    	    if (minSinceMidnight  < other.minSinceMidnight  ) true
    	    else false
    	  }
    	}
    	
    	val time1 = new Time(2,35)
    	val time2 = new Time(3,51)
    	val time3 = new Time(1,15)
    	
    	println(time1.before(time2))
    	println(time1.before(time3))
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
    	class Person(name: String, var age: Int) {
    		if (age < 0) age = 0
    	}
    	
    	val p = new Person("Peter", -2)
    	println (p.age)
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
    	class Car(val manufactor: String, val model: String) {
    	  //this is the primary constructor because all constructor need this values
			private var year = -1
			var licensePlate = ""
		    
			def modelYear = year
			  
			def this(manufactor: String, model: String, modelYear: Integer) {
			    this(manufactor, model)
			    this.year = modelYear
			}
			
			def this(manufactor: String, model: String, licensePlate: String) {
			    this(manufactor, model)
			    this.licensePlate  = licensePlate  
			}
			
			def this(manufactor: String, model: String, modelYear: Integer, licensePlate: String) {
			    this(manufactor, model)
			    this.year = modelYear
			    this.licensePlate  = licensePlate  
			}
    	  
    	}
    	
    	val car1 = new Car("Audi", "A1")
    	println("Car1:")
    	println("Manufactor: " + car1.manufactor )
    	println("Model: " + car1.model  )
    	println("Year: " + car1.modelYear )
    	println("License plate: " + car1.licensePlate + "\n"  )
    	
    	val car2 = new Car("Audi", "A2", 2010)
    	println("Car2:")
    	println("Manufactor: " + car2.manufactor )
    	println("Model: " + car2.model  )
    	println("Year: " + car2.modelYear )
    	println("License plate: " + car2.licensePlate + "\n" )
    	
    	val car3 = new Car("Audi", "A3", "DA-DA-2014")
    	println("Car3:")
    	println("Manufactor: " + car3.manufactor )
    	println("Model: " + car3.model  )
    	println("Year: " + car3.modelYear )
    	println("License plate: " + car3.licensePlate + "\n"  )
    	
    	val car4 = new Car("Audi", "A4", 2014, "DA-DA-2015")
    	println("Car4:")
    	println("Manufactor: " + car4.manufactor )
    	println("Model: " + car4.model  )
    	println("Year: " + car4.modelYear )
    	println("License plate: " + car4.licensePlate  )
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here
      
      class Employee(na: String = "John Q. Public", sal: Double = 0.0) {
        val name = na
        val salary = sal
      }
      
      val e = new Employee
      println("Name: " + e.name + " Salary: " + e.salary )
    }
  }

}
