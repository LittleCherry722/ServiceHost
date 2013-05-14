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
      val counter = new Counter
      counter.increment
      counter.increment
      counter.increment
      counter.increment

      println(counter.current)
      //works, but takes a bit to execute
      //      for (i <- 0 to Int.MaxValue-1) {
      //        counter.increment
      //      }
      //      println(counter.current)

    }
    class Counter {
      private var value: Int = 0
      def increment() { if (value < Int.MaxValue) value += 1 }
      def current = value
    }
  }

  new Task("Task 2") {
    def solution() = {
      var acc = new BankAccount
      acc.deposit(20)
      println(acc.balance)
      acc.withdraw(10)
      println(acc.balance)
    }
    class BankAccount {
      private var total = 0
      def deposit(amount: Int) {
        total += amount
      }
      def withdraw(amount: Int) {
        total -= amount
      }
      def balance = total
    }
  }

  new Task("Task 3") {
    def solution() = {
      val nineteenThirty = new Time(19, 30)
      val twentyTwenty = new Time(20, 20)
      //      val illegalTime = new Time(25, 20)
      println(nineteenThirty.before(twentyTwenty))
      println(twentyTwenty.before(nineteenThirty))
      println(twentyTwenty.before(twentyTwenty))
    }
    class Time(val hrs: Int, val min: Int) {
      require(hrs >= 0 && hrs < 24)
      require(min >= 0 && min < 60)
      def before(other: Time): Boolean = {
        (other.hrs > hrs) || (other.hrs == hrs && other.min > min)
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      val nineteenThirty = new Time(19, 30)
      val twentyTwenty = new Time(20, 20)
      //      val illegalTime = new Time(25, 20)
      println(nineteenThirty.before(twentyTwenty))
      println(twentyTwenty.before(nineteenThirty))
      println(twentyTwenty.before(twentyTwenty))
    }

    class Time(hrs: Int, min: Int) {
      require(hrs >= 0 && hrs < 24)
      require(min >= 0 && min < 60)

      var time = hrs * 60 + min

      def before(other: Time): Boolean = {
        other.time > time
      }
    }
  }

  new Task("Task 6") {
    def solution() = {
      val person1 = new Person(5)
      val person2 = new Person(-5)
      println(person1.age)
      println(person2.age)
      person1.age_=(4)
      println(person1.age)
      person1.age_=(8)
      println(person1.age)
    }

    class Person(private var privateAge: Int) {
      if (privateAge < 0) privateAge = 0

      def age = privateAge

      def age_=(newValue: Int) {
        if (newValue > privateAge) privateAge = newValue
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      val car1 = new Car("Porsche", "Cayenne", 1999, "AB-CD 42")
      val car3 = new Car("Porsche", "Cayenne")
      val car2 = new Car("Porsche", "Cayenne", "AB-CD 42")
      val car4 = new Car("Porsche", "Cayenne", 1999)

      println(car1.licensePlate)
      println(car4.licensePlate)
      println(car3.year)
      println(car2.name)

      //The primary constructor is the default constructor Car(String, String, Int, String) 
      //so it can easily be re-used by the other constructors and no explicit fields are required.
    }

    class Car(val manufacturer: String, val name: String, val year: Int, var licensePlate: String) {
      def this(manufacturer: String, name: String) = {
        this(manufacturer, name, -1, "")
      }

      def this(manufacturer: String, name: String, year: Int) {
        this(manufacturer, name, year, "")
      }

      def this(manufacturer: String, name: String, licensePlate: String) {
        this(manufacturer, name, -1, licensePlate)
      }
    }
  }

  new Task("Task 10") {
    def solution() = {
      val john = new Employee()
      println(john.name)
      val jeff = new Employee("Jeff B. Good", 42.0)
      println(jeff.name)
      
      //The other form is shorter and easier to write and maintain, so I prefer that one.
    }

    class Employee {
      private var privateName: String = "John Q. Public"
      var salary: Double = 0.0

      def name = privateName

      def this(name: String, salary: Double) {
        this()
        this.privateName = name
        this.salary = salary
      }
    }
  }

}
