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

    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      class Money(val dollars: Int, val cents: Int) {
        def +(m: Money)  = {
          if (cents + m.cents >= 100)
            new Money(dollars + 1 + m.dollars, (cents + m.cents) % 100)
          else new Money(dollars + m.dollars, cents + m.cents )
        } 
        def -(m: Money)  = {
          if (cents - m.cents < 0)
            new Money(dollars - 1 - m.dollars, (cents - m.cents) % 100)
          else new Money(dollars - m.dollars, cents - m.cents )
        }
        
        def ==(m: Money): Boolean = {if ((dollars == m.dollars) && (cents == m.cents )) true else false}
        
        def <(m: Money): Boolean = {
          if (dollars < m.dollars ) true
          else if ((dollars == m.dollars) && (cents < m.cents) ) true
          else false
        }
      }
      
      val m1 = new Money(1, 75)
      val m2 = new Money(0, 50)
      val m3 = new Money(0, 80)
      println((m1 + m2).dollars)
      println((m1 - m3).dollars)
      println(m1 + m2 == new Money(2,25))

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here

    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {

      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {

      // your solution for task 10 here

    }
  }

}
