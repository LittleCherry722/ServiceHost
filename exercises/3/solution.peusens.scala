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

      // your solution for task 2 here
      def swap(a: Array[Int]) = {
        for(i <- 0 until (a.length, 2)){
	      val temp = a(i)
	      a(i) = a(i+1)
	      a(i+1) = tmp
	    }
      }
      
      val a = Array(1, 1, 2, 3, 5, 8, 13, 21)
      swap(a)
      
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      def swap(a: Array[Int]): Array[Int] = {
        val b = scala.collection.mutable.ArrayBuffer[Int]()
        for (i <- 0 until (a.length, 2)) yield {
          if (i != (a.length - 1)){
            b += a(i + 1)
        	b += a(i)
          } else {
            b += a(i)
          }
        }
        
        b.toArray
      }
      
      
      val a = Array(1, 1, 2, 3, 5, 8, 13, 21)
      val b = swap(a)
      
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      def reorder(a: Array[Int]): Array[Int] = {
        val b1 = scala.collection.mutable.ArrayBuffer[Int]()
        val b2 = scala.collection.mutable.ArrayBuffer[Int]()
        
        //sort into arraybuffer b1 and b2
        for (i <- 0 until a.length){
          if(a(i) > 0){
            b1 += a(i)
          }
          else
          {
            b2 += a(i)
          }
        }
        
        //concatinate
        b1 ++= b2
        
        b1.toArray
      }
      
      val a = Array(1, -1, 2, -3, 5, -8, 13, -21)
      println(reorder(a).mkString("\n"))

    }
  }



  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      def noDublicates(a: Array[Int]): Array[Int] = {
        a.distinct
      }
      
      val a = Array(1, 1, 2, 2, 3, 3, 4, 4)
      println(noDublicates(a).mkString("\n"))

    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
      val a = Array(1, 1, 2, 3, -1, 8, -8, 21)
      val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
      indexes.drop(1)
	  for (j <- (1 until indexes.length).reverse) a.remove(indexes(j))

    }
  }



}
