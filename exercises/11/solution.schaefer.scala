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

      println("3 + 4 -> 5	==	(3 + 4) -> 5")
      println("3 -> 4 + 5	==	(3 -> 4) + 5")
      println("Both operators have the same precedence")

    }
  }

  new Task("Task 4") {
    def solution() = {
      println("The * operator doesn't make much sense within the Money class because")
      println("you wouldn't want to multiply two Money objects: Money(1, 75) * Money(0, 50)")
      println("Defining * for Money and an Int would provide the possibility to, say compute Money(1, 75) * 2")
      println("Nonetheless, calling 2 * Money(1, 75) would call Int.* which is not defined for Money objects as second argument.")
      println("Thus, defining * would not be very intuitive.")
      println("The / operator doesn't make much sense either because Money(0, 12) / 5 would result in an inaccurate result.")
    }

    class Money(val dollars: Int, val cents: Int) {
      val value = dollars * 100 + cents

      def +(other: Money): Money = {
        new Money(this.dollars + other.dollars, this.cents + other.cents)
      }

      def -(other: Money): Money = {
        new Money(this.dollars - other.dollars, this.cents - other.cents)
      }

      def ==(other: Money): Boolean = {
        this.value == other.value
      }

      def <(other: Money): Boolean = {
        this.value < other.value
      }
    }
  }

  new Task("Task 7") {
    def solution() = {
    	val sequence = new BitSequence(1101L)
    	println(sequence.apply(3))
    	println(sequence.apply(2))
    	println(sequence.apply(1))
    	println(sequence.apply(0))
    	sequence.update(0, false)
    	println(sequence.apply(0))
    }

    class BitSequence {
      def this(bitSequence: Long) {
        this()
        bits = bitSequence
      }
      var bits: Long = 0L
      def apply(pos: Int): Boolean = {
        val shifted = bits >> pos
        (shifted & 1) == 1L
      }

      def update(pos: Int, value: Boolean) {
        val newVal = 1L << pos

        if (value) {
        	bits |= newVal
        } else {
        	bits &= ~newVal
        }
      }
    }
  }

}
