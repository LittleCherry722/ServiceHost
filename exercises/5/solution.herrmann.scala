object Solution extends App {

  // execute all tasks
  Tasks.execute();
 
  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name+":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute() }) }
  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  
  new Task("Task 1") {
    def solution() = {
/* Improve the Counter class in Section 5.1, “Simple Classes and Parameterless
Methods,” on page 51 so that it doesn’t turn negative at Int.MaxValue. */
    	class Counter {
    		private var value = 0

    		def increment() { 
    			value = if (value < Int.MaxValue) (value + 1) else 0
    		}

    		def current = value
    	}

    }
  }
  
  new Task("Task 2"){
	  def solutuion() = {
/* Write a class BankAccount with methods deposit and withdraw, and a read-only
property balance. */
		  class BankAccount {
			  private var credit : Double = 0

			  def deposit(n : Double) = {
			  	credit += n
		  	  }

		  	  def withdraw(n : Double) = {
			  credit -= n
		  	  }

		  	  def balance = credit
		  }

	  }
	  }
  
  new Task("Task 3"){
	  def solution() = {
/* Write a class Time with read-only properties hours and minutes and a method
before(other: Time): Boolean that checks whether this time comes before the
other. A Time object should be constructed as new Time(hrs, min), where hrs is in
military time format (between 0 and 23). */
		  class Time(private val hrs : Int, private val min : Int) {
			  def hours = if (hrs >= 0 && hrs <= 23) hrs else {
				  throw new IllegalArgumentException()
			  }

			  def minutes = if (min >= 0 && min <= 59) min else {
				  throw new IllegalArgumentException()
			  }
		
			  def before(other : Time) = {
				  if (this.hours < other.hours) true else {
					  if (this.hours == other.hours && this.minutes < other.minutes) true else {
						  false
					  }
				  }
			  }
		  }
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {
/* Reimplement the Time class from the preceding exercise so that the internal
representation is the number of minutes since midnight (between 0 and
24 × 60 – 1). Do not change the public interface. That is, client code should be
unaffected by your change. */
		  class Time(private val hrs : Int, private val min : Int) {
			  def minutes = if (hrs >= 0 && hrs <= 23 && min >= 0 && min <= 59) {
				  hrs * 60 + min
			  } else {
				  throw new IllegalArgumentException()
			  }

			  def before(other : Time) = {
				  if(this.minutes < other.minutes) true else false
			  }

		  }
	  }
  
	  new Task("Task 5"){
		  def solution() = {
/* In the Person class of Section 5.1, “Simple Classes and Parameterless Methods,”
on page 51, provide a primary constructor that turns negative ages to 0. */
			  class Person(private var n : Int) {
				  if (n < 0) n = 0

						  def age = n

						  def age_=(newAge: Int) {
					  		if (newAge > n) n = newAge; 
				  			}
			  }

		  }	
	  }
  
	  new Task("Task 6"){
		  def solution() = {
/* Make a class Car with read-only properties for manufacturer, model name,
and model year, and a read-write property for the license plate. Supply four
constructors. All require the manufacturer and model name. Optionally,
model year and license plate can also be specified in the constructor. If not,
the model year is set to -1 and the license plate to the empty string. Which
constructor are you choosing as the primary constructor? Why? */
			  class Car(var manufacturer : String,
					  var modelName : String,
					  var modelYear : Int = -1,
					  var licensePlate : String = "") {
	// this class doesn't need four constructors, because we use default values
	// in the primary constructor
			  }	 
	    
		  }
	  } 
   
	  new Task("Task 7"){
		  def solution() = {
/* Consider the class
class Employee(val name: String, var salary: Double) {
def this() { this("John Q. Public", 0.0) }
}
Rewrite it to use explicit fields and a default primary constructor. Which form
do you prefer? Why? */
			  class Employee(name : String = "John Q. Public", salary : Double = 0.0) {
				  val employeeName: String = name
						  var employeeSalary: Double = salary
			  }
	    
		  }
	  }  
  }