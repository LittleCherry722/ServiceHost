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
  
    	  def deposit(amount: Double) = { 
    		  balance += amount 
    		  balance 
    	  }
  
    	  def withdraw(amount: Double) = { 
    		  balance -= amount 
    		  balance 
    	  }
  
    	  override def toString = balance.toString
      }

      class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance: Double) {
  
    	  override def deposit(amount: Double) = {
    		  super.deposit(amount - 1.0)
    	  }
  
    	  override def withdraw(amount : Double) = {
    		  super.withdraw(amount + 1.0)
    	  }
      }

      val a = new CheckingAccount(1000)
      println(a)
      a.deposit(100)
      println(a)
      a.withdraw(200)
      println(a)


    }
  }

  new Task("Task 2") {
    def solution() = {

      class BankAccount(initialBalance: Double) {
    	  private var balance = initialBalance
  
    	  def deposit(amount: Double) = { 
    		  balance += amount 
    		  balance 
    	  }
  
    	  def withdraw(amount: Double) = { 
    		  balance -= amount 
              balance 
    	  }
  
    	  override def toString = balance.toString
      }

      class SavingsAccount(initialBalance: Double) extends BankAccount(initialBalance: Double) {
  
    	  var transactions = 0
  
    	  def earnMonthlyInterest(percent: Double) = {
    		  val currentBalance = super.deposit(0)
    		 super.deposit(currentBalance * (percent / 100))
    	  }
  
    	  override def deposit(amount: Double) = {
    		  transactions += 1
    		  if (transactions <= 3)
    			 super.deposit(amount)
    		  else 
    			 super.deposit(amount - 1.0)
    	  }
  
    	  override def withdraw(amount : Double) = {
    		  transactions += 1
    		  if (transactions <= 3)
    			  super.withdraw(amount)
    		  else
    			  super.withdraw(amount + 1.0)
    	  }
      }

 
 
      val a = new SavingsAccount(1000)
      println(a)
      a.deposit(100)
      println(a)
      a.withdraw(200)
      println(a)
      a.earnMonthlyInterest(10)
      println(a)


    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

    	import scala.collection.mutable.ArrayBuffer
  
    	abstract class Item {
   
    		def price: Double
    		def description: String
   
    	}
 
    	class simpleItem(val price: Double, val description: String) extends Item {
        }
 
    	class Bundle(val description: String) {
    		private val items = new ArrayBuffer[Item]
  
    		def addItem(item: Item) {
    			items += item
    		}
  
    		def Sum : Double = {
    			var sum = 0.0
    			items.foreach(sum += _.price)
    			sum
    			}
    	}
 
    	val item1 = new simpleItem(700, "Galaxy S5")
        println(item1.description +" "+ item1.price)
 
        val item2 = new simpleItem(900, "iPhone 6S")
        println(item2.description+" "+ item2.price)
  
        val bundle = new Bundle("Mobiles")
    	bundle.addItem(item1)
    	bundle.addItem(item2)
    	println(bundle.description +" "+ bundle.Sum)

    }
  }

  new Task("Task 5") {
    def solution() = {

      class Point(val x: Double, val y: Double) {
      }

      class LabeledPoint(val label: String, x: Double, y: Double) extends Point(x, y) {
    	  override def toString = label +" "+ x +" "+ y
      }
 
      val a = new LabeledPoint("Black Thursday", 1929, 230.07)
      println(a)


    }
  }

  new Task("Task 6") {
    def solution() = {

      abstract class Shape(val x: Double, val y: Double){
    
    	  def centerPoint: (Double, Double)
  
      }
  
      class Rectangle(x: Double, y: Double, x2: Double, y2: Double) extends Shape (x, y){
    
    	  def centerPoint = {((x+x2)/2, (y+y2)/2)
	  
	  
    	  }  
      }
  
      class Circle(x: Double , y: Double, val radius: Double) extends Shape(x, y) {
  
    	  def centerPoint = {
    		  (x, y)
    	  }
      }
  
  
      val a = new Rectangle(5, 5, 10, 10)
      println(a.centerPoint)
  
  	  val b = new Circle(5, 5, 10)
      println(b.centerPoint)


    }
  }

}
