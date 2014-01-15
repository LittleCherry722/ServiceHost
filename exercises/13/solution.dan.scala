import scala.collection.mutable._

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
      val str = "Mississippi"
      def createMapIndex(str: String): Map[Char, LinkedHashSet[Int]] = {
        var result = Map[Char, LinkedHashSet[Int]]()
        for ((c,s) <- str.zipWithIndex) {
          val set = result.getOrElse(c, new scala.collection.mutable.LinkedHashSet[Int]())
          set += s
          result(c) = set
        }
        result
      }
      val result = createMapIndex(str)
      println(result)
    }
  }

  new Task("Task 2") {
    def solution() = {
      val str = "Mississippi"
      def createMapIndex(str: String) = {
        str.zipWithIndex.groupBy(_._1).map(x => (x._1, x._2.map(_._2).toList))
      }
      val result = createMapIndex(str)
      println(result)
    }
  }

  new Task("Task 3") {
    def solution() = {
      def filterZeros(list: scala.collection.mutable.LinkedList[Int]) = {
        var cur = list
        while (cur != Nil && cur.next != Nil) {
          while (cur.next != Nil && cur.next.elem == 0) {
            cur.next = cur.next.next
          }
          cur = cur.next
        }
        cur = list
        //remove leading zeros
        while (cur != Nil && cur.elem == 0) {
          cur = cur.next
        }
        cur
      }
      val lst = scala.collection.mutable.LinkedList(0, 0, 0, 1, 0, -8, 0, 0, 3, 4, 0, 9, 0)
      val filtered = filterZeros(lst)
      println(filtered)
    }
  }

  new Task("Task 4") {
    def solution() = {
      def getValues(array: Array[String], map: Map[String, Int]) = {
        array.flatMap(e => map.get(e))
      }
      val res = getValues(Array("Tom", "Fred", "Harry"), Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))
      assert(res sameElements Array(3, 5))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(1, 6, 5)
      val res1 = (lst :\ List[Int]())((b,a) => a :+ b)//reverse Fold right
      val res2 = (List[Int]() /: lst)((b,a) => a :: b)//reverse Fold left
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = Array(2, 4, 5)
      val quantities = Array(10, 6, 8)
      val res = (prices zip quantities) map Function.tupled { _ * _ }
      assert(res sameElements Array(20, 24, 40))
    }
  }

}
