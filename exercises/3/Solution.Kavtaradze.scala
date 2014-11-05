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

    }
  }

  new Task("Task 2") {
    def solution() = {
    	val arr = Array(1, 2, 3, 4, 5)
    	val newarr = arr.grouped(2).flatMap {
    		case Array(x, y) => Array(y, x)
    		case Array(x) => Array(x)
    		}.toArray

    	println(arr.mkString(" "))
    	print(newarr.mkString(" "))


    }
  }

  new Task("Task 3") {
    def solution() = {

      	   	def swap(arr: Array[Int]) = {
    for (i <- 0 until arr.length) yield (
        if (i % 2 == 0) 
        	if (i == arr.length - 1) arr(i) else arr(i + 1)
        else arr(i - 1)
    )
}
val t = Array(1, 2, 3, 4, 5);
val a = swap(t).toArray
println(t.mkString(" "))
print(a.mkString(" "))
	   

    }
  }

  new Task("Task 4") {
    def solution() = {

    	val a = Array(-1, 2, -3, -4, -5, 6, 7, 8, -9)
    	val b = a.partition {_ > 0}
    	val c = b._1 ++   b._2

    	println(a.mkString(" "))
    	println(c.mkString(" "))

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

      val a = Array("Alice", "Bob", "Charlie", "Charlie", "David", "David")
      val b = a.distinct

      println(a.mkString(" "))
      print(b.mkString(" "))

    }
  }

  new Task("Task 8") {
    def solution() = {

      val a = Array(1, -2, 3, 4, -5, -6, 7, -8)
      var indexes = for (i <- 0 until a.length if a(i) < 0) yield i
      indexes = indexes.reverse
      indexes = indexes.dropRight(1)

      val vals = for (i <- 0 to indexes.length){
      a.remove(indexes(i))
  
      }

      println(a.mkString(" "))
      print(indexes.toArray.mkString(" "))
      print(vals.toArray.mkString(" "))

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
