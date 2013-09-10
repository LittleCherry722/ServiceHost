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

  def execute() = { tasks.foreach((t: Task) => {t.execute()}) }

  def execute(name: String) = { tasks.filter(_.name == name).head.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {

    import scala.collection.mutable

    def solution() = {
      val x = indexes("Mississippi")
      println(x.mkString(", "))
    }

    def indexes(value: String): mutable.Map[Char, mutable.Set[Int]] = {
      val map = mutable.Map[Char, mutable.Set[Int]]()

      for ((character, index) <- value.zipWithIndex) {
        if (map.contains(character))
          map(character) += index
        else
          map(character) = mutable.LinkedHashSet(index)
      }

      map
    }
  }

  new Task("Task 2") {

    type IndexMap = Map[Char, List[Int]]

    def solution() = {
      val x = indexes("Mississippi")
      println(x.mkString(", "))
    }

    def indexes(value: String): IndexMap = {
      val indexFolder = {
        (map: IndexMap, charIndex: (Char, Int)) =>
          val char = charIndex._1
          val index = charIndex._2
          val list = map.getOrElse(char, Nil)
          map + (char -> (index :: list))
      }
      val initial = Map[Char, List[Int]]()

      value.zipWithIndex.foldLeft(initial)(indexFolder)
    }
  }

  new Task("Task 3") {

    import scala.collection.mutable

    def solution() = {
      val list = mutable.LinkedList(0, 1, 2, 3, 6, 0, 65, 0, 3, 0, 12, 344, 0)
      removeZeros(list)
      println(list.mkString(", "))

      val onlyZeros = mutable.LinkedList(0, 0, 0, 0, 0)
      removeZeros(onlyZeros)
      println(onlyZeros)
    }

    def removeZeros(lst: mutable.LinkedList[Int]) {
      var cur = lst
      while (cur != Nil) {
        if (cur.elem == 0) {
          if (cur.next != Nil) {
            cur.elem = cur.next.elem
            cur.next = cur.next.next
          } else {
            cur.next = cur
          }
        } else {
          cur = cur.next
        }
      }
    }
  }

  new Task("Task 4") {
    def solution() = {
      val lst = task4(Array("Tom", "Fred", "Harry"), Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))
      println(lst.mkString(", "))
    }

    def task4(names: Iterable[String], weights: Map[String, Int]) = names.flatMap(weights.get(_))
  }

  new Task("Task 5") {
    def solution() = {
      val lst = List(1, 2, List(3, 4, 5), List(6, 7, 8), 9)
      println(lst.mkString("<", ", ", ">"))
      println(myMkString(lst, "<", ", ", ">"))
    }

    def myMkString(values: Iterable[Any], start: String = "", delim: String = "", end: String = ""): String = {
      start + values.reduceLeft(_ + delim + _.toString) + end
    }
  }

  new Task("Task 6") {
    def solution() = {
      // (lst :\ List[Int]())(_ :: _)   -> lst
      // (List[Int] /: lst)(_ :+ _)     -> lst

      val lst = List(1, 2, 3, 4, 5, 6)
      println((lst :\ List[Int]())((x, l) => l :+ x))
      println((List[Int]() /: lst)((l, x) => x :: l))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      val lst = (prices zip quantities) map {(p: Double, q: Int) => p * q}.tupled
      println(lst)
    }
  }
}
