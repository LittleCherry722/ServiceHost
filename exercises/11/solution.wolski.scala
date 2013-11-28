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
      
      println("3 + 4 -> 5 is evaluated as ((3 + 4) -> 5)")
      println("3 -> 4 + 5 is evaluated as ((3 -> 4) + 5)")

    }
  }

  new Task("Task 4") {
    class Money(val amount: Int) {
      def this(dollars: Int, cents: Int) {
        this(dollars*100 + cents)
      }


      def +(other: Money): Money = { new Money(this.amount + other.amount) }
      def -(other: Money): Money = { new Money(this.amount - other.amount) }
      def ==(other: Money): Boolean = { this.amount == other.amount }
      def <(other: Money): Boolean = { this.amount < other.amount }
      
      override def toString: String = {
        val dollars = this.amount / 100
        val cents = math.abs(this.amount % 100)
        val cents_str = if (cents < 10) "0"+cents else cents
        dollars+"."+cents_str+" $"
      }
    }

    object Money {
      def apply(dollars: Int, cents: Int) = { new Money(dollars, cents) }
    }

    def solution() = {
      /*
      var m1 = new Money(1, 0)
      var m2 = new Money(-2, 0)
      var m3 = new Money(0, 50)
      var m4 = new Money(0, 1)

      println("m1: " + m1);
      println("m1 + m2: " + (m1 + m2));
      println("m1: " + m1);
      m1 += m2
      println("m1 += m2: " + m1);

      println("")
      println("m3: " + m3)
      println("m3 + m3: " + (m3+m3))
      println("m3 + m3 + m3: " + (m3+m3+m3))
      println("m3: " + m3)
      
      println("m4: " + m4)

      println("m1-m2 == m3+m3: " + ((m1-m2)==(m3+m3)));
      println("m4 < m3: " + (m4<m3));
      */

      println("(Money(1, 75) + Money(0, 50) == Money(2, 25)): " + (Money(1, 75) + Money(0, 50) == Money(2, 25)))

      println("I would not provide (Money*Money->Money), as money can not be multiplied with money. (Money*Double->Money) would make sense, therefore also (Money/Money->Double)");
    }
  }

  new Task("Task 7") {
    class BitSequence {
       var data: Long = 0
       val zero: Long = 0
       val one: Long = 1
       val not_one: Long = -2
       
       def apply(n: Int): Boolean = {
         (this.data & (one<<n)) != zero
       }

       def update(n: Int, value: Boolean): Unit = {
         if (value) { this.data |= (one<<n) } else { this.data &= (not_one<<n) }
       }

       override def toString = this.data.toBinaryString
    }

    def solution() = {
      val seq = new BitSequence
      println("seq: "+seq)
      seq.update(0, true)
      println("seq: "+seq)
      seq.update(2, true)
      println("seq: "+seq)
      seq.update(0, false)
      println("seq: "+seq)
      seq.update(40, true)
      println("seq: "+seq)
      println("seq(2): "+seq(2))
    }
  }

}
