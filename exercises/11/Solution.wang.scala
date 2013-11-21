package test
/**
 * Please don't run this file directly. If you want to run it, 
 * please create individual file.
 */
//task chapter11_1 begin
object Chapter11_1 extends App {
  val resultOne = 3 + 4 -> 5
  //val resultTwo = 3 -> 4 + 5
  
  println("+ and -> have the same precedence.")
  println("(3 + 4 -> 5) is evaluated left to right: " + resultOne)
  println("(3 -> 4 + 5) should be evaluated left to right. " +
    "(3 -> 4) is Map[Int,Int], but 5 is Int." +
    " There is an error 'type mismatch'.")
}
//task chapter11_1 end

//task chapter11_4 begin

class Money(val dollars: Int, val cents: Int) {
  def +(other: Money): Money = {
    val (currentDollar, currentCent) =
      if ((this.cents + other.cents) >= 100)
        (1, this.cents + other.cents - 100)
      else
        (0, this.cents + other.cents)
    new Money(this.dollars + other.dollars +
      currentDollar, currentCent)
  }
  def -(other: Money): Money = {
    val newDollar = ((this.dollars * 100 + this.cents)
      - (other.dollars * 100 + other.cents)) / 100
    val newCent = ((this.dollars * 100 + this.cents)
      - (other.dollars * 100 + other.cents)) % 100
    new Money(newDollar, newCent)
  }
  def ==(other: Money): Boolean = {
    (this.dollars == other.dollars) &&
    (this.cents == other.cents)
  }
  def <(other: Money): Boolean = {
    (this.dollars < other.dollars) ||
    (this.dollars == other.dollars &&
    this.cents < other.cents)
  }
  override def toString = "Money(" + dollars + "," + cents + ")"
}
object Money {
  def apply(dollar: Int, cent: Int) = {
    new Money(dollar, cent)
  }
}

object Chapter11_4 extends App {
  val thisMoney = Money(11, 30)
  val otherMoney = Money(3, 89)
  
  println(thisMoney + otherMoney)
  println(thisMoney - otherMoney)
  println(thisMoney == otherMoney)
  println(thisMoney < otherMoney)
  print("In general, * and / are not necessary." +
    " Because money don't need to do these operation. ")
}
//task chapter11_4 end

//task chapter11_7 begin

class BitSequence(var bits: String) {
  var num = pack(bits)

  def pack(nbits: String): Long = {
    if (nbits.charAt(0).toString.toInt == 1) {
      num = java.lang.Long.parseLong(nbits.patch(0, "0", 1), 2)
      num *= -1
    } else {
      num = java.lang.Long.parseLong(nbits, 2)
    }
    num
  }
  def apply(pos: Int): Int = {
    bits.charAt(pos).toString.toInt
  }
  def update(pos: Int, value: Int) {
    bits = bits.patch(pos, value.toString, value.toString.length())
    pack(bits)
  }

}
object Aufgabe11_7 extends App {
  val t = new BitSequence("0000000000000000000000000000000000000000000000000000000000000001")
  val s = new BitSequence("1101010101010101010101010101010101010101010101010101010101010101")

  println(s(63))
  println(s.num)
  s(62) = 1
  println(s.num)

}
//task chapter11_7 end