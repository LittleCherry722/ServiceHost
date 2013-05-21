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



object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
//(3 + 4) -> 5
//(3 -> 4) + 5

    }
  }

  new Task("Task 4") {
    def solution() = {
      class Money(val dollars: Int, val cents: Int) {

      def +(m: Money) = {
        val newCents = m.cents + this.cents
        Money(this.dollars + m.dollars + newCents / 100, newCents % 100)
      }

      def -(m: Money) = {
        val newCents = total - m.total
        Money(newCents / 100, newCents % 100)
      }

      private def total = dollars * 100 + cents

      def ==(m: Money) = total == m.total

      def <(m: Money) = total < m.total
    }

    object Money {
      def apply(dollars: Int, cents: Int) = new Money(dollars, cents)
    }

      

    }
  }

  new Task("Task 7") {
    def solution() = {

      //???

    }
  }

}