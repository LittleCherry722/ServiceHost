object Solution extends App {
  Tasks.execute()
}

abstract class Task(val name: String) {
  Tasks.add(this)

  def solution()

  def execute() {
    println(name + ":")
    solution()
    println("\n")
  }
}

class Tasks {
  private var tasks = Seq[Task]()
  def add(t: Task) = tasks :+= t
  def execute() = tasks foreach { _.execute() }
  def execute(name: String) =
    (tasks filter { _.name == name }).head.execute()
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {
      import scala.collection.mutable
      def indexes(s: String) = {
        val m = mutable.Map[Char,mutable.Set[Int]]()
        for (i <- s.indices) {
          val c = s(i)
	      if (m.contains(c)) m(c) += i
	      else m(c) = mutable.TreeSet(i)
        }
        m
      }

      println(indexes("Mississippi"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      def indexes(s: String) = {
        val il = (c: Char) => s.zipWithIndex filter { _._1==c } map { _._2 } toList;
        s.distinct map { c => (c, il(c)) } toMap
      }

      println(indexes("Mississippi"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      import scala.collection.mutable.LinkedList
      def stripZeroes(l: Traversable[Int]) = l filter { _ != 0 }

      println(stripZeroes(LinkedList(5,4,0,2,4,0,10,0,6)))
    }
  }

  new Task("Task 4") {
    def solution() = {
      def indexedMap1(a: Array[String], m: Map[String,Int]) =
        (a.toSet & m.keySet) map { m(_) } toArray

      def indexedMap2(a: Array[String], m: Map[String,Int]) =
        a flatMap { m get _ }

      val a = Array("Tom", "Fred", "Harry")
      val m = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(indexedMap2(a,m).mkString("Array(", ",", ")"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      def foldRight(lst: List[Int])    = (lst :\ List[Int]()) (_ :: _)
      def foldRightRev(lst: List[Int]) = (lst :\ List[Int]()) ((e,l) => l :+ e)
      def foldleft(lst: List[Int])     = (List[Int]() /: lst) (_ :+ _)
      def foldleftRev(lst: List[Int])  = (List[Int]() /: lst) ((l,e) => e :: l)

      val l = List(1, 2, 3)
      println(foldRightRev(l))
      println(foldleftRev(l))
    }
  }

  new Task("Task 7") {
    def solution() = {
      import Function.tupled
      def Mul(ld: List[Double], li: List[Int]) = (ld zip li) map tupled { _ * _ }

      val prices = List(5.0, 20.0, 9.95)
      val quantities = List(10, 2, 1)
      println(Mul(prices, quantities))
    }
  }

}
