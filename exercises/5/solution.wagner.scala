object Solution extends App {
  Tasks.execute()
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

      class Counter(start: Int) {
        private var value: Int = start
        def increment() { value = (value % Int.MaxValue) + 1 }
        def current() = value
      }
      val c = new Counter(Int.MaxValue)
      println("max value: " + c.current())
      c.increment()
      println("next value: " + c.current())

    }
  }

  new Task("Task 2") {
    def solution() = {

      class BankAccount {
        var thebalance: Int = 0
        def balance = thebalance
        def deposit(amount: Int) { thebalance += amount }
        def withdraw(amount: Int) { thebalance -= amount }
      }

    }
  }

  new Task("Task 3") {
    def solution() = {

      class Time(hrs: Int, min: Int) {
        def hours = hrs
        def minutes = min
        def before(other: Time) = this.time < other.time
        private def time = s"$hrs$min".toInt
        override def toString = s"$hrs:$min"
      }
      val t1 = new Time(12, 45)
      val t2 = new Time(13, 15)
      println(s"t1: $t1, t2: $t2 -> before: " + t1.before(t2).toString)
      println(s"t2: $t2, t1: $t1 -> before: " + t2.before(t1).toString)

    }
  }

  new Task("Task 4") {
    def solution() = {

      class Time(hrs: Int, min: Int) {
        val time = hrs * 60 + min
        def hours = time / 60
        def minutes = time % 60
        def before(other: Time) = time < other.time
        override def toString = s"$hours:$minutes"
      }
      val t1 = new Time(12, 45)
      val t2 = new Time(13, 15)
      println(s"t1: $t1, t2: $t2 -> before: " + t1.before(t2).toString)
      println(s"t2: $t2, t1: $t1 -> before: " + t2.before(t1).toString)
    }
  }

  new Task("Task 6") {
    def solution() = {

      class Person(val name: String, val a: Int) {
        private var privateAge = if (a > 0) a else 0
        def age = privateAge
        def age_=(newAge: Int) { if (newAge > privateAge) privateAge = newAge }
        override def toString = s"Person($name, $privateAge)"
      }
      println(s"""new Persen("Hans",-20) -> ${new Person("Hans", -20)}""")

    }
  }

  new Task("Task 8") {
    def solution() = {
      class Car(manufacturer: String, modelName: String) {
        private var modelYear: Int = -1
        var licensePlate: String = ""

        def this(manufacturer: String, modelName: String, modelYear: Int) {
          this(manufacturer, modelName)
          this.modelYear = modelYear
        }
        def this(manufacturer: String, modelName: String, licensePlate: String) {
          this(manufacturer, modelName)
          this.licensePlate = licensePlate
        }
        def this(manufacturer: String, modelName: String, modelYear: Int, licensePlate: String) {
          this(manufacturer, modelName)
          this.modelYear = modelYear
          this.licensePlate = licensePlate
        }
        override def toString = s"""Car("$manufacturer", "$modelName", $modelYear, "$licensePlate")"""
      }
      println(new Car("Audi", "A4"))
      println(new Car("Audi", "A4", 2100))
      println(new Car("Audi", "A4", "XYZ"))
      println(new Car("Audi", "A4", 2100, "XYZ"))
    }
  }

  new Task("Task 10") {
    def solution() = {
      /*
    	 * Der ursprüngliche Ansatz ist wesentlich prägnanter und erlaubt es
    	 * außerdem 'name' als val umzusetzen, was im zweiten Ansatz aufgrund
    	 * der Neuzuweisung nicht möglich ist.
    	 */
      class Employee1(val name: String, var salary: Double) {
        def this() { this("John Q. Public", 0.0) }
      }

      class Employee2() {
        var name: String = "John Q. Public"
        var salary: Double = 0.0

        def this(n: String, s: Double) {
          this()
          this.name = n
          this.salary = s
        }
      }
    }
  }

}
