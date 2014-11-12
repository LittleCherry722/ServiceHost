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
        def currentBalance = balance
        def deposit(amount: Double): Double = {
          balance += amount
          balance
        }
        def withdraw(amount: Double) : Double = {
          balance -= amount
          balance
        }
      }
      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        private val transactionFee = 1.0
        override def deposit(amount: Double): Double = {
          super.withdraw(transactionFee + amount)
        }
        override def withdraw(amount: Double): Double = {
          super.withdraw(transactionFee + amount)
        }
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount {
        private var privateBalance = 0d
        def balance = privateBalance
        def withdraw(amount: Double) {
          privateBalance -= amount
        }
        def deposit(amount: Double) {
          privateBalance += amount
        }
      }

      class SavingsAccount(val interest: Double = .01, val transactionFee: Double = .5) extends BankAccount {
        private var freeTransactions = 3
        def earnMonthlyInterest = {
          deposit(balance * interest)
          freeTransactions = 3
        }
        override def deposit(amount: Double) {
          super.deposit(amount)
          if (freeTransactions > 0) {
            freeTransactions -= 1
          }
          else {
            super.withdraw(transactionFee)
          }
        }
        override def withdraw(amount: Double) {
          super.withdraw(amount)
          if (freeTransactions > 0) {
            freeTransactions -= 1
          }
          else {
            super.withdraw(transactionFee)
          }
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      class Game(val i: Int) {
        println("Game constructor")
      }
      class BoardGame(i: Int) extends Game(i) {
        println("BoardGame constructor")
      }
      class Chess(i: Int) extends BoardGame(i) {
        println("Chess constructor")
      }
    }
  }

  new Task("Task 4") {
    def solution() = {

      abstract class Item {
        def price: Double
        def description: String
      }

      class SimpleItem(priceParam: Double, descriptionParam: String) extends Item {
        override val price = priceParam
        override val description = descriptionParam
      }

      class Bundle(val items: Array[SimpleItem]) extends Item {
        def price = items.foldLeft(0.0)(_ + _.price)
        def description = "a bundle containing: " + items.map(_.description).mkString(", ")
        def addItem(item: SimpleItem): Bundle = {
          new Bundle(items ++ Array(item))
        }
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      class Point(val x: Double, val y: Double)
      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x, y)
    }
  }

  new Task("Task 6") {
    def solution() = {
      class Point(val x: Double, val y: Double)
      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x, y)

      abstract class Shape {
        def centerPoint: Point
      }

      class Circle(val origin: Point) extends Shape {
        def centerPoint = origin
      }

      class Rectangle(upperLeft: Point, bottomRight: Point) {
        def centerPoint = new Point(
          bottomRight.x - (bottomRight.x - upperLeft.x) / 2,
          bottomRight.y - (bottomRight.y - upperLeft.y) / 2)
      }
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
