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
       class BankAccount(initialBalance: Double) {
	     private var balance = initialBalance
	     def deposit(amount: Double) = { balance += amount; balance }
	     def withdraw(amount: Double) = { balance -= amount; balance }
       }
   
   
   		class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
   		  override def deposit(amount: Double) = { this.deposit(amount - 1)}
   		  override def withdraw(amount: Double) = { this.withdraw(amount + 1)}
   		}

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
       class BankAccount(initialBalance: Double) {
	     private var balance = initialBalance
	     def deposit(amount: Double) = { balance += amount; balance }
	     def withdraw(amount: Double) = { balance -= amount; balance }
       }
   
   
   		class SavingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
   		  var number_of_transactins : Int = 0
   		  
   		  def earnMonthlyInterest() = { number_of_transactins = 0}
   		  
   		  override def deposit(amount: Double) = {
   		    var fee : Double = 0
   		    if(number_of_transactins > 4){
   		    	fee = 1
   		    }
   		    number_of_transactins += 1
   		    
   		    this.deposit(amount - fee)
   		  }
   		  
   		  override def withdraw(amount: Double) = {
   		    var fee : Double = 0
   		    if(number_of_transactins > 4){
   		    	fee = 1
   		    }
   		    number_of_transactins += 1
   		    
   		    this.withdraw(amount + fee)
   		  
   		  }
   		}
	  

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
        class Box{
    	  var width: Double = 0
    	  var height: Double = 0
    	  var depth: Double = 0
    	  
    	  def this(w: Double, h: Double, d: Double){
    	    this()
    	    this.width = w
    	    this.height  = h
    	    this.depth = d
    	  }
    	  
    	  def getVorlume(){
    	    println("Volume is : " + width * height * depth)
    	  }
    	  
    	}
    	
    	class MatchBox extends Box{
    	  var weight: Double = 0
    	  
    	  def this(w: Double, h: Double, d: Double, m: Double){
    	    this()
    	    super(w, h, d)
    	  }
    	  
    	}

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      abstract class Item{
    	  
    	  def price: Double
    	  def description: String
    	}
    	
    	class SimpleItem(override val price: Double, description: String) extends Item{
    	  
    	  
    	}

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      class Point(x: Double, y:Double){
    	  
    	}
    	
    	class LabeledPoint(labe: String, x: Double, y: Double) extends Point(x, y){
    	  
    	}

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      abstract class Shape(){
    	  def CenterPoint: Double
    	}
    	
    	class Square extends Shape{
    	  
    		def CenterPoint: Double = {
    		  0
    		}
    	}
    	
    	class Circle extends Shape{
    	  
    	  def CenterPoint: Double = {
    	    0
    	  }
    	}

    }
  }


}
