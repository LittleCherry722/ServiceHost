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
        private var value: Int = 0
        def increment() {
          if (value == Int.MaxValue) {
            value = 0
          } else {
            value += 1
          }
        }
        def current() = value
      }

      val c = new Counter
      for (i <- 0 to Int.MaxValue / 2) {
        c.increment
        c.increment
      }
      for (i <- 0 to 10) {
        c.increment
      }
      println(c current)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount {
        private var amount = 0.0
        def balance = amount
        def deposit(a: Double) {
          amount += a
        }
        def withdraw(a: Double) {
          amount -= a
        }
      }

      val a = new BankAccount
      a.deposit(100.50)
      a.withdraw(50.40)
      println(a.balance)
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Time(private var hrs: Int, private var min: Int) {
        hrs = (hrs + (min / 60)) % 24
        min = min % 60
        def hours = hrs
        def minutes = min
        def before(other: Time) = {
          if (other.hours > this.hours) true else {
            if (other.hours < this.hours) false else {
              other.minutes > this.minutes
            }
          }
        }
      }

      val time1 = new Time(14, 25)
      val time2 = new Time(16, 20)
      val time3 = new Time(9, 10)
      val time4 = new Time(14, 06)
      val time5 = new Time(14, 25)
      val time6 = new Time(14, 30)

      println(time1 before time2)
      println(time1 before time3)
      println(time1 before time4)
      println(time1 before time5)
      println(time1 before time6)
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Time {
        private var stamp: Int = 0
        def this(hrs: Int, min: Int) {
          this()
          val myhrs = (hrs + (min / 60)) % 24
          val mymin = min % 60
          stamp = myhrs * 60 + mymin
        }
        def hours = stamp / 60
        def minutes = stamp % 60
        def before(other: Time) = stamp < other.stamp
      }

      val time1 = new Time(14, 25)
      val time2 = new Time(16, 20)
      val time3 = new Time(9, 10)
      val time4 = new Time(14, 06)
      val time5 = new Time(14, 25)
      val time6 = new Time(14, 30)

      println(time1 before time2)
      println(time1 before time3)
      println(time1 before time4)
      println(time1 before time5)
      println(time1 before time6)
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      class Person(var privateAge: Int) {
        if (privateAge < 0) privateAge = 0
        def age = privateAge
        def age_=(newage: Int) {
          if (newage > privateAge) privateAge = newage
        }
      }

      val p = new Person(-10)
      println(p age)
      p age = 20
      println(p age)
      p age = 15
      println(p age)
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {
      class Car(private var manu: String, private var mode: String) {
        private var year = -1
        private var plate = ""
        def this(aManu: String, aMode: String, aYear: Int) {
          this(aManu, aMode)
          year = aYear
        }
        def this(aManu: String, aMode: String, aPlate: String) {
          this(aManu, aMode)
          plate = aPlate
        }
        def this(aManu: String, aMode: String, aYear: Int, aPlate: String) {
          this(aManu, aMode)
          year = aYear
          plate = aPlate
        }

        def manufacture = manu
        def modelName = mode
        def modelYear = year
        def licensePlate = plate
        def licensePlate_=(aPlate: String) {
          plate = aPlate
        }
      }
    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      class Employee() {
        var privateName = "John Q. Public"
        var salary = 0.0

        def this(aName: String, aSalary: Double) {
          this()
          privateName = aName
          salary = aSalary
        }

        def name = privateName
      }

      println("I prefer to use implicit fields because we cannot change the name anymore even inside the class")
    }
  }

}
