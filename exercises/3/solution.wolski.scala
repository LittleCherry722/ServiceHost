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
    
    def adjacent[A](list: scala.collection.mutable.IndexedSeq[A]): scala.collection.mutable.IndexedSeq[A] = {
      for(index <- 1 until (list.length, 2)){
        val tmp: A = list(index-1)
        list(index-1) = list(index)
        list(index) = tmp
      }
      
      list
    }
    
    def solution() = {
      import scala.collection.mutable.ArrayBuffer
      
      val list1 = Array(1, 2, 3, 4, 5)
      val list2 = ArrayBuffer(1, 2, 3, 4, 5)
      
      println("adjacent " + list1.mkString(", ") + ": " + adjacent(list1).mkString(", "));
      println("adjacent " + list1.mkString(", ") + ": " + adjacent(list1).mkString(", "));
      
      
      println("adjacent " + list2.mkString(", ") + ": " + adjacent(list2).mkString(", "));

    }
  }

  new Task("Task 3") {
    
    def adjacent[A](list: scala.collection.mutable.IndexedSeq[A]): scala.collection.immutable.IndexedSeq[A] = {
      def newIndex(i: Int, length: Int): Int = { if (i % 2 == 0) ((i + 1) min (length-1)) else i - 1 }
      
      for (i <- 0 until list.length) yield list(newIndex(i, list.length))
    }
    
    def solution() = {
      import scala.collection.mutable.ArrayBuffer
      
      val list1 = Array(1, 2, 3, 4, 5)
      val list2 = ArrayBuffer(1, 2, 3, 4, 5)
      
      println("adjacent " + list1.mkString(", ") + ": " + adjacent(list1).mkString(", "));
      println("adjacent " + list1.mkString(", ") + ": " + adjacent(list1).mkString(", "));
      
      
      println("adjacent " + list2.mkString(", ") + ": " + adjacent(list2).mkString(", "));
    }
  }

  new Task("Task 4") {
    
    def posNeg(list: scala.collection.mutable.IndexedSeq[Int]): scala.collection.mutable.IndexedSeq[Int] = {
      import scala.collection.mutable.IndexedSeq;
      list.filter(_ > 0) ++ list.filter(_ < 0)
    }
    
    def solution() = {
      import scala.collection.mutable.ArrayBuffer

      val list1 = Array(1, -2, 3, -4, 5)
      val list2 = ArrayBuffer(-5, 4, -3, 2, -1)

      println("posNeg " + list1.mkString(", ") + ": " + posNeg(list1))
      println("posNeg " + list2.mkString(", ") + ": " + posNeg(list2))
      
    }
  }

  new Task("Task 7") {
    def solution() = {
      import scala.collection.mutable.ArrayBuffer

      val list1 = Array(1, 2, 3, 5, 7, 1, 2, 6, 7)
      val list2 = ArrayBuffer(1, 2, 3, 12, 3)

      println("distinct " + list1.mkString(", ") + ": " + list1.distinct.mkString(", "))
      println("distinct " + list2.mkString(", ") + ": " + list2.distinct.mkString(", "))

    }
  }

  new Task("Task 8") {
    def remNeg(list: scala.collection.mutable.IndexedSeq[Int]): scala.collection.mutable.IndexedSeq[Int] = {
      val (left, right) = list.splitAt(list.indexWhere(_ < 0)+1)
      left ++ right.filter(_ > 0)
    }
    
    def solution() = {
      import scala.collection.mutable.ArrayBuffer

      val list1 = Array(1, -2, 3, -4, 5)
      val list2 = ArrayBuffer(-5, 4, -3, 2, -1)
      val list3 = Array(1)
      val list4 = Array(-1)
      val list5 = Array(-1, -2)
      val list6 = Array(1, 2)

      println("remNeg " + list1.mkString(", ") + ": " + remNeg(list1))
      println("remNeg " + list2.mkString(", ") + ": " + remNeg(list2))
      println("remNeg " + list3.mkString(", ") + ": " + remNeg(list3))
      println("remNeg " + list4.mkString(", ") + ": " + remNeg(list4))
      println("remNeg " + list5.mkString(", ") + ": " + remNeg(list5))
      println("remNeg " + list6.mkString(", ") + ": " + remNeg(list6))

    }
  }

}
