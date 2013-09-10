object Solution extends App {
  Tasks.execute()
}

abstract class Task(val name: String) {
  Tasks.add(this)

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = tasks :+= t
  def execute() = tasks foreach { _.execute() }
  def execute(name: String) = (tasks filter { _.name == name } head).execute()
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
      object Money {
        def apply(d: Int, c: Int) = new Money(d, c)
      }
      class Money(val d: Int, val c: Int) {
        def this(c: Int) = this(c / 100, c % 100)
        private def toCents = d * 100 + c
        def +(other: Money) = new Money(this.toCents + other.toCents)
        def -(other: Money) = new Money(this.toCents - other.toCents)
        def ==(other: Money) = this.toCents == other.toCents
        def <(other: Money) = this.toCents < other.toCents
        override def toString = s"Money($d, $c)"
      }
      assert(Money(1, 75) + Money(0, 50) == Money(2, 25))
    }
  }

  new Task("Task 7") {
    def solution() = {
      class BitSequence {
        private var seq: Long = 0L
        def apply(pos: Int): Boolean = (seq & (1 << pos)) != 0
        def update(pos: Int, value: Int) = seq |= (value << pos)
        override def toString() = seq.toBinaryString.reverse
      }
      var s = new BitSequence()
      s(62) = 1
      s(0) = 1
      s(3) = 1
      println(s)
    }
  }

}
