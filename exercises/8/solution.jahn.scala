import scala.collection.mutable.ListBuffer

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

  class BankAccount(initialBalance: Double) {
    private var balance = initialBalance

    def deposit(amount: Double) = { balance += amount; balance }

    def withdraw(amount: Double) = { balance -= amount; balance }
  }

  new Task("Task 1") {

    def solution() = {
      val account = new CheckingAccount(10)
      println(account.deposit(10))
    }

    class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      override def deposit(amount: Double): Double = super.deposit(amount - 1)

      override def withdraw(amount: Double): Double = super.withdraw(amount + 1)
    }

  }

  new Task("Task 2") {
    def solution() = {
      val account = new SavingsAccount(100)
      println(account.earnMonthlyInterest(10))
    }

    class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      private var freeDeposits = 3
      private var freeWithdraws = 3

      override def deposit(amount: Double): Double = {
        val newBalance = super.deposit(amount)

        if (freeDeposits > 0) {
          freeDeposits -= 1
          newBalance
        } else
          super.withdraw(1)
      }

      override def withdraw(amount: Double): Double = {
        val newBalance = super.withdraw(amount)

        if (freeWithdraws > 0) {
          freeWithdraws -= 1
          newBalance
        } else
          super.withdraw(1)
      }

      def earnMonthlyInterest(interest: Double) = {
        freeDeposits = 3
        freeWithdraws = 3

        super.deposit(interest)
      }
    }

  }

  new Task("Task 3") {
    def solution() = {
      var v: Vehicle = new Lamborgini
      v.drive()

      v = new Bicycle
      v.drive()
    }

    abstract class Vehicle {
      def drive()
    }

    class Car extends Vehicle {
      def drive() { println("Bruuuum bruuuuuum bruuuuuum") }
    }

    class Polo extends Car

    class Lamborgini extends Car {
      override def drive() { println("BRUUUUUUM BRUUUUUM BRUUUUUM") }
    }

    class Motorcycle extends Vehicle {
      def drive() { println("Rrrrrrrooooooo") }
    }

    class Bicycle extends Vehicle {
      def drive() { println("Wip wip wip wip") }
    }

  }

  new Task("Task 4") {

    def solution() = {
      val item1 = new SimpleItem(12.56, "Item A")
      val item2 = new SimpleItem(23.65, "Item B")
      val item3 = new SimpleItem(67.12, "Item C")
      val bundle = new Bundle

      bundle += item1
      bundle += item2
      bundle += item3

      println(bundle.price)
      println(bundle.description)
    }

    abstract class Item {
      def price: Double

      def description: String
    }

    class SimpleItem(val price: Double, val description: String) extends Item

    class Bundle extends Item {
      private val items = new ListBuffer[Item]

      def price: Double = items.map(_.price).sum

      def description: String = items.map(_.description).mkString("[", ", ", "]")

      def +=(item: Item) { items += item }
    }

  }

  new Task("Task 5") {
    def solution() = {
      val point = new LabeledPoint("Point 1", 45, 87)
      println(point.label + ": " + point.x + "," + point.y)
    }

    class Point(val x: Double, val y: Double)

    class LabeledPoint(val label: String, override val x: Double, override val y: Double) extends Point(x, y)

  }

  new Task("Task 6") {
    def solution() = {
      val rec = new Rectangle(new Point(0, 0), new Point(10, 30))
      val circle = new Circle(new Point(10, 10), 5)

      println("Rec center: " + rec.centerPoint)
      println("Circle center: " + circle.centerPoint)
    }

    class Point(val x: Double, val y: Double) {
      override def toString: String = "[" + x + ", " + y + "]"
    }

    abstract class Shape {
      def centerPoint: Point
    }

    class Rectangle(val a: Point, val b: Point) extends Shape {
      def centerPoint = new Point((a.x + b.x) / 2, (a.y + b.y) / 2)
    }

    class Circle(val center: Point, val r: Double) extends Shape {
      def centerPoint = center
    }

  }
}
