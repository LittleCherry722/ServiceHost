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
    println(name+":");
    solution();
    println("\n");
  }
}

class Tasks {
	private var tasks = Seq[Task]();
	def add (t: Task) = { tasks :+= t }
	def execute () = { tasks.foreach( (t:Task) => { t.execute(); } )  }
	def execute (name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  
  new Task("Task 1"){
    def solution() = {
      class Counter {
        // set type of value to BigInt to prevent it from becoming negative by incrementing
        private var value : BigInt = 0 // You must initialize the field
        def increment() { value += 1 } // Methods are public by default
        def current() = value
      }
      println("code to demonstrate that its working was commented out, because it has a very long runtime")
      /*
      // code for testing (long runtime)
      var c = new Counter()
      println("max int: " + Integer.MAX_VALUE)
      println(c.current())
      for (i <- 0 to Integer.MAX_VALUE - 1) c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      c.increment()
      println(c.current())
      */
    }
  }
  
  new Task("Task 2"){
    def solution() = {
      class BankAccount {
        private var accountBalance : BigDecimal = 0
        def deposit(n : BigDecimal) = {
          accountBalance += n
        }
        def withdraw(n : BigDecimal) = {
          accountBalance -= n
        }
        def balance = accountBalance 
      }
      var account = new BankAccount()
      println("New Account created (initial value is set to 0)")
      println("current balance: " + account.balance)
      println("deposit 50")
      account.deposit(50.0)
      println("current balance: " + account.balance)
      println("withdraw 3000")
      account.withdraw(3000.0)
      println("current balance: " + account.balance)
      println("withdraw BigDecimal.MaxLong = " + BigDecimal.MaxLong)
      account.withdraw(BigDecimal.MaxLong)
      println("current balance: " + account.balance)
    }
  }
  
  new Task("Task 3"){
    def solution() = {
      class Time(private var hrs : Int,private var min : Int) {
        if (hrs < 0 || hrs > 23) throw new IllegalArgumentException("hrs-parameter has to be an integer between 0 and 23")
        if (min < 0 || min > 59) throw new IllegalArgumentException("min-parameter has to be an integer between 0 and 59")
        def hours = hrs
        def minutes = min
        def before(other : Time) = {
          this.hours < other.hours || this.hours == other.hours && this.minutes < other.minutes
        }
      }
      println("t1 = new Time(3,1)")
      println("t2 = new Time(3,2)")
      println("t3 = new Time(4,1)")
      var t1 = new Time(3,1)
      var t2 = new Time(3,2)
      var t3 = new Time(4,1)
      println("t1 before t1 = " + (t1 before t1))
      println("t1 before t2 = " + (t1 before t2))
      println("t1 before t3 = " + (t1 before t3))
      println("t2 before t1 = " + (t2 before t1))
      println("t2 before t2 = " + (t2 before t2))
      println("t2 before t3 = " + (t2 before t3))
      println("t3 before t1 = " + (t3 before t1))
      println("t3 before t2 = " + (t3 before t2))
      println("t3 before t3 = " + (t3 before t3))
    }
  }
  
  new Task("Task 4"){
    def solution() = {
      class Time(private var hrs : Int,private var min : Int) {
        if (hrs < 0 || hrs > 23) throw new IllegalArgumentException("hrs-parameter has to be an integer between 0 and 23")
        if (min < 0 || min > 59) throw new IllegalArgumentException("min-parameter has to be an integer between 0 and 59")
        private var minSinceMidnight = (hrs * 60) + min
        def hours = { (minSinceMidnight - (minutes)) / 60 }
        def minutes = { minSinceMidnight % 60 }
        def before(other : Time) = {
          this.hours < other.hours || this.hours == other.hours && this.minutes < other.minutes
        }
      }
      println("t1 = new Time(3,1)")
      println("t2 = new Time(3,2)")
      println("t3 = new Time(4,1)")
      var t1 = new Time(3,1)
      var t2 = new Time(3,2)
      var t3 = new Time(4,1)
      println("t1 before t1 = " + (t1 before t1))
      println("t1 before t2 = " + (t1 before t2))
      println("t1 before t3 = " + (t1 before t3))
      println("t2 before t1 = " + (t2 before t1))
      println("t2 before t2 = " + (t2 before t2))
      println("t2 before t3 = " + (t2 before t3))
      println("t3 before t1 = " + (t3 before t1))
      println("t3 before t2 = " + (t3 before t2))
      println("t3 before t3 = " + (t3 before t3))
    }
  }
  
  new Task("Task 5"){
	  def solution() = {

	  		// your solution for task 5 here
	    
	  }
  }
  
  new Task("Task 6"){
    def solution() = {
      class Person(private var privateAge : Int) {
        if (privateAge < 0) privateAge = 0
        def age = privateAge
        def age_=(newValue: Int) {
         if (newValue > privateAge) privateAge = newValue; // Can’t get younger
        }
      }
      println("new Person(3).age = " + new Person(3).age)
      println("new Person(-3).age = " + new Person(-3).age)
    }
  } 
   
  new Task("Task 7"){
	  def solution() = {

	  		// your solution for task 7 here
	    
	  }
  }
    
  new Task("Task 8"){
    def solution() = {
      // I chose a primary constructor with default-parameters for optional values.
      // This Allows using four different constructors for construction, but only one has to be defined for the class.
      println("I chose a primary constructor with default-parameters for optional values.")
      println("This Allows using four different constructors for construction, but only one has to be defined for the class.")
      class Car(var manufacturer : String,
                var modelName : String,
                var modelYear : Int = -1,
                var licensePlate : String = "") {
      }
      println("var c1 = new Car(\"Opel\", \"Kadett B\")")
      var c1 = new Car("Opel", "Kadett B")
      println("c1.manufacturer = " + c1.manufacturer)
      println("c1.modelName = " + c1.modelName)
      println("c1.modelYear = " + c1.modelYear)
      println("c1.licensePlate = " + c1.licensePlate)
      println("")
      println("var c2 = new Car(\"Opel\", \"Kadett B\", modelYear = 1965)")
      var c2 = new Car("Opel", "Kadett B", modelYear = 1965)
      println("c2.manufacturer = " + c2.manufacturer)
      println("c2.modelName = " + c2.modelName)
      println("c2.modelYear = " + c2.modelYear)
      println("c2.licensePlate = " + c2.licensePlate)
      println("")
      println("var c3 = new Car(\"Opel\", \"Kadett B\", licensePlate = \"AB CD 123\")")
      var c3 = new Car("Opel", "Kadett B", licensePlate = "AB CD 123")
      println("c3.manufacturer = " + c3.manufacturer)
      println("c3.modelName = " + c3.modelName)
      println("c3.modelYear = " + c3.modelYear)
      println("c3.licensePlate = " + c3.licensePlate)
      println("")
      println("var c4 = new Car(\"Opel\", \"Kadett B\", modelYear = 1965, licensePlate = \"AB CD 123\")")
      var c4 = new Car("Opel", "Kadett B", 1965, "AB CD 123")
      println("c4.manufacturer = " + c4.manufacturer)
      println("c4.modelName = " + c4.modelName)
      println("c4.modelYear = " + c4.modelYear)
      println("c4.licensePlate = " + c4.licensePlate)
    }
  }
    
  new Task("Task 9"){
	  def solution() = {

	  		// your solution for task 9 here
	    
	  }
  }
    
  new Task("Task 10"){
    def solution() = {
      // I prefer this version because it less to write and I use the python-language
      // at work where default-parameters are often used.
      class Employee(n : String = "John Q. Public", s : Double = 0.0) {
        val name: String = n
        var salary: Double = s
      }
    }
  }
  
}
