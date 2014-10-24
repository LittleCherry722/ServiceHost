package de.tkip.sbpm.newmodel

sealed trait RangeLimit
case class Number(value: Int) extends RangeLimit
// '+' for send states |user|
case object AllUser extends RangeLimit
// '*' for receive states |messages|
case object AllMessages extends RangeLimit
