
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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

    }

    class Counter {
      private var value = 0

      def increment() { if (value < Int.MaxValue) value += 1 }

      def current() = value
    }

  }

  new Task("Task 2") {
    def solution() = {
      val account = new BankAccount
      account.deposit(100)
      account.withdraw(20)

      println(account.balance)
    }

    class BankAccount {
      private var _balance = BigDecimal(0)

      def balance = _balance

      def deposit(amount: BigDecimal) {
        require(amount > 0)
        _balance += amount
      }

      def withdraw(amount: BigDecimal) {
        require(amount > 0)
        _balance -= amount
      }
    }

  }

  new Task("Task 3") {
    def solution() = {
      val time1 = new Time(3, 45)
      val time2 = new Time(4, 30)

      println(time1.hrs + ":" + time1.min)
      println(time1 before time2)
    }

    class Time(val hrs: Int, val min: Int) {
      require(hrs >= 0 && hrs < 24)
      require(min >= 0 && min < 60)

      def before(other: Time) = (hrs * 60 + min) < (other.hrs * 60 + other.min)
    }

  }

  new Task("Task 4") {
    def solution() = {
      val time1 = new Time(3, 45)
      val time2 = new Time(4, 30)

      println(time1.hrs + ":" + time1.min)
      println(time1 before time2)
    }

    class Time(h: Int, m: Int) {
      require(h >= 0 && h < 24)
      require(m >= 0 && m < 60)

      private val time = h * 60 + m

      def before(other: Time) = time < other.time

      def hrs = time / 60

      def min = time % 60
    }

  }

  new Task("Task 6") {
    def solution() = {
      val p1 = new Person(12)
      val p2 = new Person(-12)

      println(p1.age)
      println(p2.age)
    }

    class Person(private var _age: Int) {
      if (_age < 0) _age = 0

      def age = _age

      def age_=(newValue: Int) { if (newValue > _age) _age = newValue }
    }

  }

  new Task("Task 8") {
    def solution() = {
      val car1 = new Car("Manuf. A", "Model A", 2003, "AB-CD-1")
      val car2 = new Car("Manuf. B", "Model B", 2005)
      val car3 = new Car("Manuf. C", "Model C", "RT-ZU-56")
      val car4 = new Car("Manuf. D", "Model D")

      // I chose the constructor with all properties as the primary one.
      // So you can recognize all properties at first sight.
    }

    class Car(val manufacturer: String, val model: String, val modelYear: Int, var licensePlate: String) {

      def this(manufacturer: String, model: String, licensePlate: String) {
        this(manufacturer, model, -1, licensePlate)
      }

      def this(manufacturer: String, model: String, modelYear: Int) {
        this(manufacturer, model, modelYear, "")
      }

      def this(manufacturer: String, model: String) {
        this(manufacturer, model, -1)
      }
    }

  }

  new Task("Task 10") {
    def solution() = {
      // I prefer the other form, because it is shorter.
      // Only if the default values are somehow calculated, this form could be better.
    }

    class Employee {
      private var _name = "John Q. Public"
      var salary = 0.0

      def this(name: String, salary: Double) {
        this()
        this._name = name
        this.salary = salary
      }

      def name = _name
    }

  }

}
