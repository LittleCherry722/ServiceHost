import scala.collection.JavaConversions.mapAsScalaMap

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
      val map = scala.collection.mutable.Map("Product1" -> 10.0, "Product2" -> 20.0, "Product3" -> 30.0)
      println(map.mkString("\n"))
      for ((k, v) <- map) {
        map(k) = v * 0.9
      }
      println(map.mkString("\n"))
    }
  }

  new Task("Task 2") {
    def solution() = {
      val in = new java.util.Scanner(new java.io.File("myfile.txt"))
      val words = new scala.collection.mutable.HashMap[String, Int]
      while (in.hasNext()) {
        val w = in.next();
        val oldCount = words.getOrElse(w, 0);
        words(w) = oldCount + 1
      }
      in.close()
      println(words.mkString("\n"))
    }
  }

  new Task("Task 3") {
    def solution() = {
      val in = new java.util.Scanner(new java.io.File("myfile.txt"))
      var words = new scala.collection.immutable.HashMap[String, Int]
      while (in.hasNext()) {
        val w = in.next();
        val oldCount = words.getOrElse(w, 0);
        words += (w -> (oldCount + 1))
      }
      in.close()
      println(words.mkString("\n"))
    }
  }

  new Task("Task 4") {
    def solution() = {
      val in = new java.util.Scanner(new java.io.File("myfile.txt"))
      var words = scala.collection.immutable.SortedMap.empty[String, Int]
      while (in.hasNext()) {
        val w = in.next();
        val oldCount = words.getOrElse(w, 0);
        words = words + (w -> (oldCount + 1))
      }
      in.close()
      println(words.mkString("\n"))
    }
  }

  new Task("Task 5") {
    def solution() = {
      val in = new java.util.Scanner(new java.io.File("myfile.txt"))
      val words: scala.collection.mutable.Map[String, Int] = new java.util.TreeMap[String, Int]
      while (in.hasNext()) {
        val w = in.next();
        val oldCount = words.getOrElse(w, 0);
        words(w) = (oldCount + 1)
      }
      in.close()
      println(words.mkString("\n"))
    }
  }

  new Task("Task 6") {
    def solution() = {
      val map: scala.collection.mutable.Map[String, Int] = new java.util.LinkedHashMap[String, Int]
      map("Monday") = java.util.Calendar.MONDAY
      map("Tuesday") = java.util.Calendar.TUESDAY
      map("Wednesday") = java.util.Calendar.WEDNESDAY
      map("Thursday") = java.util.Calendar.THURSDAY
      map("Friday") = java.util.Calendar.FRIDAY
      map("Saturday") = java.util.Calendar.SATURDAY
      map("Sunday") = java.util.Calendar.SUNDAY
      println(map.mkString("\n"))
    }
  }

  new Task("Task 7") {
    def solution() = {
      val map: scala.collection.mutable.Map[String, String] = new java.util.LinkedHashMap[String, String]
      val propertiesName = System.getProperties().keySet()
      var maxLength = 0
      for (k <- propertiesName.toArray()) {
        if (maxLength < k.toString().length()) {
          maxLength = k.toString().length()
        }
        val propertiesValue = System.getProperty(k.toString());
        map(k.toString()) = propertiesValue
      }

      for ((k, v) <- map) {
        val pad = maxLength - k.length()
        val padding = " " * pad
        println(k + padding + " | " + v)
      }
    }
  }

  new Task("Task 8") {
    def solution() = {
      def minmax(values: Array[Int]) = {
        var min = Int.MaxValue
        var max = Int.MinValue
        for (v <- values) {
          if (v > max) {
            max = v
          }
          if (v < min) {
            min = v
          }
        }
        (min, max)
      }
      val values = Array(1, 3, -3, 4, 5, -6, 7, 0)
      println(minmax(values))
    }
  }

  new Task("Task 9") {
    def solution() = {
      def lteqgt(values: Array[Int], v: Int) = {
        var lessThan = 0
        var equal = 0
        var greaterThan = 0
        for (x <- values) {
          if (x < v) {
            lessThan += 1
          } else if (x == v) {
            equal += 1
          } else {
            greaterThan += 1
          }
        }
        (lessThan, equal, greaterThan)
      }
      val values = Array(1, 3, -3, 4, 5, -6, 7, 0)
      println(lteqgt(values, 0))
    }
  }

  new Task("Task 10") {
    def solution() = {
      println("Hello" zip "World")
      println("Zip two strings: make a sequence of pair of char. The rest of the longer string is strimmed")
    }
  }

}
