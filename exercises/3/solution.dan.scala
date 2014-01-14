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

      // your solution for task 6 here
      def mySwap(array: Array[Int]) {
        for (i <- 0 until (array.length - 1, 2)) {
          var temp = array(i)
          array(i) = array(i + 1)
          array(i + 1) = temp
        }
      }

      val a = Array(2, 3, 5, 7, 11)
      mySwap(a)
      print(a.mkString("<", ";", ">"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      val array = Array(2, 3, 5, 7, 11)
      val b = for (i <- 0 until (array.length,2); j <- Array(1,0) if i+j < array.length) yield array(i+j)
      println(b)
      // your solution for task 7 here
    }
  }

  new Task("Task 4") {
    def solution() = {

      
      val array = Array(9, 4,-6,-3, 2, 0, 5, 11)
      val sorted = array.sortWith((x,y) => x>0 && y <=0)
      print(sorted)
      
//      val pos = for (i <- 0 until (array.length,2) if i >0) yield array(i)
//      val neg = for (i <- 0 until (array.length,2) if i <=0) yield array(i)
      // your solution for task 8 here

    }
  }

  new Task("Task 7") {
    def solution() = {
    	val array = Array(9, 4,-6,0, 9, 0, 5, 11)
    	val b = array.distinct;
    	print(b)
      // your solution for task 9 here

    }
  }

  new Task("Task 8") {
    def solution() = {
      //Array doesn't have remove function
      val a = collection.mutable.ArrayBuffer(9, 4,-6,0, -2, 0, 5, 11)
      val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
      val filteredIndexes = indexes.drop(1) //drop first index
      for(i <- filteredIndexes)
    	  a.remove(i);
      print(a)
    }
  }

}
