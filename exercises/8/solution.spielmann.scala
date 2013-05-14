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
        override def toString() = { "Balance: " + balance}
      }
      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance: Double) {
        override def deposit(amount: Double) = { super.deposit(amount - 1)}
        override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
      }
      println("var account = new CheckingAccount(10)")
      var account = new CheckingAccount(10)
      println(account)
      println("deposit 5$")
      account.deposit(5)
      println(account)
      println("withdraw 10$")
      account.withdraw(10)
      println(account)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class BankAccount(initialBalance: Double) {
        private var balance = initialBalance
        def deposit(amount: Double) = { balance += amount; balance }
        def withdraw(amount: Double) = { balance -= amount; balance }
        override def toString() = { "Balance: " + balance}
      }
      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance: Double) {
        private var freeActions = 3
        def earnMonthlyInterest() = {
          freeActions = 3
        }
        override def deposit(amount: Double) = {
          if (freeActions > 0) {
            freeActions -= 1
            super.deposit(amount)
          }
          else
            super.deposit(amount - 1)
        }
        override def withdraw(amount: Double) = {
          if (freeActions > 0) {
            freeActions -= 1
            super.deposit(amount)
          }
          else
            super.deposit(amount - 1)
        }
      }
      println("var account = new SavingsAccount(10)")
      var account = new SavingsAccount(10)
      println(account)
      println("deposit 5$")
      account.deposit(5)
      println(account)
      println("withdraw 10$")
      account.withdraw(10)
      println(account)
      println("withdraw 10$")
      account.withdraw(10)
      println(account)
      println("withdraw 10$")
      account.withdraw(10)
      println(account)
      println("account.earnMonthlyInterest")
      account.earnMonthlyInterest
      println(account)
      println("withdraw 10$")
      account.withdraw(10)
      println(account)
    }
  }

  new Task("Task 3") {
    def solution() = {
      // Java ist auch eine Insel
      class Disko {
        private var anzahlLeute : Int = 0
        def anzahlPersonene = anzahlLeute
        def personRein() = {
          anzahlLeute += 1
        }
        def personRaus() = {
          anzahlLeute -= 1
        }
      }
      class KinderDisko(var maskottchen : String) extends Disko {
      }
      println("var kd = new KinderDisko(\"Elchi der Elch\")")
      var kd = new KinderDisko("Elchi der Elch")
      println("kd.maskottchen: " + kd.maskottchen)
    }
  }

  new Task("Task 4") {
    def solution() = {
      abstract class Item {
        def price() : Double
        def description() : String
      }
      class SimpleItem(val price : Double, val description : String) extends Item
      class Bundle extends Item {
        var items = List[Item]()
        def addItem(i : Item) = {
          items = i :: items
        }
        override def price() = {
          items.foldLeft(0.0)((a,b) => a + b.price())
        }
        override def description() = {
          items.tail.foldLeft(items.head.description)((a,b) => a + ", " + b.description)
        }
      }
      println("var i1 = new SimpleItem(10.0, \"10 Euro Schein\")")
      println("var i2 = new SimpleItem(20.0, \"20 Euro Schein\")")
      var i1 = new SimpleItem(10.0, "10 Euro Schein")
      var i2 = new SimpleItem(20.0, "20 Euro Schein")
      println("i1.description: " + i1.description + ", i1.price: " + i1.price)
      println("i2.description: " + i2.description + ", i2.price: " + i2.price)
      println("var b = new Bundle()")
      println("b.addItem(i1)")
      println("b.addItem(i2)")
      var b = new Bundle()
      b.addItem(i1)
      b.addItem(i2)
      println("b.description: " + b.description + ", b.price: " + b.price)
    }
  }

  new Task("Task 5") {
    def solution() = {
      class Point(var x: Double, var y: Double) {
        override def toString() = {
          "(" + x + ", " + y + ")"
        }
      }
      class LabeledPoint(var label : String, x: Double, y: Double) extends Point(x: Double, y: Double) {
        override def toString() = {
          label + ":" + super.toString()
        }
      }
      println("var point = new LabeledPoint(\"nice point\", 2.3, 4.2)")
      var point = new LabeledPoint("nice point", 2.3, 4.2)
      println("point.toString() = " + point)
    }
  }

  new Task("Task 6") {
    def solution() = {
      // Point-class from task 5
      class Point(var x: Double, var y: Double) {
        override def toString() = {
          "(" + x + ", " + y + ")"
        }
      }
      // every shape needs a position (point(0,0) is in the upper left corner and y goes down positive)
      // position is a point to start drawing (e.g. centerpoint for circle, upper left corner for rectangle)
      abstract class Shape(var position: Point){
        def centerPoint(): Point
      }
      class Rectangle(position : Point, var width: Double, var height: Double) extends Shape(position: Point){
        def centerPoint(): Point = {
          var x = position.x + (width / 2)
          var y = position.y + (height / 2)
          new Point(x, y)
        }
      }
      class Circle(position: Point, var radius : Double) extends Shape(position: Point){
        def centerPoint(): Point = {
          position
        }
      }
      println("var p = new Point(10.0, 20.0)")
      println("var r = new Rectangle(p, 4.0, 6.0)")
      println("var c = new Circle(p, 5.0)")
      var p = new Point(10.0, 20.0)
      var r = new Rectangle(p, 4.0, 6.0)
      var c = new Circle(p, 5.0)
      println("r.centerPoint = " + r.centerPoint)
      println("c.centerPoint = " + c.centerPoint)
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
