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
      println("3 + 4 -> 5")
      println("7 -> 5")
      println("(7, 5)")
      println("")
      println("3 -> 4 + 5")
      println("3 -> 9")
      println("(3, 9)")
    }
  }

  new Task("Task 2") {
    def solution() = {
    }
  }

  new Task("Task 3") {
    def solution() = {
    }
  }

  new Task("Task 4") {
    def solution() = {
      class Money(val d: BigInt, val c: BigInt) {
        // I do no input-validation because its not requiered.
        def +(that: Money) = {
          var newD = this.d + that.d + ((this.c + that.c) / 100)
          var newC = (this.c + that.c) % 100
          Money(newD, newC)
        }
        def -(that: Money) = {
          var newD = this.d - that.d - ((this.c - that.c) / 100)
          var newC = (this.c - that.c) % 100
          Money(newD, newC)
        }
        def ==(that: Money) = {
          this.d == that.d && this.c == that.c
        }
        override def toString() = {
          d + "," + c + "$"
        }
      }
      object Money {
        def apply(d: BigInt, c: BigInt) = {
          new Money(d, c)
        }
      }
      println("Money(1, 13) + Money(42, 90) evaluates to " + (Money(1, 13) + Money(42, 90)))
      println("Money(1, 13) - Money(42, 90) evaluates to " + (Money(1, 13) - Money(42, 90)))
      println("Money(42, 90) == Money(42, 90) evaluates to " + (Money(42, 90) == Money(42, 90)))
      println("")
      println("I wouldn't supply the operators * and / because I can't imagine" +
      		"a use case where that makes sense and I have never seen those" +
      		"operations used with two money-values.")
    }
  }

  new Task("Task 5") {
    def solution() = {
    }
  }

  new Task("Task 6") {
    def solution() = {
    }
  }

  new Task("Task 7") {
    def solution() = {
      class BitSequence(x: Long = 0) {
        if (x >= (2 ^ 65) || x < 0) throw new IllegalArgumentException("value must fit in 64 bytes")
        var value: Long = x
        def apply(position: Int) = {
          if (position > 64 || position < 0) throw new IllegalArgumentException("Argument has to be in the range 0-64")
          (value >> position) & 1L
        }
        def boolToLong(b: Boolean) = {
          if (b) 1L else 0L
        }
        def update(position: Int, newVal: Boolean) = {
          if (newVal)
            value |= (1L << position)
          else
            value &= ~(1L << position)
        }
        override def toString() = {
          String.format("%64s", value.toBinaryString).replace(' ', '0')
        }
      }
      println("var b = new BitSequence(10)")
      var b = new BitSequence(10)
      println("b = " + b)
      println("b(0) = true")
      println("b(1) = false")
      println("b(2) = false")
      println("b(3) = true")
      b(0) = true
      b(1) = false
      b(2) = false
      b(3) = true
      println("b = " + b)
    }
  }

  new Task("Task 8") {
    def solution() = {
    }
  }

  new Task("Task 9") {
    def solution() = {
    }
  }

  new Task("Task 10") {
    def solution() = {
    }
  }

}
