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

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here

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
