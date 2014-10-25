package de.tkip.sbpm.misc

/**
 * Extend this trait to increase performance and cache the HashCode for
 * immutable(!) case classes
 */
trait HashCodeCache {
  self: Product =>
  override lazy val hashCode: Int = scala.runtime.ScalaRunTime._hashCode(this)
}