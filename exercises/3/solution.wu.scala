

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
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val a = Array(1, 2, 3, 4, 5)
      ch3_2(a)
      println(a.mkString("<", ",", ">"))

    }

    def ch3_2(x: Array[Int]) {
      var temp = 0
      for (i <- 0 until x.length)
        if (i % 2 == 1) {
          temp = x(i - 1)
          x(i - 1) = x(i)
          x(i) = temp
        }

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      var a = Array(1, 2, 3, 4, 5)
      println(ch3_3(a).mkString("<", ",", ">"))

    }

    def ch3_3(x: Array[Int]): Array[Int] = {
      val a = (for (i <- 0 until x.length if i % 2 == 0) yield x(i)).toBuffer
      val b = for (i <- 0 until x.length if i % 2 == 1) yield x(i)
      for (i <- (0 until b.length).reverse) a.insert(i, b(i))
      return a.toArray
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      var a = Array(1, -3, 2, 4, -1, 5, 0)
      println(ch3_4(a).mkString("<", ",", ">"))

    }

    def ch3_4(x: Array[Int]): Array[Int] = {
      val a = for (elem <- x if elem > 0) yield elem
      val b = for (elem <- x if elem <= 0) yield elem
      return a ++ b
    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      println(ch3_7(Array(1, 1, 2, 2, 2, 6, 8, 9, 9, 9, 10)).mkString("<", ",", ">"))

    }

    def ch3_7(x: Array[Any]) = {
      (x.toSet).toArray
    }
  }

  new Task("Task 8") {
    def solution() = {

      // your solution for task 8 here
      println(ch3_8(Array(1, 1, 2, -1, 3, -3, 5, -9, 23)).mkString("<", ",", ">"))

    }
    
    def ch3_8(x: Array[Int]) = {
    // index of the first negative number
    var i = 0
    while (x(i) >= 0 && i < x.length) {
      i += 1
    }
    // drop
    if (x(i) < 0) {
      val a = x.drop(i + 1)
      val b = for (elem <- a if elem >= 0) yield elem
      x.dropRight(x.length - i - 1) ++ b
    } else x

  }
  }

}