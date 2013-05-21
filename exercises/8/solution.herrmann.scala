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
  
  class BankAccount(initialBalance: Double){
        private var balance = initialBalance
        
        def deposit(amount: Double) = { balance += amount; balance }
        
        def withdraw(amount: Double) = { balance -= amount; balance }
  }

  new Task("Task 1") {
    def solution() = {
/* Extend the following BankAccount class to a CheckingAccount class that charges $1
 * for every deposit and withdrawal.
 */
      
      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        override def deposit(amount: Double) = { super.deposit(amount - 1) }
        
        override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
      }

    }
  }

  new Task("Task 2") {
    def solution() = {
/*
 * Extend the BankAccount class of the preceding exercise into a class SavingsAccount
 * that earns interest every month (when a method earnMonthlyInterest is called)
 * and has three free deposits or withdraws every month. Reset the transaction
 * count in the earnMonthlyInterest method.
 */
      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance) {
        private var freeTransactions = 3
        
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
        
        def earnMonthlyInterest(value: Double) = {
          freeTransactions = 3
          super.deposit(value)
        }        
      }

    }
  }

  new Task("Task 3") {
    def solution() = {
/*
 * Consult your favourite Java or C++ textbook that is sure to have an example
 * of a toy inheritance hierarchy, perhaps involving employees, pets, graphical
 * shapes, or the like. Implement the example in Scala
 */
 // Beispiel aus EiSE WS11/12
      abstract class Shape(var posx: Int, var posy: Int) {
        def paint()
      }
      
      class Square(var width: Int, var height: Int, var posx: Int, var posy: Int) extends Shape {
        def paint() = println("Paints a square")
      }
      
      class Circle(var diameter: Int, var posx: Int, var posy: Int) extends Shape {
        def paint() = println("Paints a Circle")
      }
      
      class Triangle(var length: Int, var posx: Int, var posy: Int) extends Shape {
        def paint() = println("Paints a Triangle")
      }

    }
  }

  new Task("Task 4") {
    import scala.collection.mutable.ListBuffer
    def solution() = {
      abstract class Item {
        def price(): Double
        def descripiton(): String
      }
      
      class SimpleItem(val description: String, val price: Double) extends Item{
        
      }

      class Bundle extends Item {
        private val items = new ListBuffer[Item]
        
        def price: Double = items.map(_.price).sum
        
        def add(item: Item) {
          items += item
        }
      }
     

    }
  }

  new Task("Task 5") {
    def solution() = {
      class Point(val x: Int, val y: Int) {
        
      }

      class LabeledPoint(val label: String, val x: Int, val y: Int) extends Point(x, y) {

      }      

    }
  }

  new Task("Task 6") {
    def solution() = {
    	class Point(val x: Int, val y: Int) {
        
    	}
      
    	abstract class Shape {
    	  def centerPoint(): Point
    	}
    	
    	class Rectangle(width: Int, height: Int) extends Shape {
    	  def centerPoint() = new Point((width / 2), (height / 2))
    	}
    	
    	class Circle(point: Point, radius: Int) {
    	  def centerPoint() = point
    	}

    }
  }

}
