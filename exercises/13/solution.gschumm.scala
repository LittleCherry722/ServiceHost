object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
}

abstract class Task(val name: String) {
  Tasks add this
  def solution();
  def execute() {
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    import scala.collection.JavaConversions._

    def indexes(str: String) = {
      val map = collection.mutable.Map[Char, collection.mutable.Set[Int]]()
      for (i <- str.indices) {
        if (!map.contains(str(i))) {
          map(str(i)) = new java.util.TreeSet[Int]()
        }
        map(str(i)) += i
      }
      map
    }

    def solution() = {
      println(indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def indexes(str: String) = {
      var map = collection.immutable.Map[Char, List[Int]]()
      for (i <- str.indices) {
        if (!map.contains(str(i)))
          map += (str(i) -> List[Int](i))
        else
          map += (str(i) -> (map(str(i)) :+ i))
      }
      map
    }
    def solution() = {

      println(indexes("Mississippi"))
    }
  }

  new Task("Task 3") {
    import scala.collection.mutable.LinkedList

    def removeZeros(lst: LinkedList[Int]) = {
      var cur = lst
      while (cur != Nil) {
        if (cur.elem == 0 && cur.next != Nil) {
          cur.elem = cur.next.elem
          cur.next = cur.next.next
        } else {
          cur = cur.next
        }
      }
    }
    def solution() = {
      val lst = LinkedList(0, 1, 5, 0, 5, 14)
      removeZeros(lst)
      println(lst)
    }
  }

  new Task("Task 4") {
    def mapStrInt(arr: Array[String], map: Map[String, Int]) = 
      arr.flatMap(map.get(_))

    def solution() = {
      val arr = Array("Tom", "Fred", "Harry")
      val map = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(mapStrInt(arr, map).mkString(", "))
    }
  }

  new Task("Task 5") {
    def mkString[A](seq: collection.Traversable[A], sep: String = "") = 
      seq.map(_.toString).reduceLeft(_ + sep + _)

    def solution() = {
      println(mkString(Array(1, 2, 3, 4, 5), ", "))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(1, 2, 3, 4)
      println((List[Int]() /: lst)((x, y) => y :: x))
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here

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
