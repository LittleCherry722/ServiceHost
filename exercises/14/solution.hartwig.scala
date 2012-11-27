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
  
  new Task("Task 1"){
	  def solution() = {
	    
	  		// unwichtig
	    
	  }
  }
  
  new Task("Task 2"){
	  def solution() = {

	    def swap(pair: (Int, Int)): (Int, Int) = pair match {
	      case (x, y) => (y, x)
	    }
      
      println(swap((1,2))) // (2,1)

	  }
  }
  
  new Task("Task 3"){
	  def solution() = {

	   implicit def array2string[T](x: Array[T]) = x.mkString(", ");
	    
	    def swap(arr: Array[Int]): Array[Int] = arr match {
	      case Array(x, y, tail @ _*) => Array(y, x) ++ tail
	      case _ => arr
	    }
	    
	    val arr = Array(1,2,3,4)
	    
	    println(swap(arr))
	    
	  }
  }
  
  new Task("Task 4"){
	  def solution() = {

	    abstract class Item
	    case class Article(description: String, price: Double) extends Item
	    case class Bundle(description: String, discount: Double, items: Item*) extends Item
	    case class Multiple(quantity: Int, item: Item) extends Item
	    
    	def price(it: Item): Double = it match {
	      case Article(_, p) => p
	      case Multiple(q, p) => q * price(p)
	      case Bundle(_, d, items @ _*) => items.map( (item: Item) => price(item) ).sum - d
	    }
	    
			val test =	Bundle("Father's day special", 20.0,
									Article("Scala for the Impatient", 39.95),
									Bundle("Anchor Distillery Sampler", 10.0,
										Article("Old Potrero Straight Rye Whiskey", 79.95),
										Article("Junípero Gin", 32.95)))
										
			println(price(test)); // ~122.85

	  }
  }
  
  new Task("Task 5"){
	  def solution() = {

  		def leafSum(treeList: List[Any]): Int = treeList match {
	      case (head: Int)       :: (tail: List[Any]) => head + leafSum(tail)
	      case (head: List[Any]) :: (tail: List[Any]) => leafSum(head) + leafSum(tail)
	      case _ => 0
	    }
    
  		println(leafSum(List(List(3,8), 2, List(5)))); // 18
  		
	  }
  }
  
  new Task("Task 6"){
	  def solution() = {

  		sealed abstract class BinaryTree
			case class Leaf(value: Int) extends BinaryTree
			case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
	    
			def leafSum(tree: BinaryTree): Int = tree match {
	      case Leaf(x) => x
	      case Node(left, right) => leafSum(left) + leafSum(right)
	      case _ => 0
	    }
			
  		println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2)))); // 13
  		
	  }
  } 
   
  new Task("Task 7"){
	  def solution() = {

  		sealed abstract class Tree
			case class Leaf(value: Int) extends Tree
			case class Node(children: Tree*) extends Tree
	    
			def leafSum(tree: Tree): Int = tree match {
	      case Leaf(x) => x
	      case Node(children @ _*) => children.map( (item) => leafSum(item) ).sum
	      case _ => 0
	    }
			
  		println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5))))); // 18
	    
	  }
  }
    
  new Task("Task 8"){
	  def solution() = {

  		sealed abstract class Tree
			case class Leaf(value: Int) extends Tree
			case class Node(operator: Char, children: Tree*) extends Tree
	    
			def eval(tree: Tree): Int = tree match {
	      case Leaf(x) => x
	      case Node('+', children @ _*) => children.map( (node) => eval(node) ).sum // warum klappt folgendes nicht? children.reduce( (n1, n2) => eval(n1) + eval(n2) )
	      case Node('-', children @ _*) => -children.map( (node) => eval(node) ).sum
	      case Node('*', children @ _*) => children.map( (item) => eval(item) ).product
	      case Node('/', children @ _*) => 1/children.map( (item) => eval(item) ).product
	      case _ => 0
	    }
			
  		println(eval(Node('+', Node('*', Leaf(3), Leaf(8)), Leaf(2), Node('-', Leaf(5))))); // 21
	    
	  }
  }
    
  new Task("Task 9"){
	  def solution() = {

	    def sumNotNone(l: List[Option[Int]]): Int = {
	      l.reduceLeft( (a: Option[Int], b: Option[Int]) => a.getOrElse[Int](0) + b.getOrElse[Int](0) ); // why isnt it working?!
	    }
	      
	    
	  }
  }
  
}
