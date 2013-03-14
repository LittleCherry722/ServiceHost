package de.tkip.sbpm.persistence.query

object Schema {
  trait Query extends BaseQuery
  
  case object Create extends Query
  case object Drop extends Query
  case object Recreate extends Query
}