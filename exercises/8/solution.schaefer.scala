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
  def execute() = { tasks.foreach((t: Task) => { t.execute() }) }
  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */
//defined up here since BankAccount is required for tasks 1 and 2
class BankAccount(initialBalance: Double) {
  private var balance = initialBalance
  def deposit(amount: Double) = { balance += amount; balance }
  def withdraw(amount: Double) = { balance -= amount; balance }
}

//defined up here since Point is required for tasks 5 and 6
class Point(val x: Int, val y: Int) {}

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

    }
    class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      override def deposit(amount: Double) = { super.deposit(amount - 1) }
      override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
    }
  }

  new Task("Task 2") {
    def solution() = {
      val account = new SavingsAccount(5)
      println(account.withdraw(1))
      for (i <- 0 to 2) {
        println(account.deposit(1))
      }
      println(account.withdraw(1))
    }
    class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      private var freeTransactions = 3

      def earnMonthlyInterest(interestRate: Double) = {
        freeTransactions = 3
        val balance = super.deposit(0)
        super.deposit(balance * interestRate)
      }

      override def deposit(amount: Double) = {
        if (freeTransactions > 0) {
          freeTransactions -= 1
          super.deposit(amount)
        } else {
          super.deposit(amount - 1)
        }
      }

      override def withdraw(amount: Double) = {
        if (freeTransactions > 0) {
          freeTransactions -= 1
          super.withdraw(amount)
        } else {
          super.withdraw(amount + 1)
        }
      }
    }
  }

  new Task("Task 3") {
    def solution() = {
      new Circle().print
      new Rectangle().print
      new Square().print
      new Dot().print
    }

    abstract class Graphic {
      def print()
    }

    class Circle extends Graphic {
      def print() = println("○")
    }

    class Rectangle extends Graphic {
      def print() = println("█")
    }

    class Square extends Rectangle {
      override def print() = println("■")
    }

    class Dot extends Graphic {
      def print() = println("•")
    }
  }

  new Task("Task 4") {
    def solution() = {
      val item = new SimpleItem("Brick", 2)
      val item2 = new SimpleItem("Window", 5)
      val bag = new Bundle
      bag.add(item)
      bag.add(item2)

      println(item.description + ": " + item.price)
      println(item2.description + ": " + item2.price)
      println(bag.description + ": " + bag.price)

    }
    abstract class Item {
      def price(): Double
      def description(): String
    }

    class SimpleItem(val description: String, val price: Double) extends Item

    class Bundle extends Item {
      val items = new ListBuffer[Item]
      def price() = {
        items.map(_.price).sum
      }
      def description() = {
        val sb = new StringBuilder()
        sb.append("Bundle containing ")
        sb.append(items.map(_.description).mkString(", "))
        sb.toString
      }
      def add(item: Item): Boolean = {
        items += item
        true
      }
    }
  }

  new Task("Task 5") {
    def solution() = {
      val point = new Point(4, 2)
      val labeledPoint = new LabeledPoint("Hello World", 42, 24)
      println(point.x + "|" + point.y)
      println(labeledPoint.label + ": " + labeledPoint.x + "|" + labeledPoint.y)
    }

    class LabeledPoint(val label: String, override val x: Int, override val y: Int) extends Point(x, y) {

    }
  }

  new Task("Task 6") {
    def solution() = {
      val point1 = new Point(2, 4)
      val point2 = new Point(4, 2)

      val rect = new Rectangle(point1, point2)
      val circ = new Circle(point2, 42)

      println(rect.centerPoint.x + "|" + rect.centerPoint.y)
      println(circ.centerPoint.x + "|" + circ.centerPoint.y)
    }

    abstract class Shape {
      def centerPoint(): Point
    }

    class Rectangle(point1: Point, point2: Point) extends Shape {
      def centerPoint() = new Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2)
    }
    class Circle(point: Point, radius: Double) extends Shape {
      def centerPoint() = point
    }
  }

}
