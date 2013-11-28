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
      def indices(chars: String) = {
        for (char <- chars) {
          println(char)
        }
      }
      println(indices("Mississippi"))
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

}
