
//Scala for Impatient - Exercise 12
//David Dornseifer

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
   * Write a function values(fun: (Int) => Int, low: Int, high: Int) that yields a
   * collection of function inputs and outputs in a given range. For example, values(x
   * => x * x, -5, 5) should produce a collection of pairs (-5, 25), (-4, 16), (-3, 9),
   * . . ., (5, 25).
   */
  
  new Task("Task 1"){
          def solution() = {
            
            def func(f: (Int) => Int, low: Int, high: Int) = {
              for (i <- low to high) {
                println((i,f(i)))
              }
            }
            
            func(x => x * x, -5, 5)
          }
  }
  
  /**
   * 2. How do you get the largest element of an array with reduceLeft?
   */
  
  new Task("Task 2"){
          def solution() = {

        	  val array = Array(-5, 1.5, 4, 6, 1000, -345)
        	  print(
        	      array.reduceLeft( (elem1, elem2) => {
        	        if (elem1 > elem2) {
        	          elem1
        	        } else {
        	          elem2
        	        }
        	      }    
        	  )
            )
          }
  }
  

  /**
   * 3. Implement the factorial function using to and reduceLeft, without a loop or
   * recursion.
   */
  
  new Task("Task 3"){
          def solution() = {
        	  
              def fac(n: Int): Double = {
                if ( n == 0) {
                  1
                } else {
                  (1 to n).reduceLeft(_ * _)
                }
              }
              
              println(fac(0))
              println(fac(5))
            
          }
  }
  
  /**
   * 4. The previous implementation needed a special case when n < 1. Show how you
   * can avoid this with foldLeft. (Look at the Scaladoc for foldLeft. It’s like
   * reduceLeft, except that the first value in the chain of combined values is supplied
   * in the call.)
   */
  
  new Task("Task 4"){
          def solution() = {
            

        	def fac(n: Int): Double = {
        	  (1 to n).toList.foldLeft(1)((init, elem) => init * elem)
              }
              
              println(fac(0))
              println(fac(5))
            
          }
  }  
  
  
  /**
   * 5. Write a function largest(fun: (Int) => Int, inputs: Seq[Int]) that yields the
   * largest value of a function within a given sequence of inputs. For example,
   * largest(x => 10 * x - x * x, 1 to 10) should return 25. Don’t use a loop or
   * recursion.
   */
  
  new Task("Task 5"){
          def solution() = {

               def largest(f: (Int) => Int, inputs: Seq[Int]) = {
                 inputs.map(f(_)).max
               }
               
               println(largest(x => 10 * x - x * x, 1 to 10))
          }
  }
  
/**
 * 6. Modify the previous function to return the input at which the output is largest. For
 * example, largestAt(fun: (Int) => Int, inputs: Seq[Int]) should return 5.
 * Don’t use a loop or recursion.
 */
  
  new Task("Task 6"){
            def solution() = {

               def largest(f: (Int) => Int, inputs: Seq[Int]) = {
                 inputs.zipWithIndex.map(i => (i._1, f(i._1))).maxBy(_._2)._1
               }
            
               println(largest(x => 10 * x - x * x, 1 to 10))
          }
  } 
   
  /**
   * 7. It’s easy to get a sequence of pairs, for example
   * val pairs = (1 to 10) zip (11 to 20)
   * Now suppose you want to do something with such a sequence—say, add up
   * the values. But you can’t do pairs.map(_ + _) The function _ + _ takes two Int 
   * parameters, not an (Int, Int) pair. Write a function adjustToPair that receives a 
   * function of type (Int, Int) => Int and returns the equivalent function that 
   * operates on a pair. For example, adjustToPair(_ * _)((6, 7)) is 42. Then use 
   * this function in conjunction with map to compute the sums of the elements in pairs.
   */
  
  new Task("Task 7"){
          def solution() = {
            
            def adjustToPair(f: (Int, Int) => Int) : ((Int, Int)) => Int = {
              case(x, y) => f(x, y)
            }
            
            println(adjustToPair(_ * _)((6, 7)))
           
          }
  }
    
  /**
   * 8. In Section 12.8, “Currying,” on page 149, you saw the corresponds method used
   * with two arrays of strings. Make a call to corresponds that checks whether the
   * elements in an array of strings have the lengths given in an array of integers.
   */
  
  new Task("Task 8"){
          def solution() = {
           val a = Array("Hello", "World")
           val b = Array(5, 5)
           
           println(a.corresponds(b)(_.length() == (_)))

            
          }
  }
  
  /**
   * 9. Implement corresponds without currying. Then try the call from the preceding
   * exercise. What problem do you encounter?
   */
  
  new Task("Task 9"){
          def solution() = {
            
            def newCorresponds(str: Array[String], len: Array[Int],
                func:(String, Int) => Boolean) = {
              str.zip(len).map(t => func(t._1, t._2)).max
            }
            val a = Array("Hello", "World")
            val b = Array(5, 5)
            
            println(newCorresponds(a, b, _.length() == (_)))
          }
  }
  
  // There is the problem, that we have to deal with with the data-types.

  /**
   * 10. Implement an unless control abstraction that works just like if, but with an
   * inverted condition. Does the first parameter need to be a call-by-name parameter?
   * Do you need currying?
   */
  
  new Task("Task 10"){
          def solution() = {

          def invertedIf(condition: => Boolean)(block: => Unit) {
            if (!condition) {
              block
            }
          }
          
          invertedIf( 1 != 1)(println("1 != 1 => TRUE"))
     
          }
  }
  
}