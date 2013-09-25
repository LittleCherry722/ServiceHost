import scala.collection.mutable.ArrayBuffer

object Solution extends App {
	Tasks.execute();
}

abstract class Task(val name: String) {
	Tasks add this
	def solution();
	def execute() {
		println(name + ":");
		solution();
		println("\n");
	}
}

class Tasks {
	private var tasks = Seq[Task]();
	def add(t: Task) = { tasks :+= t }
	def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
	def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

object Tasks extends Tasks {
	new Task("Task 1") {
		def solution() = {
			class BankAccount(initialBalance: Double) {
				private var balance = initialBalance
				def deposit(amount: Double) = { balance += amount; balance }
				def withdraw(amount: Double) = { balance -= amount; balance }
			}
			class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
				override def deposit(amount: Double) = { super.deposit(amount - 1) }
				override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
			}
		}
	}

	new Task("Task 2") {
		def solution() = {
			class BankAccount(initialBalance: Double) {
				private var balance = initialBalance
				def deposit(amount: Double) = { balance += amount; balance }
				def withdraw(amount: Double) = { balance -= amount; balance }
			}
			class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
				var freeTransactions = 3
				override def deposit(amount: Double) = {
					freeTransactions -= 1
					super.deposit(if (freeTransactions > 0) amount else amount - 1)
				}
				override def withdraw(amount: Double) = {
					freeTransactions -= 1
					super.withdraw(if (freeTransactions > 0) amount else amount - 1)
				}
				def earnMonthlyInterest {
					freeTransactions = 3;
					super.deposit(super.deposit(0) * 1.01) // 1% interest
				}
			}
		}
	}

	new Task("Task 4") {
		def solution() = {
			abstract class Item {
				def price: Double
				def description: String
			}
			class SimpleItem(val price: Double, val description: String) extends Item
			class Bundle extends Item {
				val contents = new ArrayBuffer[Item]
				def price = {
					contents.map(_.price).sum
				}
				def description = {
					"a Bundle of " + contents.map(_.description).mkString(" and ")
				}
				def addItem(a: Item) {
					contents += a
				}
			}
			val b: Bundle = new Bundle
			b.addItem(new SimpleItem(1, "bottle"))
			b.addItem(new SimpleItem(2, "spoon"))
			println(b.price)
		}
	}

	new Task("Task 5") {
		def solution() = {
			class Point(val x: Double, val y: Double)
			class LabeledPoint(val desc: String, x: Double, y: Double) extends Point(x, y)
		}
	}

	new Task("Task 6") {
		def solution() = {
			class Point(val x: Double, val y: Double)
			abstract class Shape {
				def centerPoint: Point
			}
			class Circle(val centerPoint: Point, val radius: Double) extends Shape
			class Rectangle(val pos: Point, val width: Double, val height: Double) extends Shape {
				def centerPoint = {
					new Point(pos.x + width / 2, pos.y + height / 2)
				}
			}			
		}
	}
}