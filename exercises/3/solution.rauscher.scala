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

  new Task("Task 2") {
    def solution() = {
      def swapArray(data : Array[Int]) = {
        for (i <- 0 until data.length if i % 2 != 0) {
          val tmp = data(i)
          data(i) = data(i-1)
          data(i-1) = tmp
        }
      }
      val nums = Array(1,2,3,4,5)
      println(nums.mkString("<",",",">"))
      swapArray(nums)
      println(nums.mkString("<",",",">"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      def swapArray(data : Array[Int]) = for (i <- 0 until data.length) yield data(if (i+1>=data.length) i else if (i%2==0) i+1 else i-1)
      val nums = Array(1,2,3,4,5)
      println(nums.mkString("<",",",">")) 
      println(swapArray(nums).mkString("<",",",">"))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def reorder(data : Array[Int]) = (for (i <- 0 until data.length if data(i) >= 0) yield data(i)) ++ (for (i <- 0 until data.length if data(i) < 0) yield data(i))
      val nums = Array(5,-2,2,4,0,-7)
      println(nums.mkString("<",",",">")) 
      println(reorder(nums).mkString("<",",",">"))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val array = Array(5,14,6,5,2,6,5)
      println(array.mkString("<",",",">"))
      println(array.distinct.mkString("<",",",">"))
    }
  }

  new Task("Task 8") {
    def solution() = {
      def operation(a : Array[Int]) = {
        val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
        for (j <- indexes.drop(1)) a.remove(indexes(j))
      }
      val nums = Array(5,2,-1,35,3,-5,2,-6)
      println(nums.mkString("<",",",">"))
      operation(nums)
      println(nums.mkString("<",",",">"))
    }
  }

}
