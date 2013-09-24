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
			class Counter {
				private var value = 0
				def increment() {
					if (value != Int.MaxValue) value += 1
				}
				def current = value
			}
			val c = new Counter
			println("this takes some seconds...")
			for (_ <- 1 to Int.MaxValue) c.increment
			println(c.current)
			c.increment
			println(c.current)
		}
	}

	new Task("Task 2") {
		def solution() = {
			class BankAccount {
				private var balance = 0
				def deposit(amount: Int) {
					if (amount >= 0) balance += amount
				}
				def withdraw(amount: Int) {
					if (amount >= 0 && balance - amount >= 0) balance -= amount
				}
			}
			println("see code")
		}
	}

	new Task("Task 3") {
		def solution() = {
			class Time(private val hours: Int, private val minutes: Int) {
				def before(other: Time): Boolean = {
					60 * hours + minutes < 60 * other.hours + other.minutes
				}
			}
			println(new Time(4, 50).before(new Time(4, 55)))
		}
	}

	new Task("Task 4") {
		def solution() = {
			class Time(private val hours: Int, private val minutes: Int) {
				private val minsincemidnight = 60 * hours + minutes
				def before(other: Time): Boolean = {
					minsincemidnight < other.minsincemidnight
				}
			}
			println(new Time(4, 50).before(new Time(4, 55)))
		}
	}

	new Task("Task 6") {
		def solution() = {
			class Person(private var privateAge: Int) {
				if (privateAge < 0) privateAge = 0
				def age = privateAge
				def age_=(newValue: Int) {
					if (newValue > privateAge) privateAge = newValue
				}
			}
			val p = new Person(-42)
			println(p.age)
		}
	}

	new Task("Task 8") {
		def solution() = {
			class Car(private val manufacturer: String, private val modelName: String) {
				private var modelYear = -1
				private var licensePlate = ""
				def this (manufacturer: String, modelName: String, modelYear: Int) {
					this(manufacturer, modelName)
					this.modelYear = modelYear
				}
				def this (manufacturer: String, modelName: String, licensePlate: String) {
					this(manufacturer, modelName)
					this.licensePlate = licensePlate
				}
				def this (manufacturer: String, modelName: String, modelYear: Int, licensePlate: String) {
					this(manufacturer, modelName)
					this.modelYear = modelYear
					this.licensePlate = licensePlate
				}
			}
			println("see code")
		}
	}
}