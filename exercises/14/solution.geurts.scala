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

            print("I am so smart, s-m-r-t")
            
          }
  }
  
  new Task("Task 2"){
          def solution() = {
  import scala.collection.mutable.ArrayBuffer
  def swap(x: Pair[Int, Int]): Pair[Int, Int] = {
    var result = x match {
      case (k, v) => (v, k)
      case _ => (0, 0)
    }
    result
  }
            
          }
  }
  
  new Task("Task 3"){
          def solution() = {
  import scala.collection.mutable.ArrayBuffer
  def swapArray(arr: ArrayBuffer[Any]): ArrayBuffer[Any] = {
    arr.length match {
      case 0 => arr
      case 1 => arr
      case _ => ArrayBuffer(arr(1), arr(0)) ++ arr.drop(2)
    }
  }
          }
  }
  
  new Task("Task 4"){
          def solution() = {
  import scala.collection.mutable.ArrayBuffer
  case class Item(x: Item) {
    def prize(): Int = {
      1
    }
  }
  case class Multiple(numberMultiple: Int, item: Item) extends Item(item) {
    var itemList = ArrayBuffer[Item]()
    for(i <- 0 to numberMultiple) {
      itemList(i) = x.copy()
    }
    override def prize(): Int = {
      var fisherprize: Int = 0
      for(i <- itemList)
        fisherprize += i.prize
      fisherprize
    }
  }
  }
  }
  
  new Task("Task 5"){
          def solution() = {
  def leafSum(lst: List[Any]): Int = {
    lst match {
      case head :: tail => head match {
        case x: Int => x + leafSum(tail)
        case x: List[Any] => leafSum(x) + leafSum(tail)
        case _ => 0
      }
      case head: List[Any] => leafSum(head)
      case _ => 0
    }
  }
          }
  }
  
  new Task("Task 6"){
          def solution() = {
  sealed abstract class BinaryTree
  case class Leaf(value: Int) extends BinaryTree {}
  case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree {}
  def computeElements(tree: BinaryTree): Int = {
    tree match {
      case node: Node => computeElements(node.left) + computeElements(node.right)
      case leaf: Leaf => leaf.value
      case _ => 0
    }
  }
          }
  } 
   
  new Task("Task 7"){
          def solution() = {
  sealed abstract class BinaryTree
  case class Leaf(value: Int) extends BinaryTree {}
  case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree {}
  def computeElements(tree: BinaryTree): Int = {
    tree match {
      case node: Node => computeElements(node.left) + computeElements(node.right)
      case leaf: Leaf => leaf.value
      case _ => 0
    }
  }
          }
  }
    
  new Task("Task 8"){
          def solution() = {

                        // your solution for task 8 here
            
          }
  }
    
  new Task("Task 9"){
          def solution() = {
  def computeSum(lst: List[Option[Int]]): Int = {
    var result = 0
    for(i <- lst) {
      result += i.getOrElse(0)
    }
    result
  }
          }
  }
    
  new Task("Task 10"){
          def solution() = {
  def composeFunctions(func1: Double => Option[Double], 
      func2: Double => Option[Double], value: Double): Option[Double] = {
    func1(func2(value).getOrElse(return None))
  }
          }
  }
  
}