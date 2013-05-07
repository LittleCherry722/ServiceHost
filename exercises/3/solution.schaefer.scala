import java.util.Properties
import scala.collection.mutable.ArrayBuffer
import java.util.Calendar

object Solution extends App {

  // execute all tasks
  Tasks.execute();

  // execute only a single one
  //TaskManager.execute("Task 1");
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
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }

  def printArray(arr: Array[Int]) {
    for (i <- 0 to arr.length - 1) {
      print(arr(i) + " ")
    }
    println()
  }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 2") {
    //3.2
    def solution() = {
      val arr = Array(1, 2, 3, 4, 5, 6, 7)
      printArray(arr)
      swap(arr)
      printArray(arr)
    }

    def swap(arr: Array[Int]): Array[Int] = {
      for (i <- 0 until arr.length - 2 by 2) {
        val tmp = arr(i)
        arr(i) = arr(i + 1)
        arr(i + 1) = tmp
      }
      arr
    }
  }

  new Task("Task 3") {
    //3.3
    def solution() = {
      val arr = Array(1, 2, 3, 4, 5, 6, 7)
      printArray(arr)
      val brr = swapYield(arr)
      printArray(brr)
      printArray(arr)
    }

    def swapYield(arr: Array[Int]): Array[Int] = {
      val brr = for (elem <- arr.grouped(2)) yield elem match {
        case Array(a, b) => Array(b, a)
        case Array(c) => Array(c)
      }
      brr.flatten.toArray
    }
  }

  new Task("Task 4") {
    //3.4
    def solution() = {
      val arr = Array(5, 0, -3, 8, 7, 8, -2, 0, 42)
      printArray(strangeSort(arr))

    }

    def strangeSort(arr: Array[Int]): Array[Int] = {
      val arrBuff = ArrayBuffer[Int]()
      arrBuff ++= arr.filter(_ > 0)
      arrBuff ++= arr.filter(_ <= 0)
      arrBuff.toArray
    }
  }

  new Task("Task 7") {
    //3.7
    def solution() = {
      val array = Array(1, 1, 5, 3, 1, 5, 3, 0, 6, 38)
      printArray(array)
      //distinct liefert ein neues Array ohne Duplikate
      printArray(array.distinct)
    }
  }

  new Task("Task 8") {
    //3.8
    def solution() = {

      val arrBuff = ArrayBuffer(3, 5, 6, -8, 3, 2, -8, -7, -42, 7)

      val negNumbers = for (i <- 0 to arrBuff.length - 1 if (arrBuff(i) < 0)) yield i

      for (i <- negNumbers.drop(1).reverse) arrBuff.remove(i)

      printArray(arrBuff.toArray)
    }
  }
}
