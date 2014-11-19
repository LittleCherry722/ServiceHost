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
      println("+ and -> have the same precedence. both are left-associative, hence: ")
      assert(3 + 4 -> 5 == (3 + 4) -> 5)
      println("3 -> 4 + 5 === (3 -> 4) + 5 is a type mismatch, you cannot add scalar values to tuples.")

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
      class Money(val dollars: Int, val cents: Int) {
        def +(other: Money) = Money((dollars + other.dollars) * 100 + cents + other.cents)
        def -(other: Money) = Money((dollars - other.dollars) * 100 + cents - other.cents)
        def ==(other: Money) = dollars == other.dollars && cents == other.cents
        def <(other: Money) = dollars * 100 + cents < other.dollars * 100 + other.cents
      }

      object Money {
        def apply(cents: Int): Money = new Money(cents / 100, cents % 100)
        def apply(dollars: Int, cents: Int): Money = new Money(dollars, cents)
      }

      assert(Money(1, 75) + Money(0, 50) == Money(2, 25))

      println("* is not implemented because it is unclear what exactly Money * Money is supposed to mean. " +
      "/ could be used in order to determine the factor by which a sum exceeds another sum, e.g., " +
      "A / B == 2 if A is twice as much worth as B.")
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
      class BitSequence {
        var value: Long = 0
        def apply(i: Int): Long = {
          (value >> i) & 0x01
        }
        def update(pos: Int, bit: Int) {
          if (bit == 1) {
            value |= (1L << pos)
          }
          else if (bit == 0) {
            value &= ~(1L << pos)
          }
        }
      }
      val x = new BitSequence
      x(0) = 1
      x(1) = 1
      x(0) = 0
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
