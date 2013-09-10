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
      def countChar(s: String) = {
        val inputChars = s.toCharArray()
        var map = new scala.collection.mutable.HashMap[Char, scala.collection.mutable.TreeSet[Int]]()
        for (i <- (0 until inputChars.length)) {
          var s = map.getOrElse(inputChars(i), new scala.collection.mutable.TreeSet[Int]());
          s.add(i);
          map(inputChars(i)) = s;
        }
        map
      }

      val map = countChar("Mississippi")
      println(map.mkString("\n"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def countChar(s: String) = {
        val inputChars = s.toCharArray()
        var map = new scala.collection.immutable.HashMap[Char, scala.collection.mutable.TreeSet[Int]]()
        for (i <- (0 until inputChars.length)) {
          var s = map.getOrElse(inputChars(i), new scala.collection.mutable.TreeSet[Int]());
          s.add(i);
          map = map + (inputChars(i) -> s)
        }
        map
      }

      val map = countChar("Mississippi")
      println(map.mkString("\n"))

    }
  }

  new Task("Task 3") {
    def solution() = {
      def removeZeros(l: scala.collection.mutable.LinkedList[Int]) = l.filterNot(_ == 0)

      val l = scala.collection.mutable.LinkedList[Int](0, 1, 4, 5, 0, 3, 4)
      println(removeZeros(l).mkString(", "))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def f(set: Array[String], map: Map[String, Int]) = map.keySet.flatMap(s => if (set.contains(s)) List(map(s)) else Nil)

      val s = Array("Tom", "Fred", "Harry")
      val m = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(f(s, m).mkString(", "))

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      /*
       * :\ = foldRight
       * /: = foldLeft
       */

      val lst = List(1, 2, 3, 4, 5)
      val temp = (lst :\ List[Int]())((x, y) => y :+ x)
      val temp1 = (List[Int]() /: lst)((x, y) => y :: x)

      println(temp.mkString(", "))
      println(temp1.mkString(", "))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List[Double](5.0, 20.0, 9.95)
      val quantities = List[Double](10, 2, 1)

      val totalPrice = (prices zip quantities) map (((x: Double, y: Double) => x * y).tupled) sum
        println(totalPrice);
    }
  }
}
