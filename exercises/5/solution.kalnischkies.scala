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
        private var value = 0
        def increment() {
          if (value < scala.Int.MaxValue)
            value += 1
        }
        def current = value
      }
      var c = new Counter
      c.increment()
      c.increment()
      println(c.current)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount(private[this] var pBalance: Int = 0) {
        def balance = pBalance
        def deposit(money: Int) { pBalance += money }
        def withdraw(money: Int) { pBalance -= money }
      }
      var david = new BankAccount
      david.deposit(1000)
      david.withdraw(333)
      println(david.balance)
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Time(val hrs: Int, val min: Int) {
        def before(other: Time) = {
          hrs <= other.hrs && min < other.min
        }
      }
      val zero = new Time(0, 0)
      val max = new Time(23, 59)
      val birth = new Time (7, 5)
      println(zero.hrs + ":" + zero.min + " < " + max.hrs + ":" + max.min + " " + zero.before(max))
      println("00:00 < 07:05 " + zero.before(birth))
      println("23:59 < 00:00 " + max.before(zero))
      println("07:05 < 00:00 " + birth.before(zero))
      println("23:59 < 07:05 " + max.before(birth))
      println("07:05 < 23:59 " + birth.before(max))
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Time(chrs: Int, cmin: Int) {
        private[this] val pMin = (chrs * 60) + cmin
        def min = pMin % 60
        def hrs : Int = pMin / 60
        def before(other: Time) = {
          hrs <= other.hrs && min < other.min
        }
      }
      val zero = new Time(0, 0)
      val max = new Time(23, 59)
      val birth = new Time (7, 5)
      println(zero.hrs + ":" + zero.min + " < " + max.hrs + ":" + max.min + " " + zero.before(max))
      println("00:00 < 07:05 " + zero.before(birth))
      println("23:59 < 00:00 " + max.before(zero))
      println("07:05 < 00:00 " + birth.before(zero))
      println("23:59 < 07:05 " + max.before(birth))
      println("07:05 < 23:59 " + birth.before(max))
    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(pAge: Int) {
        val age = if (pAge < 0) 0 else pAge
      }
      val david = new Person(26)
      val zero = new Person(0)
      val impossible = new Person(-2)
      println("David=" + david.age + " zero=" + zero.age + " impossible=" + impossible.age)
    }
  }

  new Task("Task 8") {
    def solution() = {
      // the exercise asks for 4 constructors, I have no idea why…
      class Car(val manufacturer: String, val modelname: String, val modelyear: Int = -1, var licenseplate: String = "") {
        def this(manufacturer: String, modelname: String, licenseplate: String) {
          this(manufacturer, modelname, -1, licenseplate)
        }
      }
      val star1 = new Car("Galactic Empire", "Death Star")
      val star2 = new Car("Galactic Empire", "Death Star", 1988)
      val star3 = new Car("Galactic Empire", "Death Star", "DEATH-3")
      val star4 = new Car("Galactic Empire", "Death Star", 1992, "DEATH-4")
      star1.licenseplate = "DEATH-1"
      println(star1.manufacturer + " " + star1.modelname + " " + star1.modelyear + " " + star1.licenseplate)
      println(star2.manufacturer + " " + star2.modelname + " " + star2.modelyear + " " + star2.licenseplate)
      println(star3.manufacturer + " " + star3.modelname + " " + star3.modelyear + " " + star3.licenseplate)
      println(star4.manufacturer + " " + star4.modelname + " " + star4.modelyear + " " + star4.licenseplate)
    }
  }

  new Task("Task 10") {
    // not exactly sure what this exercise is supposed to show…
    def solution() = {
      class Employee private(val pName: String) {
        val name = "John Q. Public"
        var salary = 0.0
        def this(name: String, pSalary: Double) {
          this(name)
          salary = pSalary
        }
      }
      val david = new Employee("David", 0.0)
      println(david.name + " " + david.salary)
    }
    // the "scala-style" is a lot shorted, but unusual.
    // What I will prefer in practice depends on surrounding parameters
    // (established codestyle, other teammembers, …) more than my personal
    // taste 10 minutes after I learned a new syntax…
  }
}
