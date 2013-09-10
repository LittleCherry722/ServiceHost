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
      class BankAccount(initialBalance: Double) {
        private var balance = initialBalance
        def deposit(amount: Double) = { balance += amount; balance }
        def withdraw(amount: Double) = { balance -= amount; balance }

        override def toString() = "balance = " + balance
      }

      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        override def deposit(amount: Double) = { super.deposit(amount - 1) }
        override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
      }

      val ca = new CheckingAccount(10.0)
      println(ca)
      ca.deposit(5.0)
      println(ca)
      ca.withdraw(2.0)
      println(ca)

    }
  }

  new Task("Task 2") {
    def solution() = {

      class BankAccount(initialBalance: Double) {
        private var balance = initialBalance
        def deposit(amount: Double) = { balance += amount; balance }
        def withdraw(amount: Double) = { balance -= amount; balance }

        override def toString() = "balance = " + balance
      }

      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        private var freeTransAmount = 3

        override def deposit(amount: Double) = {
          if (freeTransAmount > 0) {
            freeTransAmount -= 1
            super.deposit(amount)
          } else {
            super.deposit(amount - 1)
          }
        }

        override def withdraw(amount: Double) = {
          if (freeTransAmount > 0) {
            freeTransAmount -= 1
            super.withdraw(amount)
          } else {
            super.withdraw(amount + 1)
          }
        }

        def earnMonthlyInterest() = {
          freeTransAmount = 3
          val currentBalance = super.deposit(0)
          val interest = currentBalance * 0.01
          super.deposit(interest)
        }
      }

    }
  }

  new Task("Task 3") {
    def solution() = {
      /* Example from http://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html */
      class Bicycle(var cadence: Int, var gear: Int, var speed: Int) {}
      class MountainBike(var height: Int, cadence: Int, gear: Int, speed: Int) extends Bicycle(cadence, gear, speed) {}
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item {
        def price: Double
        def description: String
      }

      class SimpleItem(override val price: Double, override val description: String) extends Item {}

      class Bundle extends Item {
        var items = Array[Item]()
        override def price = {
          var sum = 0.0
          for (item <- items) {
            sum += item.price
          }
          sum
        }
        override def description = {
          var desc = "This is a bundle with following items:\n"
          for (item <- items) {
            desc += item.description + ": " + item.price + "\n"
          }
          desc
        }

        def addItem(item: Item) {
          items = items :+ item
        }
      }

      val iphone = new SimpleItem(600.00, "Iphone 5S")
      val laptop = new SimpleItem(1000.00, "Vaio")
      val tv = new SimpleItem(5000.00, "Samsung TV")

      val bundle = new Bundle
      bundle.addItem(iphone)
      bundle.addItem(laptop)
      bundle.addItem(tv)
      println(bundle.description)
    }
  }

  new Task("Task 5") {
    def solution() = {
      class Point(val x: Double, val y: Double) {}
      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x, y) {}

      val lp = new LabeledPoint("Black Thursday", 1929, 230.07)
      println(lp.label + " -> (" + lp.x + "|" + lp.y + ")")
    }
  }

  new Task("Task 6") {
    def solution() = {
      class Point(val x: Double, val y: Double) {
        override def toString() = { "(" + x + "|" + y + ")" }
      }

      abstract class Shape {
        def centerPoint: Point
      }

      /* screen coordinate. (x|y) is the top left corner */
      class Rectangle(val x: Double, val y: Double, val width: Double, val height: Double) extends Shape {
        override def centerPoint = new Point(x + width / 2, y + height / 2)
      }

      class Circle(val x: Double, val y: Double, val radius: Double) extends Shape {
        override def centerPoint = new Point(x, y)
      }

      val r = new Rectangle(10, 10, 20, 30)
      println(r centerPoint)
      val c = new Circle(5, 6, 7)
      println(c centerPoint)
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

    }
  }

}
