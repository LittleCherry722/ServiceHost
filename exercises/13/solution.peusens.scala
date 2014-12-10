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

      def indexes(input: String) = {
        var map = new scala.collection.mutable.HashMap[Char, scala.collection.mutable.ArrayBuffer[Int]]

        for (i <- 0 until input.length) {
          val element_collection = input(i)
          if (!map.contains(element_collection)) {
            map(element_collection) = new scala.collection.mutable.ArrayBuffer[Int]
          }
          map(element_collection) += i
        }
        map
      }
    
      println(indexes("Mississippi"))
      println("Using a ArrayBuffer as a Set, it is guarenteed that the set is orderd by the input sequence.")
    }
  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      def indexes(input: String) = {
        var map = new scala.collection.immutable.HashMap[Char, scala.collection.mutable.ArrayBuffer[Int]]

        for (i <- 0 until input.length) {
          val element_collection = input(i)
          if (!map.contains(element_collection)) {
            map = map + (element_collection -> new collection.mutable.ArrayBuffer[Int]())
          }
          map(element_collection) += i
        }
        map
      }
    
      println(indexes("Mississippi"))
      println("Using a ArrayBuffer as a Set, it is guarenteed that the set is orderd by the input sequence.")
       
    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      def noZeros(list: scala.collection.mutable.LinkedList[Int]) = {
        list.filter(_ != 0)
      }
      
      var list = scala.collection.mutable.LinkedList(-2, -1, 0, 1, 2, 1, 0, -1, -2)
      println(noZeros(list))

    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      def func(strings: scala.collection.mutable.LinkedList[String], maps: Map[String,Int]) = {
       val result = new collection.mutable.ArrayBuffer[Int]()

        for (i <- strings){
          if(maps.contains(i)){
            result += maps(i)
          }
        }
        
        result
      }
      val strings = scala.collection.mutable.LinkedList("Tom", "Fred", "Harry")
      val maps = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
      println(func(strings, maps))

    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here

    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      println(" the operator :+ creates a collection fo the same type and appends the element")
      println(" the operator :: creates a copy of the collection with the same elemtns")
      println(" the operator :+ used as shown in the example will copy each element from list to the new collection, it is the same as ::")
      println("to reverse the list, do: (lst :\ List[Int]())((a1: Int, a2: List[Int]) => a2 :+ a1), does not work with :: operator!")

    }
  }

  new Task("Task 7") {
    def solution() = {

      // your solution for task 7 here
      var prices = List(5.0, 20.0, 9.95)
      var quantities = List(10, 2, 1)
      
      var zipped = prices zip quantities map { ((p: Double, q: Int) => p * q).tupled }
      
      println(zipped)

    }
  }

}
