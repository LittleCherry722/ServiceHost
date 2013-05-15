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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      // 3 + 4 -> 5 <==> (3 + 4) -> 5
      // 3 -> 4 + 5 <==> (3 -> 4) + 5
    }
  }

  new Task("Task 4") {
    def solution() = {
      println(Money(1, 75) + Money(0, 50) == Money(2, 25))
      println(Money(1, 20) < Money(0, 95))

      // Providing simple * and / operators could be non intuitive for users.
      // For example Money(1, 20) * 2 would work as expected, but 2 * Money(1, 20) would invoke 2.*(Money(1, 20)),
      // which is not defined.
    }

    class Money(val dollars: Int, val cents: Int) {

      def +(other: Money) = {
        val centsNew = other.cents + this.cents
        Money(this.dollars + other.dollars + centsNew / 100, centsNew % 100)
      }

      def -(other: Money) = {
        val centsNew = totalCents - other.totalCents
        Money(centsNew / 100, centsNew % 100)
      }

      private def totalCents = dollars * 100 + cents

      def ==(other: Money) = totalCents == other.totalCents

      def <(other: Money) = totalCents < other.totalCents
    }

    object Money {
      def apply(dollars: Int, cents: Int) = new Money(dollars, cents)
    }

  }

  new Task("Task 7") {
    def solution() = {
      val seq = new BitSequence

      println(seq(3))
      seq(3) = true
      seq(4) = true
      seq(5) = false
      println(seq(3))
      seq(3) = false
      println(seq(3))
    }

    class BitSequence {
      private var bits = 0L

      def apply(pos: Int) = {
        require(pos >= 0 && pos < 64)

        val bit = 1 << pos
        (bit & bits) != 0
      }

      def update(pos: Int, set: Boolean) {
        require(pos >= 0 && pos < 64)

        val bit = 1 << pos

        if (set)
          bits = bits | bit
        else
          bits = bits & ~bit
      }
    }

  }
}
