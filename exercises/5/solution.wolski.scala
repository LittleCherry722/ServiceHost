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
    class Counter {
      private var value = 0
      
      def increment() {
        if (value < Int.MaxValue) {
          value += 1
        }
      }
      
      def current() = value
    }
    
    def solution() = {

      // your solution for task 1 here

    }
  }

  new Task("Task 2") {
    class BankAccount(private var privateBalance: Int = 0) {
      
      def balance: Int = this.privateBalance
      
      def deposit(amount: Int): Unit = {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        this.privateBalance += amount
      }
      
      def withdraw(amount: Int): Unit = {
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        this.privateBalance -= amount
      }
      
    }
    
    def solution() = {

      val acc1 = new BankAccount()
      println("balance: " + acc1.balance);
      acc1.deposit(50)
      println("balance: " + acc1.balance);
      acc1.withdraw(100)
      println("balance: " + acc1.balance);
      

    }
  }

  new Task("Task 3") {
    class Time(val hours: Int, val minutes: Int) {
      if (hours < 0 || hours > 23) throw new IllegalArgumentException("hours must be between 0 and 23");
      if (minutes < 0 || minutes > 59) throw new IllegalArgumentException("minutes must be between 0 and 59");
      
      def before(other: Time): Boolean = {
        (this.hours < other.hours) || (this.hours == other.hours && this.minutes < other.minutes)
      }
    }
    
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    class Time {
      private var privateMinutes: Int = 0
      
      def hours: Int = privateMinutes / 60
      def minutes: Int = privateMinutes % 60
      
      def this(hrs: Int, min: Int) {
        this()
        if (hrs < 0 || hrs > 23) throw new IllegalArgumentException("hrs must be between 0 and 23");
        if (min < 0 || min > 59) throw new IllegalArgumentException("min must be between 0 and 59");
        
        this.privateMinutes = hrs*60 + min
      }
      
      def before(other: Time): Boolean = {
        (this.privateMinutes < other.privateMinutes)
      }
    }
    
    def solution() = {

      val t1 = new Time(8, 23);
      val t2 = new Time(9, 50);
      println("t1: " + t1.hours+":"+t1.minutes);
      println("t2: " + t2.hours+":"+t2.minutes);
      println("t1.before(t2): " + t1.before(t2));
      
      val t3 = new Time(8, 23);
      val t4 = new Time(9, 00);
      println("t3: " + t3.hours+":"+t3.minutes);
      println("t4: " + t4.hours+":"+t4.minutes);
      println("t3.before(t4): " + t3.before(t4));

    }
  }

  new Task("Task 6") {
    class Person (val initAge: Int) {
      private var privateAge: Int = 0 max initAge // Make private and rename
      def age: Int = privateAge
      def age_(newValue: Int): Unit = {
        if (newValue > privateAge) privateAge = newValue; // Can’t get younger
      }
    }
    
    def solution() = {

      val a1 = new Person(5);
      val a2 = new Person(-5);
      
      println("a1.age: " + a1.age);
      println("a2.age: " + a2.age);

    }
  }

  new Task("Task 8") {
    class Car(val manufacturer: String, val model_name: String, val model_year: Int = -1, var license_plate: String = "") {
      
      def mkString(): String = {
        manufacturer + "; " + model_name + "; " + model_year + "; " + license_plate
      }
      
    }
    
    def solution() = {

      val c1 = new Car("AA", "a");
      println("c1: " + c1.mkString);

      val c2 = new Car("AA", "a7", 2007);
      println("c2: " + c2.mkString);

      val c3 = new Car("BB", "b8", 2008, "xy-12");
      println("c3: " + c3.mkString);

      val c4 = new Car("BB", "b", license_plate = "xy-12");
      println("c4: " + c4.mkString);
      
      
      
      println();
      println("I don't understand why there should be 4 constructors.. just using one");

    }
  }

  new Task("Task 10") {
    class Employee() {
      private var privateName: String = "John Q. Public"
      val name: String = privateName
      var salary: Double = 0.0
      
      def this(name: String, salary: Double) {
        this()
        this.privateName = name
        this.salary = salary
      }
    }
    
    def solution() = {

      // your solution for task 10 here

    }
  }

}
