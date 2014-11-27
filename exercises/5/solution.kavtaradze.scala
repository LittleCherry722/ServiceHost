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
    	
    class counter {
      private var value = Int.MaxValue - 3
    	def increment() {
          if (value < Int.MaxValue) 
    	 value += 1
    	 }
   
      def current() = value
   
 }
 
    val myCounter = new counter
    myCounter.increment()
    println(myCounter.current())
    myCounter.increment()
    println(myCounter.current())
    myCounter.increment()
    println(myCounter.current())
    myCounter.increment()
    println(myCounter.current())

    }
  }

  new Task("Task 2") {
    def solution() = {

      class BankAccount{
   
      private var _balance: Double = 1000
   
      def deposit(amount: Double){
    	  _balance += amount
     
      }
   
      def withdraw(amount: Double){
    	  _balance -= amount
     
      }
   
      def balance = _balance
   
      }
 
      val a = new BankAccount

      println(a.balance)
      a.withdraw(100)
      println(a.balance)
      a.deposit(250.5)
      println(a.balance)


    }
  }

  new Task("Task 3") {
    def solution() = {

      class Time(private var _hrs : Int, private var _min: Int)  {
    	  def before(other: Time) = {
    		  if (_hrs < other._hrs) {
    			  if (_min < other._min)
    				  true
    		  }
        
    		  else 
    			  false
    	  }
   
    	  if (_hrs < 0)
    		  _hrs = 23
    	  if (_hrs > 23)
    		  _hrs = 0
          if (_min < 0)
        	  _min = 59
    	  if (_min > 59)
    		  _min = 0

    	  def hours = _hrs
    	  def minutes = _min
   
   
      }
 
	val time = new Time(10, 20)
 	val time2 = new Time(20, 30)
 	
	println(time2.before(time))
    print(time.before(time2))

    }
  }

  new Task("Task 4") {
    def solution() = {

      class Time(private var _hrs : Int, private var _min: Int)  {
    	  def before(other: Time) = {
    		  if (_minutes < other._minutes)
    			  true
    		  else 
    			  false
    	  }
   
   
    	  val _totalMinutes = 24 * 60 - 1
    	  var _minutes = (_hrs * 60) + _min
    	  def hours = _minutes / 60
    	  def minutes = _minutes % 60
   
   
   
   
      }
 
          val time = new Time(10, 20)
          val time2 = new Time(20, 30)
 	
          println(time2.before(time))
          print(time.before(time2))

    }
  }


  new Task("Task 6") {
    def solution() = {

      class Person(private var _age: Int) {
    	  if (_age < 0)
    		  _age = 0
     
          def age = _age
      }
    
      val person = new Person(-20)
      print(person.age)

    }
  }

  new Task("Task 8") {
    def solution() = {

      class Car(val Manufacturer: String, val Model: String, val Year: Int = -1, var Plate: String = "") {}
 
      val Audi = new Car("Audi", "R8", 2006, "DA-AU-R8")
      val BMW = new Car("BMW", "M6")
 
      println(Audi.Manufacturer +" "+ Audi.Model +" "+ Audi.Year +" "+Audi.Plate)
      println(BMW.Manufacturer +" "+ BMW.Model +" "+ BMW.Year +" "+BMW.Plate)

    }
  }

  new Task("Task 10") {
    def solution() = {

      class Employee(val name: String = "John Q. Public", var salary: Double = 0.0) {}

    }
  }

}
