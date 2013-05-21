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
      // 3 + 4 -> 5 is evaluated as (3 + 4) -> 5
      // 3 -> 4 + 5 is evaluated as (3 -> 4) + 5, type mismatch
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
      class Money(var dollars: Int, var cents: Int) {
        def +(that: Money) = {
          var c = this.cents + that.cents
          var d = this.dollars + that.dollars + (c / 100)
          c = c % 100
          Money(d, c)
        }

        def -(that: Money) = {
          var c = this.cents - that.cents
          var d = this.dollars - that.dollars + (c / 100)
          c = c % 100
          Money(d, c)
        }

        def ==(that: Money) = (this.dollars == that.dollars && this.cents == that.cents)

        def <(that: Money) = {
          var m1 = this.dollars * 100 + this.cents
          var m2 = that.dollars * 100 + that.cents
          m1 < m2
        }

        override def toString() = this.dollars + "." + this.cents
      }

      object Money {
        def apply(d: Int, c: Int) = new Money(d, c)
      }

      println(Money(1, 75) + Money(0, 50))
      println(Money(1, 75) + Money(0, 50) == Money(2, 25))
      // we shouldn't supply * and / operators because they don't defined with money
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
        var sequence: Long = 0
        def apply(index: Int) = {
          (sequence >> index) % 2
        }
        def update(index: Int, value: Int) {
          val v = value % 2
          sequence = sequence - (this.apply(index) << index)
          sequence = sequence + (v << index)
        }

        override def toString = {
          val indices = for (i <- 0 to 15) yield i
          val bits = indices.map(i => this.apply(i))
          bits.mkString("")
        }
      }

      val bs = new BitSequence
      bs(0) = 1
      bs(1) = 1
      bs(10) = 1
      bs(7) = 1
      println(bs)
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
