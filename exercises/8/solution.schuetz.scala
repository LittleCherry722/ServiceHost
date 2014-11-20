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

      // your solution for task 1 here
      
      class BankAccount( initialBalance: Double) {
        private var balance = initialBalance
        def currentBalance = balance 
        def deposit( amount: Double) = { balance += amount; balance }
        def withdraw( amount: Double) = { balance -= amount; balance }
      }
      
      class CheckingAccount( initialBalance: Double) extends BankAccount( initialBalance) {
        override def deposit( amount: Double) = { super.deposit(amount -1)}
        override def withdraw( amount: Double) = { super.withdraw(amount + 1)}
      }
      
      val ca = new CheckingAccount(500)
      println("current balance: " + ca.currentBalance)
      println("deposit 50")
      ca.deposit(50)
      println("current balance: " + ca.currentBalance)
      println("withdraw 100")
      ca.withdraw(100)
      println("current balance: " + ca.currentBalance)

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      class BankAccount( initialBalance: Double) {
        private var balance = initialBalance
        def currentBalance = balance 
        def deposit( amount: Double) = { balance += amount; balance }
        def withdraw( amount: Double) = { balance -= amount; balance }
      }
      
      class SavingsAccount( initialBalance: Double) extends BankAccount( initialBalance) {
        var freeTrans: Integer = 0
        def earnMonthlyInterest(percentage: Double) = {super.deposit(super.currentBalance * percentage); freeTrans = 0 }
        override def deposit( amount: Double) = { 
          if(freeTrans < 3) {
            freeTrans += 1
            super.deposit(amount)
          }
          else
            super.deposit(amount- 1)
        }
        override def withdraw( amount: Double) = { 
          if(freeTrans < 3) { 
            freeTrans += 1
            super.withdraw(amount)
          }
          else
            super.withdraw(amount + 1)
        }
      }
      
      val sa = new SavingsAccount(500)
      println("current balance: " + sa.currentBalance)
      sa.deposit(20)
      sa.deposit(20)
      sa.withdraw(20)
      println("balance after 3 transactions (+20,+20,-20): " + sa.currentBalance)
      sa.withdraw(50)
      println("balance after another withdraw (-50): " + sa.currentBalance)
      sa.earnMonthlyInterest(0.01)
      println("balance after motnhly interest: " + sa.currentBalance)
      sa.deposit(10)
      println("balance after deposit in a new month (+10): " + sa.currentBalance)

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
      abstract class Item() {
        def price: Double
        def description: String
      }
      
      class SimpleItem(simplePrice: Double, simpleDescription: String) extends Item {
        override val price = simplePrice
        override val description = simpleDescription
      }
      
      import scala.collection.mutable.ArrayBuffer
      
      class Bundle(val items: ArrayBuffer[Item]) extends Item {
        def price = items.foldLeft(0.0)(_ + _.price).toDouble
        def description = "This Bundle contains the following items:" + items.foldLeft(" ")(_ + ", " + _.description)
        
        def addItem(item: Item) {
          items += item
        }
      }
      
      val si1 = new SimpleItem(1, "SimpleItem1")
      val si2 = new SimpleItem(2, "SimpleItem2")
      val si3 = new SimpleItem(3, "SimpleItem3")
      
      val bundle = new Bundle(ArrayBuffer(si1,si2))
      println("Price of the bundle: " + bundle.price)
      println("Description of the bundle: " + bundle.description)
      bundle.addItem(si3)
      println("Price of the new bundle: " + bundle.price)
      println("Description of the new bundle: " + bundle.description)

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      
      class Point( val x: Double, val y: Double)
      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x,y)

      val lp = new LabeledPoint("Black Thursday", 1929, 230.07)
      println(lp.label  + " x: " + lp.x  + " y: " + lp.y )
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      
      
      class Point( val x: Double, val y: Double)
      
      abstract class Shape {
        def centerPoint: Point
      }
      
      class Rectangle(val startPoint: Point, val expandX: Double, val expandY: Double) extends Shape {
        def centerPoint = new Point(startPoint.x + expandX/2, startPoint.y + expandY/2)
      }
      
      class Circle(val startPoint: Point, val radius: Double) extends Shape {
        def centerPoint = startPoint
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
