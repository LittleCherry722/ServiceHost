import java.util.LinkedList
import scala.collection.mutable.LinkedList
import com.sun.org.apache.xalan.internal.xsltc.compiler.ForEach
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
    def solution() = {

      val indices = scala.collection.mutable.Map[Char, scala.collection.mutable.Set[Int]]()

      var word = "Mississippi"
      for (i <- 0 until word.length()) {
        indices(word(i)) = indices.getOrElse(word(i), scala.collection.mutable.Set()) += i
      }

      println(indices)

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
    def solution() = {

      val l = scala.collection.mutable.LinkedList(0, 1, 0, 2, 0, 3, 4)

      println(l.filter(_ != 0))

    }
  }

  new Task("Task 4") {
    def solution() = {

      def foo(col: Collection[String], m: Map[String, Int]) = {
        col.flatMap(m.get(_))
      }
      println(foo(Seq("a", "b", "c"), Map("a" -> 1, "c" -> 3, "d" -> 4)))

    }
  }

  new Task("Task 5") {
    def solution() = {

      class myArray(var s: Array[Any]) {
        def myMkString(prefix: String = "", infix: String, postfix: String = "") = {
          var x: String = prefix + s.reduceLeft(_ + infix + _) + postfix
          x
        }
      }
      object myArray {
        def apply(s: Array[Any]) = { new myArray(s) }
      }

      var a = myArray(Array(1, 2, 3))
      println(a.myMkString(" < ", " ; ", " > "))
    }
  }

  new Task("Task 6") {
    def solution() = {

      var lst = List(1, 2, 3, 4, 5)
      println((lst :\ List[Int]())(_ :: _)) // same list
      println((List[Int]() /: lst)(_ :+ _)) // same list

      (lst :\ List[Int]())(_ :: _).reverse // modified to get reverse list
      (List[Int]() /: lst)(_ :+ _).reverse // modified to get reverse list

    }
  }

  new Task("Task 7") {
    def solution() = {

    }
  }

  new Task("Task 8") {
    def solution() = {

    }
  }

  new Task("Task 9") {
    def solution() = {

    }
  }

  new Task("Task 10") {
    def solution() = {

    }
  }

}
