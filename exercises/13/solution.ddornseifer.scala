//Book-Exercises 13
//David Dornseifer

import scala.Function2

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
 * 1. Write a function that, given a string, produces a map of the indexes of all
 * characters. For example, indexes("Mississippi") should return a map associating
 * 'M' with the set {0}, 'i' with the set {1, 4, 7, 10}, and so on. Use a mutable map
 * of characters to mutable sets. How can you ensure that the set is sorted?  
 */
  
  new Task("Task 1"){
          def solution() = {
            
            import scala.collection.mutable
            def indexes(input: String): mutable.HashMap[Char, mutable.SortedSet[Int]] = {
              var index = new mutable.HashMap[Char, mutable.SortedSet[Int]]
              var counter = 0
              for(c <- input) {
                if (index.contains(c)) {
                  index(c) += counter
                } else {
                  index += (c -> mutable.SortedSet[Int](counter))
                }
                counter += 1
              }
              index  
              }
   
            println(indexes("Mississippi"))
   
          }
  }
  
  // The order can be ensured by using a mutable.SortedSet
  
  /**
   * 2. Repeat the preceding exercise, using an immutable map of characters to lists.
   */
  
  new Task("Task 2"){
          def solution() = {
            
            import scala.collection.mutable
            import scala.collection.immutable
            
            def indexes(input: String): immutable.HashMap[Char, mutable.SortedSet[Int]] = {
              var index = new immutable.HashMap[Char, mutable.SortedSet[Int]]
              var counter = 0
              for(c <- input) {
                if (index.contains(c)) {
                  index(c) += counter
                } else {
                  index += (c -> mutable.SortedSet[Int](counter))
                }
                counter += 1
              }
              index  
              }
   
            println(indexes("Mississippi"))
   
          }
  }
  
  /**
   * 3. Write a function that removes all zeroes from a linked list of integers.
   */
  
  new Task("Task 3"){
          def solution() = {
            import collection.mutable.LinkedList
            
            
            def removeZeros(zeroList: LinkedList[Int]): LinkedList[Int] = {
              zeroList.filter(_ != 0)
            }

            val list = LinkedList(1, 0, -1, 3, 4, -7)          
            println(removeZeros(list))
          }
  }
  
  /**
   * 4. Write a function that receives a collection of strings and a map from strings to
   * integers. Return a collection of integers that are values of the map corresponding
   * to one of the strings in the collection. For example, given Array("Tom", "Fred",
   * "Harry") and Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5), return Array(3, 5).
   * Hint: Use flatMap to combine the Option values returned by get.
   */
  
  new Task("Task 4"){
          def solution() = {
            
            def StringToInt(str: Array[String], int: Map[String, Int]): Array[Int] = {
              str.flatMap(int.get(_))
            }
            
            val str = Array("Tom", "Fred", "Harry")
            val int = Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5)
            
            println(StringToInt(str, int).mkString(", "))
        	 
          }
  }
  
  /**
   * 5. Implement a function that works just like mkString, using reduceLeft.
   */
  new Task("Task 5"){
          def solution() = {

            def likeMKString(str: Array[String], divider: String): String = {
              str.reduceLeft(_ + divider + _ )
              
            }
                        
            val str = Array("Dies", "ist", "ein", "Array")
            val div = ", "
            
            println(likeMKString(str, div))
            
          }
  }
  
  /**
   * 6. Given a list of integers lst, what is (lst :\ List[Int]())(_ :: _)? (List[Int]()
   * /: lst)(_ :+ _)? How can you modify one of them to reverse the list?
   */
  
  new Task("Task 6"){
          def solution() = {
            
            val lst = List[Int](1, 2, 4, 0, 6 , -8, 10)
            
            
            //appends each element piece by piece
            println( (lst :\ List[Int]())(_ :: _) )
            println( (List[Int]() /: lst)(_ :+ _) )
            
            //reverse
            println( (List[Int]() /: lst) ((x, y) => (y::x)) )
            
            /**
             * :\ It applies a binary operator to all elements - right to left
             * :: Adds element at the beginning on the list
             * /: Applies binary operator to a start value and all elements - left to right
             */

          }
  } 
   
  /**
   * 7. In Section 13.11, “Zipping,” on page 171, the expression (prices zip
   * quantities) map { p => p._1 * p._2 } is a bit inelegant. We can’t do (prices zip
   * quantities) map { _ * _ } because _ * _ is a function with two arguments, and
   * we need a function with one argument that is a tuple. The tupled method of the
   * Function2 class changes a function with two arguments to one that takes a tuple.
   * Apply tupled to the multiplication function so you can map it over the list of
   * pairs.
   */
  
  new Task("Task 7"){
          def solution() = {
        
            
            val prices = List(5.0, 20.0, 9.95)
            val quantities = List(10, 2, 1)

            //(prices zip quantities) map { tupled(_ * _)}
                      
            
          }
  }
    
}