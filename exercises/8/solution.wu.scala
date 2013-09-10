import scala.collection.mutable.ArrayBuffer

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
    println(name + ":");
    solution();
    println("\n");
  }
}

class Tasks {
  private var tasks = Seq[Task]();
  def add(t: Task) = { tasks :+= t }
  def execute() = { tasks.foreach((t: Task) => { t.execute(); }) }
  def execute(name: String) = { tasks.filter(_.name == name)(0).execute() }
}

/* insert your solutions below */

object Tasks extends Tasks {

  new Task("Task 1") {
    def solution() = {

      // your solution for task 1 here
      val account = new CheckingAccount(100)
      account.deposit(50)
      println(account.showBalance)
      account.withdraw(50)
      println(account.showBalance)

    }

    class BankAccount(initialBalance: Double) {
      private var balance = initialBalance
      def deposit(amount: Double) = { balance += amount; balance }
      def withdraw(amount: Double) = { balance -= amount; balance }
      def showBalance = this.balance
    }

    class CheckingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      override def deposit(amount: Double) = { super.deposit(amount - 1) }
      override def withdraw(amount: Double) = { super.withdraw(amount + 1) }
    }

  }

  new Task("Task 2") {
    def solution() = {

      // your solution for task 2 here
      val account = new SavingAccount(100)
      account.deposit(50)
      println(account.showBalance)
      account.withdraw(50)
      println(account.showBalance)
      account.deposit(10)
      println(account.showBalance)
      account.deposit(20)
      println(account.showBalance)
      println(account.earnMonthlyInterest(0.01))

    }

    class BankAccount(initialBalance: Double) {
      private var balance = initialBalance
      def deposit(amount: Double) = { balance += amount; balance }
      def withdraw(amount: Double) = { balance -= amount; balance }
      def showBalance = this.balance
    }

    class SavingAccount(initialBalance: Double) extends BankAccount(initialBalance) {
      var counter = 0
      override def deposit(amount: Double) = {
        counter += 1
        if (counter <= 3)
          super.deposit(amount)
        else super.deposit(amount - 1)
      }

      override def withdraw(amount: Double) = {
        counter += 1
        if (counter <= 3)
          super.withdraw(amount)
        else super.withdraw(amount + 1)
      }
      
      def earnMonthlyInterest(rate: Double) = {
        counter = 0
        super.deposit(super.showBalance * rate)
      }

    }
  }

  new Task("Task 3") {
    def solution() = {

      // your solution for task 3 here
      println("see the code")

    }
    
    class shape(){
      def draw(){}
      def erase(){}
    }
    
    class Circle extends shape{
      override def draw(){ println("circle.draw()")}
      override def erase(){ println("circle.erase()")}
    }
    
    class Square extends shape{
      override def draw(){ println("square.draw()")}
      override def erase(){ println("square.erase()")}
    }
    
    class Triangle extends shape{
      override def draw(){ println("triangle.draw()")}
      override def erase(){ println("triangle.erase()")}
    }
  }

  new Task("Task 4") {
    def solution() = {

      // your solution for task 4 here
      val item1 = new SimpleItem(20, "iPhone")
      val item2 = new SimpleItem(10, "iPod")
      val item3 = new SimpleItem(30, "Google Glass")
      val bundle = new Bundle("some stuff")
      bundle.addItem(item1)
      bundle.addItem(item2)
      bundle.addItem(item3)
      println(bundle.price)

    }
    
    abstract class Item{
      def price: Double
      def description: String
    }
    
    class SimpleItem(val price: Double, val description: String) extends Item{
      
    }
    
    class Bundle(val description: String) extends Item{
      private val items = new ArrayBuffer[Item]
      def addItem(item: Item) {
        items += item
      }
      def price = {
        var sum = 0.0
        for (elem <- items) sum += elem.price
        sum
      }
    }
  }

  new Task("Task 5") {
    def solution() = {

      // your solution for task 5 here
      val p = new LabeledPoint("north star", 122.2, 334.6)
      println(p.description +": " +p.x +","+ p.y)

    }
    
    class Point(val x: Double, val y: Double){
      
    }
    
    class LabeledPoint(val description: String, x: Double, y: Double) extends Point(x,y){
      
    }
  }

  new Task("Task 6") {
    def solution() = {

      // your solution for task 6 here
      val s1 = new Rectangle(2,2,4,6)
      println(s1.centerPoint)
      val s2 = new Circle(3,4,5)
      println(s2.centerPoint)

    }
    
    abstract class shape{
      def centerPoint: (Double,Double)
    }
    
    class Rectangle(val xmin: Double, val ymin: Double, val xmax: Double, val ymax: Double) extends shape{
      def centerPoint = {
        ( (xmax - xmin)/2, (ymax - ymin)/2 )
      }
    }
    
    class Circle(val x: Double, val y: Double, val radius: Double){
      def centerPoint = {
        (x,y)
      }
    }
  }

}
