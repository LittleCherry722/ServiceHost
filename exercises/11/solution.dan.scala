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
      println("3+4->5  =>  (3+4) -> 5")
      println("3->4+5  =>  3 -> (4+5)")
    }
  }

  new Task("Task 4") {
    def solution() = {
      case class Money(var dollar: Int, var cent: Int) {
        require(dollar >= 0 && cent >= 0 && cent < 100)
        def +(m: Money) = {
          var sCent = cent + m.cent
          val sDollar = dollar + m.dollar + sCent / 100
          sCent %= 100
          new Money(sDollar, sCent)
        }
        def ==(m: Money): Boolean = {
          dollar == m.dollar && cent == m.cent
        }
        def <(m: Money): Boolean = {
          (dollar < m.dollar) || (dollar == m.dollar && cent < m.cent)
        }

        def -(m: Money) = {
          if (this < m) {
            new Money(0, 0)
          } else if (this == m) {
            new Money(0, 0)
          } else {
            val cents = dollar * 100 + cent - m.dollar * 100 - m.cent
            new Money(cents / 100, cents % 100)
          }
        }
        override def toString: String = dollar + "$ " + cent
      }
      assert(Money(1, 75) + Money(0, 50) == Money(2, 25))
      assert(Money(1, 75) < Money(3, 50))
      assert(Money(1, 75) == Money(1, 75))
      assert((Money(2, 75) - Money(1, 90) == Money(0, 85)))
    }
  }

  new Task("Task 7") {
    def solution() = {
      class BitSequence() {
        var b: Long = 0L

        override def toString = b.toBinaryString

        def apply(i: Int): Int = (((1L << i) & b) >> i).asInstanceOf[Int]
        def update(i: Int, value: Int) {
          if (value == 1)
            b |= (1L << i)
          else if (value == 0)
            b &= ~(1L << i)
        }
      }
    }
  }

}
