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
  def values(fun: (Int) => Int, low: Int, high: Int) = {
    for(i <- low to high) yield (i, fun(i))
  }
          }
  }
  
  new Task("Task 2"){
          def solution() = {
  (1 to 9).reduceLeft(_.max(_))
          }
  }
  
  new Task("Task 3"){
          def solution() = {
  def fac(n: Int): Int = {
    var x = 0
    if(n > 0)
      x = (1 to n).reduceLeft(_*_);
    x
  }
          }
  }
  
  new Task("Task 4"){
          def solution() = {
  def fac(n: Int): Int = {
    (1 to n).foldLeft(1)(_*_)
  }
          }
  }
  
  new Task("Task 5"){
          def solution() = {
  def largest(fun: (Int) => Int, input: Seq[Int]): Int = {
    var temp = input.map(fun)
    val result = temp.reduceLeft(_.max(_))
    result
  }
          }
  }
  
  new Task("Task 6"){
          def solution() = {
  def largest(fun: (Int) => Int, input: Seq[Int]): Int = {
    var temp = input.map(fun)
    val result = temp.reduceLeft(_.max(_))
    input(temp.indexOf(result))
  }
          }
  } 
   
  new Task("Task 7"){
          def solution() = {
  def adjustToPair(fun: (Int, Int) => Int) = (a: Int, b: Int) => {
    ((x: Int, y: Int) => fun(x, y))(a, b)
  }
          }
  }
    
  new Task("Task 8"){
          def solution() = {
  def arrayHasLength(a: Array[String], b: Array[Int]): Boolean = {
    val result = a.corresponds(b)(_.length == _)
    result
  }
          }
  }
    
  new Task("Task 9"){
          def solution() = {
  def corresponds(arrStr: Array[String], arrInt: Array[Int], func: (String, Int) => Boolean) = {
   var result = true
    if (arrStr.length == arrInt.length) {
      for (i <- 0 until arrStr.length) {
        if (!func(arrStr(i), arrInt(i))) {
          result = false
        }
      }
    } else result = false
    result
  }
// Der andere Aufruf ist nicht möglich, da die Methoda 3 Parameter erwartet
          }
  }
    
  new Task("Task 10"){
          def solution() = {
  def unless(condition: => Boolean)(block: => Unit) {
    if(!condition)
      block
  }
          }
  }
  
}