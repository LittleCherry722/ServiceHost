/**
 * Please don't run this file directly. If you want to run it,
 * please create individual file for every class.
 */
// task chapter12_1 begin

object Chapter12_1 extends App {
  def values(fun: (Int) => Int, low: Int, hight: Int) = {
    val collection = scala.collection.mutable.Map[Int, Int]()
    low to hight foreach {
      num =>
        collection += (num -> fun(num))
    }
    for (k <- low to hight)
      print("(" + k + "," + collection(k) + ") ")
  }
  values(x => x * x, -5, 5)
}
//task chapter12_1 end

//task chapter12_2 begin

object Chapter12_2 extends App {
  val num = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 23, -4, -89, 87, 21)
  val largestNum = num.reduceLeft((a, b) => if (a > b) b else b)

  print(largestNum)
}
//task chapter12_2 end

//task chapter12_3 begin

object Chapter12_3 extends App {
  print("Input a number:")
  val num = readInt()
  
  print(num + "! = " + (1 to num).reduceLeft(_ * _))

}
//task chapter12_3 end

//task chapter12_4 begin

object Chapter12_4 extends App{
	println((1 to -3).foldLeft(1)(_ * _))
}
// task chapter12_4 end

// task chapter12_5 begin

object Chapter12_5 extends App {
  def largestAt(fun: (Int) => Int, inputs: Seq[Int]) = {
    fun(inputs.reduceLeft((a, b) => if (fun(a) >= fun(b)) a else b))
  }
  
  println(largestAt(x => 10 * x - x * x, 1 to 10))
}
//task chapter12_5 end

//task chapter12_10 begin

object Chapter12_10 extends App {
  def unless(condition: => Boolean)(block: => Unit) {
    if (!condition) {
      block
      unless(condition)(block)
    }
  }
  var x = 10
  unless(x == 0) {
    x -= 1
    println(x)
  }
  println("The first parameter need to be a call-by-name." +
    "Currying should be needed.")
}
//task chapter12_10 end
