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
      println("+ and -> have the same precedence, hence:")
      println("3 + 4 -> 5  ==  (3 + 4) -> 5")
      println("3 -> 4 + 5  ==  (3 -> 4) + 5")
      println("the later being an illegal operation")
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Money(cdollar: Int, ccent: Int) {
        private val value = (cdollar * 100) + ccent
        private def this(cvalue: Int) { this(cvalue / 100, cvalue % 100) }
        def dollar : Int = value / 100
        def cent : Int = value % 100
        private def inCents = value
        // beware: 0.3 is not 30ct, but 3ct in this display
        override def toString = "" + dollar + "." + cent + "$" //+ " (in " + inCents + "ct)"
        def +(other: Money) = new Money(inCents + other.inCents)
        def -(other: Money) = new Money(inCents - other.inCents)
        def *(other: Money) = new Money((inCents * other.inCents) / 100)
        def /(other: Money) = new Money((inCents / other.inCents) * 100)
        def ==(other: Money) = inCents == other.inCents
        def <(other: Money) = inCents < other.inCents
      }
      object Money {
        def apply(dollar: Int, cent: Int) = new Money(dollar, cent)
      }
      val m1 = Money(1, 75)
      val m2 = Money(0, 50)
      val m3 = m1 + m2
      val m4 = m1 - m2
      val m5 = m1 * m2
      val m6 = m1 / m2
      println(m1 + " < " + m2 + " " + m2 + " < " + m1 + "  =>  " + (m1 < m2) + " " + (m2 < m1))
      println(m1 + " + " + m2 + " == " + m3 + "  =>  " + (m1 + m2 == m3))
      println(m1 + " - " + m2 + " == " + m4 + "  =>  " + (m1 - m2 == m4))
      println("(no) implementation of * and / depends on what you want (not) from them.")
      println("We just do a little basic math here even if money rarely multiplies:")
      println(m1 + " * " + m2 + " == " + m5 + "  =>  " + (m1 * m2 == m5))
      println(m1 + " / " + m2 + " == " + m6 + "  =>  " + (m1 / m2 == m6))
    }
  }

  new Task("Task 7") {
    def solution() = {
      class BitSequence {
        private var seq: Long = 0
        def update(i: Int, b: Boolean) = if (b == true) seq |= (1.toLong << i) else seq &= ~(1.toLong << i)
        def apply(i: Int) = if ((seq & (1.toLong << i)) == 0) 0 else 1
        override def toString = 0.until(64).map(apply).mkString
      }
      var bit = new BitSequence
      println("BITS are: " + bit + " (start)")
      bit(0) = true
      println("BITS are: " + bit + " bit(0)")
      bit(1) = true
      println("BITS are: " + bit + " bit(1)")
      bit(42) = true
      println("BITS are: " + bit + " bit(42)")
      bit(2) = false
      println("BITS are: " + bit + " bit(2)")
      bit(1) = false
      println("BITS are: " + bit + " bit(1)")
    }
  }
}
