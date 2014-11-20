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
      println("+ and -> have the same precedence!")
      println("3 + 4 -> 5: 3 + 4 = x will be evaluated first and then the x -> 5")
      println("3 -> 4 + 5: 3 -> 4 = x will be evaluated first and then the x + 5")

    }
  }

 
  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      class Money(val dollars: Int, val cents: Int) {

        def +(other: Money): Money = {
            new Money(this.dollars + other.dollars, this.cents + other.cents)
          }
        
        def -(other: Money): Money = {
            new Money(this.dollars - other.dollars, this.cents - other.cents)
          }
        
        def ==(other: Money): Boolean = {
           this.dollars == other.dollars && this.cents == other.cents
          }
        
        def <(other: Money): Boolean = {
          if(this.dollars < other.dollars){
            return true
          }
          else if(this.dollars == other.dollars && this.cents < other.cents){
            return true
          }
          else{
            return false
          }
        }
        
        println("It is not recommanded to provide *(other: Money) or /(other: Money).")
        println("Reason: Money is an value with an unit which definition does not cover * or /!")
        println("But it is possible to provide a * or / function with a unitless value like: *(other: Int) or /(other: Int)")
      
    }
  }


  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

 

}
