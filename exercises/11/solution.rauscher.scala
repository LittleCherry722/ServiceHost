import scala.collection.JavaConverters._

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
      // (3 + 4) -> 5
      // (3 -> 4) + 5
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Money(dollars: Int, cents: Int) {
        def value = dollars * 100 + cents
        def +(m: Money) = {
          val value = this.value + m.value
          new Money(value / 100, value % 100)
        }
        def -(m: Money) = {
          val value = this.value - m.value
          new Money(value / 100, value % 100)
        }
        def ==(m: Money) = value == m.value
        def <(m: Money) = value < m.value
        // * and/or / operators are not useful with Money-Instances. Only an implementation accepting Ints would be useful
      }
      object Money {
        def apply(dollars: Int, cents: Int) = new Money(dollars, cents)
      }
      println(Money(1, 75) + Money(0, 50) == Money(2, 25))
    }
  }

  new Task("Task 7") {
    def solution() = {
      class BitSequence {
        var value: Long = 0
        
        def apply(n: Int) = value >> n & 1
        def update(n: Int, value: Boolean) = this.value = if (value) this.value | (1 << n) else this.value & (0 << n) fi
      }
      val b = new BitSequence()
      println(b(3))
      b(3) = true
      println(b(3))
    }
  }

}
