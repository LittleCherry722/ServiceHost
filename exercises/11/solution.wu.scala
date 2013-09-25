import scala.collection.mutable.ArrayBuffer
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

      // your solution for task 1 here
      println("-> has the same precedence as +, so the orders are (3 + 4) -> 5 and (3 -> 4) + 5")

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val m1 = new Money(30,40)
      val m2 = new Money(55,50)
      val m3 = new Money(20,60)
      println(m2 - m1)
      println(m1 + m3)
      println(m1 - m2)
      println(m1 * 2)
      println(m3 / 2)

    }
    
    class Money(x: Int, y: Int){
      val dollars = x
      val cents = y
      
      def toCents = x * 100 + y
      def toMoney(cents: Int) = new Money(cents / 100, Math.abs(cents % 100))
      
      def +(that: Money) = {
        var cents = this.toCents + that.toCents
        toMoney(cents)
      }
      
      def -(that: Money) = {
        var cents = this.toCents - that.toCents
        toMoney(cents)
      }
      
      def ==(that: Money) = {
        this.toCents == that.toCents
      }
      
      def <(that: Money) = {
        this.toCents < that.toCents
      }
      
      def *(scalar: Double) = {
        var cents = this.toCents * scalar
        toMoney(cents.toInt)
      }
      
      def /(den: Double) = {
        var cents = this.toCents / den
        toMoney(cents.toInt)
      }
      
      override def toString = "$" + dollars + "." + cents 
      
    }
  }

  

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      val x = new BitSequence(45L)
      x.show
      println("lowest bit is: " + x(63))
      x(62) = 1
      x.show

    }
    
    class BitSequence(value: Long) {
      var sequence = new ArrayBuffer[Int]()
      private var s = value.toBinaryString
     
      s = "0" * (64 - s.size) + s
      for (elem <- s) {
        sequence += elem.toString.toInt
      }
      
      def show() {
        println(sequence.mkString("<",",",">"))
      }
      
      def apply(n: Int) = {
        sequence(n)
      }
      
      def update(n: Int, bit: Int) {
        sequence(n) = bit
      }
      
    }
  }

  

}
