object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //  Tasks.execute("Task 4");
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
    def solution() = {
      def indexes(input: String):
          scala.collection.Map[Char, scala.collection.Set[Int]] = {
        import scala.collection.SortedSet
        val m = scala.collection.mutable.Map[Char, SortedSet[Int]]()
          .withDefaultValue(SortedSet[Int]())

        for ((i, c) <- (1 to input.length()).zip(input)) {
          m(c) += i
        }
        m
      }

      println(indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def indexes(input: String): scala.collection.Map[Char, List[Int]] = {
        var m = Map[Char, List[Int]]().withDefaultValue(List[Int]())

        for ((i, c) <- (1 to input.length()).zip(input)) {
          m = m + (c -> (m(c) :+ i))
        }

        m
      }

      println(indexes("Mississippi"))
    }

  }

  new Task("Task 3") {
    def solution() = {
      import scala.collection.mutable.LinkedList
      // Problem wenn die Liste nur aus einer 0 besteht, kann man sie nicht in
      // Nil verändern
      def removeZero(list: LinkedList[Int]) {
        var iter = list
        if (list.head == 0) {
          if (list.length >= 2) {
            list.update(0, list.next.head)
            list.next = list.next.next
          }
        }

        while (iter != Nil) {
          if (iter.next.head == 0) {
            iter.next = iter.next.next
          }

          iter = iter.next
        }
      }

      println("a)")
      var list = LinkedList(4, 3, 0, 1, 8, 99, 0, 1, 0, -5, 0)
      println(list)
      println("remove zeros:")
      removeZero(list)
      println(list)

      println("b)")
      list = LinkedList(0, 3, 0, 1, 8, 99, 0, 1, 0, -5, 0)
      println(list)
      println("remove zeros:")
      removeZero(list)
      println(list)

    }
  }

  new Task("Task 4") {
    def solution() = {
      def combine(col: Collection[String], map: Map[String, Int]) =
        col.flatMap(map.get(_))

      val a = Array("Tom", "Fred", "Harry")
      val m = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)

      println(combine(a, m).mkString("[", ", ", "]"))
    }
  }

  new Task("Task 5") {
    def solution() = {

      def mymkString[A](iterable: Iterable[A])(div: String = ""): String = {
        iterable.reduceLeft(
          (acc: Any, x: A) => acc.toString() + div + x).toString()
      }

      val iterable = List(1, 5, 3, 2)
      println("my: %s".format(mymkString(iterable)(", ")))
      println("mk: %s".format(iterable.mkString(", ")))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(5, 3, 1, 2, 8, 4, -2, -1)

      // Reversed
      val rleft = (lst :\ List[Int]())((i: Int, list: List[Int]) => list :+ i)
      val rright = (List[Int]() /: lst)((list: List[Int], i: Int) => i :: list)

      println(rleft)
      println(rright)
    }
  }

  new Task("Task 7") {
    def solution() = {

      val prices = List(5., 20., 0.95)
      val quantities = List(10, 2, 1)

      def tupled[A, B, C](f: (A, B) => C): ((A, B)) => C =
        (p: (A, B)) => f(p._1, p._2)

      val res = (prices zip quantities) map (tupled(_ * _))

      println("prices: %s".format(prices))
      println("quantities: %s".format(quantities))
      println("(prices zip quantities) map (tupled(_ * _)) = " + res)
    }
  }

  new Task("Task 8") {
    def solution() = {
      def transform(array: Array[Double], column: Int): Array[Array[Double]] =
        array.grouped(column).toArray

      val a: Array[Double] = Array(1, 2, 3, 4, 5, 6)

      println(a.mkString(", ") + " size: 3")
      for (i <- transform(a, 3)) {
        print("(%s)".format(i.mkString(", ")))
      }
    }
  }

  new Task("Task 9") {
    def solution() = {

      // TODO your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      val str = "Das ist eine scala Aufgabe"
      val frequencies = new scala.collection.mutable.HashMap[Char, Int]
      // Wäre eine schlechte idee, weil es ein mutable set ist und man somit 
      // Gefahr läuft den gleichen Buchstaben zur gleichen Zeit mehrmals 
      // in die map zu schreiben
      println("Wäre eine schlechte idee, weil es ein mutable set ist und man" +
        " somit")
      println("Gefahr läuft den gleichen Buchstaben zur gleichen Zeit mehrmals")
      println("in die map zu schreiben")
      for (c <- str) frequencies(c) = frequencies.getOrElse(c, 0) + 1

      import scala.collection.immutable.HashMap
      val frequen = str.par.aggregate(HashMap[Char, Int]())(
        (freq: HashMap[Char, Int], c: Char) =>
          freq + (c -> (freq.getOrElse(c, 0) + 1)),
        _ ++ _)

      println("Solution from book: (not parallel)")
      println(frequencies)
      println("My solution:")
      println(frequen)
    }
  }

}