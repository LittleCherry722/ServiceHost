import sun.security.util.Length
import scala.collection.mutable.ArrayBuffer

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
    	println(signum(10));
    	println(signum(0));
    	println(signum(-29));

    }
  }
  def signum(n:Double) : Integer ={
    if(n==0) 0
    else if (n<0) -1
    else 1
  }

  new Task("Task 2") {
    def solution() = {
    	println("Value is \"No value\", type is \"Unit\"");

    }
  }

  new Task("Task 3") {
    def solution() = {
      for(i <- 0 until 10; from = 10 - i ){
        print(from+" ");
      } 

    }
  }

  new Task("Task 4") {
    def solution() = {
    	countdown(15);

    }
  }
  def countdown(n:Int) {
    for(i <- 0 to n; from = n - i ){
        print(from+" ");
      } 
  }
  new Task("Task 5") {
    def solution() = {
    	println(degree(2,5));
    	println(degree(2,-5));
    	println(degree(2,4));
    	println(degree(2,0));

    }
  }
  def degree(x:Double, n:Int):Double = {
    val r=n%2;
    if(n==0) 1
    else if (n<0) 1/(degree(x, -n))
    else if(r == 1 && n>=0) x*degree(x, n-1)
    else degree(x, n/2) * degree(x, n/2)
  }
  new Task("Task 6") {
    def solution() = {
      val b:Array[Int] = swapAdj(Array(1,2,3,4,5));
      for(e <- b){
        print(e+" ");
      }
    }
  }
  def swapAdj(a:Array[Int]):Array[Int] = {
    for(i <- 0 until (a.length, 2)) {
      if(i+1 < a.length){
        val b = a(i+1);
    	a(i+1) = a(i)
        a(i)= b
      }
    }
    a
  }
  new Task("Task 7") {
    def solution() = {
      val b:Array[Int] = swapAdj2(Array(1,2,3,4,5,6));
      for(e <- b){
        print(e+" ");
      }
    }
  }
  
  def swapAdj2(a:Array[Int]):Array[Int] = {
    val b=for(i <- 1 until (a.length, 2)) yield a(i);
    val c=for(i <- 0 until (a.length, 2)) yield a(i);
    val r=new Array[Int](a.length);
    for(j<-0 until c.length){
      if(j<b.length){
    	r(j*2)=b(j);
      	r(j*2+1)=c(j);
      }else
        r(j*2)=c(j);
    }
    r
  }

  new Task("Task 8") {
    def solution() = {
    	 val b:Array[Int] = rearrange(Array(1,2,-3,4,-5,6,0));
      for(e <- b){
        print(e+" ");
      }
    }
  }
   def rearrange(a:Array[Int]):Array[Int] = {
     val b=ArrayBuffer[Int]();
     b ++=a.filter(_>0)
     b ++=a.filter(_<=0)
     b.toArray;
   }

  new Task("Task 9") {
    def solution() = {
    	 val b:Array[Int] = removeDuplicates(Array(1,2,-3,4,5,6,5,4,-5,6,0));
      for(e <- b){
        print(e+" ");
      }

    }
  }
   def removeDuplicates(a:Array[Int]):Array[Int] = {
     val b=ArrayBuffer[Int]();
     b ++=a
     b.distinct.toArray
   }

  new Task("Task 10") {
    def solution() = {
    	 val b:Array[Int] = dropNegs(Array(1,2,-3,4,5,6,-5,-4,-5,6,0));
      for(e <- b){
        print(e+" ");
      }

    }
  }
  def dropNegs(a:Array[Int]):Array[Int] = {
    val b=ArrayBuffer[Int]()++=a
    val indexes = for (i <- 0 until a.length if a(i) < 0) yield i
    val droppedIndexes = indexes.drop(1)
    for (j <- (0 until droppedIndexes.length).reverse){
      b.remove(droppedIndexes(j))
    } 
    b.toArray
   }

   
}
