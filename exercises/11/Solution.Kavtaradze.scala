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

      println("(3 + 4) and then -> 5")
      print("(3 -> 4) and then + 5")

    }
  }


  new Task("Task 4") {
    def solution() = {

      object Money {
    	  def apply(d: Int, c: Int) = new Money(d, c)
}

      class Money(val d: Int, val c: Int) {
    	  val total: Int = d * 100 + c 
    	  val dollars: Int = total / 100
    	  val cents: Int = total % 100 
  
  
    	  def +(that: Money) = new Money(this.dollars + that.dollars, this.cents + that.cents)
          def -(that: Money) = new Money(this.dollars - that.dollars, this.cents - that.cents)
          def <(that: Money) = this.total < that.total
          def >(that: Money) = this.total > that.total
          def ==(that: Money) = this.total == that.total
  
          override val toString = dollars + "$ " + cents + "C"
  
      }

      var a = new Money(1, 75)
      var b = new Money(0, 50)
      var c = a + b  
      var d = a - b 
      var e = a == b
  
      println(c)
      println(d)
      println(e)

    }
  }


  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }


}
