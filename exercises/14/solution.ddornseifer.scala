// Solutions 14
// David Dornseifer
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
        def execute (name: String) = { tasks.filter(_.name == name).head.execute() }
}
//template modified - first => head

/* insert your solutions below */

object Tasks extends Tasks{
  
  /**
   * 1. Your Java Development Kit distribution has the source code for much of the JDK
   * in the src.zip file. Unzip and search for case labels (regular expression case
   * [^:]+:). Then look for comments starting with // and containing alls? thr to
   * catch comments such as // Falls through or // just fall thru. Assuming the
   * JDK programmers follow the Java code convention, which requires such a
   * comment, what percentage of cases falls through?
   */
  
  new Task("Task 1"){
          def solution() = {
            
                  
            
          }
  }
  
  /**
   * 2. Using pattern matching, write a function swap that receives a pair of integers and
   * returns the pair with the components swapped.
   */
  
  new Task("Task 2"){
          def solution() = {
            
            def swap(pair:(Int, Int)): (Int, Int) = {
              pair match{
                case (x, y) => (y, x)}
            }

            println(swap((15, -1)))      
            
          }
  }
  
  /**
   * 3. Using pattern matching, write a function swap that swaps the first two elements of
   * an array provided its length is at least two.
   */
  
  new Task("Task 3"){
          def solution() = {
            
            def swap(pair:(Int, Int)): (Int, Int) = {
              pair match{
                case (x,y) if (x > 9 && y > 9) => (y, x)
                case _ => pair
              }
            }

            println(swap((1, 15)))
            println(swap((11, 15)))
            
          }
  }
  
  /**
   * 4. Add a case class Multiple that is a subclass of the Item class. For example,
   * Multiple(10, Product("Blackwell Toaster", 29.95)) describes ten toasters. Of
   * course, you should be able to handle any items, such as bundles or multiples, in
   * the second argument. Extend the price function to handle this new case.
   */
  
  new Task("Task 4"){
          def solution() = {

            abstract class Item
            case class Article(desc: String, price: Double) extends Item
            case class Bundle(desc: String, discount: Double, items: Item*) extends Item
            case class Multiple(count: Int, item: Item) extends Item
	    
            def price(it: Item): Double = it match {
              case Article(_, p) => p
              case Multiple(q, p) => q * price(p)
              case Bundle(_, d, items @ _*) => items.map( (item: Item) => price(item) ).sum - d
            }
	    
			val multip =	Bundle("Father's day special", 20.0,
									Article("Scala for the Impatient", 39.95),
									Bundle("Anchor Distillery Sampler", 10.0,
									Article("Old Potrero Straight Rye Whiskey", 79.95),
									Article("Jun�pero Gin", 32.95)))
										
			println(price(multip));
            
            
          }
  }
  
  /**
   * 5. One can use lists to model trees that store values only in the leaves. For example,
   * the list ((3 8) 2 (5)) describes the tree
   *    •
   *   /|\
   *  • 2 •
   * / \  |
   *3   8 5
   * However, some of the list elements are numbers and others are lists. In
   * Scala, you cannot have heterogeneous lists, so you have to use a List[Any].
   * Write a leafSum function to compute the sum of all elements in the leaves,
   * using pattern matching to differentiate between numbers and lists.
   */
  
  new Task("Task 5"){
          def solution() = {
                        
            def leafSum(list: List[Any]): Int = {
              list match {
                case (head: Int) :: (tail: List[Any]) => head + leafSum(tail)
                case (head: List[Any]) :: (tail: List[Any]) => leafSum(head) + leafSum(tail)
                case _ => 0
              }
            }

            println(leafSum(List(List(3, 8), 2, List(5))))         
            
          }
  }
  
  /**
   * 6. A better way of modeling such trees is with case classes. Let’s start with binary trees.
   *  sealed abstract class BinaryTree
   *  case class Leaf(value: Int) extends BinaryTree
   *  case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
   * Write a function to compute the sum of all elements in the leaves.
   */
  
  new Task("Task 6"){
          def solution() = {

            abstract class BinaryTree
            case class Leaf(value: Int) extends BinaryTree
            case class Node(left: BinaryTree, right: BinaryTree) extends BinaryTree
            
            def leafSum(tree: BinaryTree): Int = {
              tree match {
                case Leaf(value) => value
                case Node(left, right) => leafSum(left) + leafSum(right)
                case _ => 0
              }  
            }
            
            println(leafSum(Node(Node(Leaf(5), Leaf(7)), Leaf(1))));
            
          }
  } 
   
  /**
   * 7. Extend the tree in the preceding exercise so that each node can have an arbitrary
   * number of children, and reimplement the leafSum function. The tree in exercise 5
   * should be expressible as 
   * Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))

   */
  
  new Task("Task 7"){
          def solution() = {

            abstract class extBinaryTree
            case class Leaf(value: Int) extends extBinaryTree
            case class Node(children: extBinaryTree*) extends extBinaryTree
            
            
            def leafSum(tree: extBinaryTree): Int = {
              tree match {
                case Leaf(value) => value
                case Node(children @ _*) => children.map(leafSum).sum
                case _ => 0
              }
            }
                  
            println(leafSum(Node(Node(Leaf(3), Leaf(8)), Leaf(2), Node(Leaf(5)))))
            
          }
  }
    
  /**
   * 8. Extend the tree in the preceding exercise so that each non-leaf node stores an
   * operator in addition to the child nodes. Then write a function eval that computes
   * the value. For example, the tree
   *     +
   *    /|\
   *   * 2 -
   *  / \  |
   * 3   8 5
   * 
   * has value (3 × 8) + 2 + (–5) = 21.
   */
  
  new Task("Task 8"){
          def solution() = {

            abstract class extBinaryTree
            case class Leaf(value: Int) extends extBinaryTree
            case class Node(operator: Char, children: extBinaryTree*) extends extBinaryTree
            
            
            def eval(tree: extBinaryTree): Int = {
              tree match {
                case _ => 0 
              }
            }
            
          }
  }
    
  /**
   * Write a function that computes the sum of the non-None values in a List[Option[Int]]. 
   * Don’t use a match statement.
   */
  
  new Task("Task 9"){
          def solution() = {
            
            def compute(values: List[Option[Int]]): Int = {
              values.map(_.getOrElse(0)).sum
            }
            
            println(compute(List(None, None, None, Some(4), None, Some(5))))
  }
    
                
  /**
   * 10. Write a function that composes two functions of type Double => Option[Double],
   * yielding another function of the same type. The composition should yield None if
   * either function does. For example,
   * def f(x: Double) = if (x >= 0) Some(sqrt(Double)) else None
   * def g(x: Double) = if (x != 1) Some(1 / (x - 1)) else None
   *  val h = compose(f, g)
   * Then h(2) is Some(1), and h(1) and h(0) are None.       
   */
          
  new Task("Task 10"){
          def solution() = {
            
            import scala.math._
            
            def compose(f: Double => Option[Double], g: Double => Option[Double]) = {
              (x: Double) => f(x) match {
                case Some(y) => g(y)
                case None => None
              } 
            }
          
          
          def f(x: Double) = if (x >= 0) Some(sqrt(x)) else None
          def g(x: Double) = if (x != 1) Some(1 / (x - 1)) else None
          val h = compose(f, g)
          println(h(2), h(1), h(0))
          
          }             
            
          }
  }
  
}