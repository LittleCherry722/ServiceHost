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



object Tasks extends Tasks {

  new Task("Task 1") {
      def solution() = {
      import scala.collection.mutable
    	def indexes(str: String) = {
    	  var m: mutable.HashMap[Char, Set[Int]] = new mutable.HashMap[Char, Set[Int]]
    	  for (i <- 0 until str.length) {
    	    if (m.contains(str(i))){
    	      m(str(i)) += (m.get(str(i))) + i
    	    } else {
    	      m + ((str(i)) -> Set(i))
    	    }
    	  }
    	  m
    	}
    }
  }

  new Task("Task 2") {
    def solution() = {
    	def indexes(str: String) = {
    	  str.map(c => (c, (for (i <- 0 until str.size) yield {
    	    if (c == str(i)) { i }
    	  }).toSet ))
    	}
    	
    	println(indexes("Mississippi"))
    }
  }

  new Task("Task 3") {
    def solution() = {
    import scala.collection.mutable.LinkedList
    
    def removeZero(intList: LinkedList[Int]) = {
      intList.filter(_ != 0)
    }
      
    println(removeZero(LinkedList(1, 0, 3, 0, 5, 0, 7)))
    }
  }

  new Task("Task 4") {
    def solution() = {
    import scala.collection.mutable.LinkedList
    
    def convStrToInt(names: Array[String], namesToInts: Map[String, Int]) = {
    	names.map { s => namesToInts.get(s) } flatMap { i => i }
    }

    val array = convStrToInt(Array("Tom", "Fred", "Harry"), Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))

    println(array.mkString(" "))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val lst = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      //println((lst :\ List[Int]())(_ :: _))
      //Builds the collection folding from right and appending with ::
      
      //println((List[Int]() /: lst)(_ :+ _))
      //same like the first one but folding from left appending with :+ 
      
      println((List[Int]() /: lst) { (x, y) => y :: x })
    }
  }

  new Task("Task 7") {
    def solution() = {
    	val prices = List(5.0, 20.0, 9.95)
    	val quantities = List(10, 2, 1)

    	println((prices zip quantities) map { p => p._1 * p._2 })
  
    	val tupler = (x: Double, y: Int) => x * y
  
    	println((prices zip quantities) map { tupler.tupled })
    }
  }

}
