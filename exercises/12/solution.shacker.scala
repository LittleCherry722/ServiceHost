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
     
      def values(fun: (Int) => Int, low: Int, high: Int) = {
         if(low < high){
           for (i <- low to high) yield (i, fun(i))
         }
      }
    }
  }

  new Task("Task 2") {
    def solution() = {
      val a = Array(1, 12, 3, 4, 125, 6)
      a.reduceLeft(_.max(_))
      // your solution for task 2 here

    }
  }

  new Task("Task 3") {
    def solution() = {
      def fuc(f: Int) = if (f < 1) 1 else (1 to f).reduceLeft(_ * _)
      // your solution for task 3 here

    }
  }

  new Task("Task 4") {
    def solution() = {
      def fuc(f: Int) = (1 to f).foldLeft(1)(_ * _)
      // your solution for task 4 here

    }
  }

  new Task("Task 5") {
    def solution() = {
      def largest(fun: (Int) => Int, inputs: Seq[Int]) = inputs.map(fun(_)).max
      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {
      def largestAt(fun: (Int) => Int, inputs: Seq[Int]) =
        inputs.map(fun(_)).indexOf(inputs.map(fun(_)).max) + 1
      // your solution for task 6 here
    }
  }

  new Task("Task 7") {
    def solution() = {
      def adjustToPair(fun: (Int, Int) => Int)(pair: (Int, Int)) = fun(pair._1, pair._2)
      val pairs = (1 to 10) zip (11 to 20)
      adjustToPair(_ * _)((6, 7))
      // your solution for task 7 here

    }
  }

  new Task("Task 8") {
    def solution() = {
      val strArray = Array("Hallo","beides")
      val len = Array(7,6)
      strArray.corresponds(len)(_.length == _)
      // your solution for task 8 here

    }
  }

  new Task("Task 9") {
    def solution() = {
     
    	def ownCorr(fun: (Any, Any) => Any)(a : Seq[Any], b: Seq[Any]) =
    			for(i <- 0 until a.length) fun(i, b(i))
      // your solution for task 9 here

    }
  }

  new Task("Task 10") {
    def solution() = {
      def unless(condition: => Boolean):Boolean = {
        if(!condition){
          false
        }
        else true
      }
      // your solution for task 10 here

    }
  }

}
