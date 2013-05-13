import scala.collection.mutable.ArrayBuffer

object Solution extends App {
  Tasks.execute()
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
    def deposit(amount: Double) = { balance += amount; balance }
    def withdraw(amount: Double) = { balance -= amount; balance }
  }

  new Task("Task 1") {
    def solution() = {

      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        override def deposit(amount: Double) = super.deposit(amount - 1)
        override def withdraw(amount: Double) = super.withdraw(amount + 1)
      }
      val a = new CheckingAccount(100)
      assert(a.deposit(1) == 100)
      assert(a.withdraw(1) == 98)
    }
  }

  new Task("Task 2") {
    def solution() = {
      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
    	val interest = .02
        var free = 3
        override def deposit(amount: Double) = super.deposit(amount - determineFee())
        override def withdraw(amount: Double) = super.withdraw(amount + determineFee())
        private def determineFee() = {
          if (free > 0) {
            free -= 1
            0
          } else 1
        }
        def earnMonthlyInterest() = {
          free = 3
          super.deposit(super.deposit(0)*interest)
        }
      }
      val a = new SavingsAccount(100)
      assert(a.deposit(1) == 101)	// #1
      assert(a.withdraw(1) == 100)	// #2
      assert(a.deposit(1) == 101)	// #3
      assert(a.deposit(1) == 101)	// -> 1$ fee
      assert(a.earnMonthlyInterest() == 103.02)	// 101 * 1.02
    }
  }

  new Task("Task 3") {
    def solution() = {
    	abstract class Animal {
    	  val sound: String
    	  def makeSound() { println(this.getClass.getSimpleName + ": " + sound) }
    	}
    	class Duck extends Animal { override val sound = "quack" }
    	class Dog extends Animal { override val sound = "BARK" }

    	val a: Animal = new Duck()
    	a.makeSound()
    	val a2: Animal = new Dog()
    	a2.makeSound()
    }
  }

  new Task("Task 4") {
    def solution() = {
    	abstract class Item {
    	  def price: Double
    	  def description: String
    	  override def toString = description
    	}
    	class SimpleItem(override val price: Double, override val description: String) extends Item
    	class Bundle extends Item {
    	  val items = ArrayBuffer[Item]()
    	  def add(item: Item) { items += item }
    	  override def price = items.map(_.price).sum
    	  override def description = items.map(_.description).mkString("; ")
    	}
    	val s1 = new SimpleItem(1.2, "Kaffee")
    	val s2 = new SimpleItem(1.8, "Muffin")
    	val b = new Bundle()
    	b.add(s1)
    	b.add(s2)
    	println(s"Bundle: [$b] (price: ${b.price})")
    }
  }

  new Task("Task 5") {
    def solution() = {
    	class Point(x: Double, y: Double)
    	class LabeledPoint(label: String, x: Double, y: Double) extends Point(x, y)
    }
  }

  new Task("Task 6") {
    def solution() = {
    	abstract class Shape {
    	  def centerPoint: (Double, Double)
    	}
    	class Rectangle(x1:Double, x2:Double, y1:Double, y2:Double) extends Shape {
    	  def centerPoint = ((x1+(x2-x1))/2, (y1+(y2-y1))/2)
    	}
    	class Circle(mid:(Double, Double), r:Double) extends Shape {
    	  def centerPoint = mid
    	}
    	val r1 = new Rectangle(0,10,0,5)
    	assert(r1.centerPoint==(5,2.5))
    	val c = new Circle((5,5), 10)
    	assert(c.centerPoint==(5,5))
    }
  }

}
