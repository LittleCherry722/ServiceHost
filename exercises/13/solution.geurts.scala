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
    println(name+":");
    solution();
    println("\n");
  }
}

class Tasks {
        private var tasks = Seq[Task]();
        def add (t: Task) = { tasks :+= t }
        def execute () = { tasks.foreach( (t:Task) => { t.execute(); } )  }
        def execute (name: String) = { tasks.filter(_.name == name).first.execute() }
}

/* insert your solutions below */

object Tasks extends Tasks{
  import scala.collection.mutable.Map
  import scala.collection.mutable.Set
  
  new Task("Task 1"){
          def solution() = {
  import scala.collection.mutable.Map
  import scala.collection.mutable.Set
  def mapWithIndexes(str: String) = {
    var result = Map[Char, Set[Int]]()
    for(i <- 0 to str.length) {
      result(str(i)) = result.getOrElse(str(i), Set[Int]()) += i
    }
    result
  }
          }
  }
  
  new Task("Task 2"){
          def solution() = {
  import scala.collection.immutable.Map
  def mapWithIndexes(str: String) = {
    var result = Map[Char, List[Int]]()
    for(i <- 0 to str.length) {
      result = Map((str(i), result.getOrElse(str(i), List[Int]())))
    }
    result
  }
          }
  }
  
  new Task("Task 3"){
          def solution() = {
  import scala.collection.mutable.LinkedList
  def removeZeros(x: LinkedList[Int]): LinkedList[Int] = {
    if(x == Nil) LinkedList.empty
    else 
      if(x.elem == 0) removeZeros(x.next)
      else new LinkedList(x.elem, removeZeros(x.next))
  }
          }
  }
  
  new Task("Task 4"){
          def solution() = {
  def getValuesFromMap(coll: Array[String], mapper: Map[String, Int]): Array[Int] = {
  var result = Array[Int]()
  var index = 0;
    for(i <- coll) {
      for((k, v) <- mapper) {
        val temp = extractValue(i, (k, v))
        if(temp == Int.MaxValue) {
          result(index) = temp
          index += 1
        }
      }
    }
  result
  }

  def extractValue(s: String, vec: Pair[String, Int]) = {
    if(vec._1 == s)
      vec._2
      else Int.MaxValue
  }
          }
  }
  
  new Task("Task 5"){
          def solution() = {
  def mkString2(seq: Seq[Any], infix: String) = {
    seq.map(_.toString).reduceLeft(_ + infix + _)
  }
          }
  }
  
  new Task("Task 6"){
          def solution() = {
        	  print("List[Int].foldRight(lst)(_ :: _))")
        	  print("lst.foldLeft(List[Int])(_ :+ _))")
                        // your solution for task 6 here
            
          }
  } 
   
  new Task("Task 7"){
          def solution() = {
            def tuplerator(func: (Int, Int) => Int) = {
              func.tupled
            }
                        // your solution for task 7 here
            
          }
  }
    
  new Task("Task 8"){
          def solution() = {

                        // your solution for task 8 here
            
          }
  }
    
  new Task("Task 9"){
          def solution() = {

                        // your solution for task 9 here
            
          }
  }
    
  new Task("Task 10"){
          def solution() = {

                        // your solution for task 10 here
            
          }
  }
  
}