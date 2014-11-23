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
  class BankAccount(initialBalance: Double) {
    private var balance = initialBalance
    def currentBalance  = balance
    def deposit(amount: Double) = { balance +=  amount; balance }
    def withdraw(amount:  Double) = { balance -=  amount; balance }
  }

  new Task("Task 1") {
    def solution() = {
      val david = new BankAccount(3.5)
      david.deposit(2.6)
      david.withdraw(3.8)
      println("Balance: "  + david.currentBalance)
      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        override def deposit(amount: Double) = super.deposit(amount - 1.0)
        override def withdraw(amount: Double) = super.withdraw(amount + 1.0)
      }
      val david2 = new CheckingAccount(3.5)
      david2.deposit(2.6)
      david2.withdraw(3.8)
      // who doesn't love the inbuilt inprecision of floating types?
      println("Balance: "  + david2.currentBalance)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        private var bankActions = 0
        override def deposit(amount: Double) = {
          bankActions += 1
          if (bankActions <= 3)
            super.deposit(amount)
          else
            super.deposit(amount - 1.0)
        }
        override def withdraw(amount: Double) = {
          bankActions += 1
          if (bankActions <= 3)
            super.withdraw(amount)
          else
            super.withdraw(amount + 1.0)
        }
        def earnMonthlyInterest() {
          bankActions = 0
          super.deposit(currentBalance * 0.05)
        }
      }
      val david = new SavingsAccount(3.5)
      david.deposit(2.6)
      david.withdraw(3.8)
      println("Balance: "  + david.currentBalance)
      david.earnMonthlyInterest()
      println("Balance: "  + david.currentBalance)
      david.earnMonthlyInterest()
      println("Balance: "  + david.currentBalance)

    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item {
        def price : Double
        def description : String
      }
      class SimpleItem(val price: Double, val description: String) extends Item {
      }
      class Bundle extends Item {
        private var items = new scala.collection.mutable.ArrayBuffer[Item]
        def price = items.foldLeft(0.0)(_ + _.price)
        def description = items.map(_.description).mkString("Bundle (", ", ", ")")
        def add(item: Item) { items += item }
      }
      val bubblegum = new SimpleItem(1.99, "Bubblegum")
      val magazine = new SimpleItem(9.99, "Magazine")
      println("ITEM: " + bubblegum.description + " costs " + bubblegum.price + " €")
      println("ITEM: " + magazine.description + " costs " + magazine.price + " €")
      val bundle = new Bundle
      bundle.add(bubblegum)
      bundle.add(magazine)
      println("ITEM: " + bundle.description + " costs " + bundle.price + " €")
    }
  }

  class Point(val x: Double, val y: Double) {
    def describe = "(" + x + "," + y + ")"
  }
  new Task("Task 5") {
    def solution() = {
      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x, y) {}
      val wallstreet = new LabeledPoint("Black Thursday", 1929, 230.07)
      println("\"" + wallstreet.label + "\" refers to a crash in " + wallstreet.x.toInt + " to " + wallstreet.y)
    }
  }

  new Task("Task 6") {
    def solution() = {
      abstract class Shape {
        def centerPoint : Point
      }
      class Rectangle(x: Double, y: Double, edge: Double) extends Shape {
        def centerPoint = new Point(x + edge/2, y + edge/2)
      }
      class Circle(x: Double, y: Double, radius: Double) extends Shape {
        def centerPoint = new Point(x, y)
      }
      val rect = new Rectangle(40, 19, 4)
      val circ = new Circle(42, 21, 5)
      println("RECTANGLE: " + rect.centerPoint.describe)
      println("CIRCLE: " + circ.centerPoint.describe)
    }
  }
}
