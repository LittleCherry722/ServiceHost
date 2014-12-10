package de.tkip.vasec

object geometric {
  
  def distance(a: VPoint, b: VPoint): Double = {
    val aa = math.abs(a.x - b.x)
    val bb = math.abs(a.y - b.y)
    math.sqrt(aa*aa + bb*bb)
  }

  def distance(points: Seq[VPoint]): Double = {
    points.sliding(2).foldLeft(0.0) {
      (sum, pair) => sum + distance(pair(0), pair(1))
    }
  }

  def interpolate(A: VPoint, B: VPoint, l: Double): VPoint = {
    VSinglePoint((B.x - A.x)*l+A.x, (B.y - A.y)*l+A.y)
  }

  def intersects(a: VSinglePoint, b: VSinglePoint, rois: Seq[VROI]): Boolean = {
    rois.exists( r => (r.metricFactor == 1 && r.intersectLength(a, b) > 0.0))
  }


  // based on http://keith-hair.net/blog/2008/08/05/line-to-circle-intersection-data/
  def intersectLength(A: VPoint, B: VPoint, C: VCircle): Double = {
    val aa: Double = (B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y)
    val bb: Double = 2 * ((B.x - A.x) * (A.x - C.x) + (B.y - A.y) * (A.y - C.y))
    val cc: Double = C.x * C.x + C.y * C.y + A.x * A.x + A.y * A.y - 2 * (C.x * A.x + C.y * A.y) - C.r * C.r
    val deter: Double = bb * bb - 4 * aa * cc

    if (deter <= 0) {
      0.0
    }
    else {
      val e: Double = math.sqrt(deter);
      val u1: Double = ( - bb + e) / (2 * aa);
      val u2: Double = ( - bb - e) / (2 * aa);
      if ((u1 < 0 || u1 > 1) && (u2 < 0 || u2 > 1)) {
        if ((u1 < 0 && u2 < 0) || (u1 > 1 && u2 > 1)) {
          0.0
        }
        else {
          distance(A, B)
        }
      }
      else {

        val A2: VPoint = if ( 0 <= u2 && u2 <= 1) {
          interpolate(A, B, u2)
        }
        else {
          A
        }

        val B2: VPoint = if ( 0 <= u1 && u1 <= 1) {
          interpolate(A, B, u1)
        }
        else {
          B
        }

        distance(A2, B2)
      }
    }
  }

  def getMin[T <: VPoint](from: VPoint, a: T, b: T): T = {
    val da = distance(from, a)
    val db = distance(from, b)

    if (da < db) {
      a
    }
    else {
      b
    }
  }

  def tsp(start: VSinglePoint, end: VSinglePoint, points: Seq[VSinglePoint]): Seq[VSinglePoint] = {
    val remaining = points.toBuffer
    val forwards = scala.collection.mutable.ArrayBuffer[VSinglePoint]()
    val backwards = scala.collection.mutable.ArrayBuffer[VSinglePoint]()

    forwards += start
    backwards += end

    while (remaining.length > 0) {
      val curF = forwards.last
      val curB = backwards.last

      def opF[T <: VPoint](a: T, b: T): T = {
        getMin(curF, a, b)
      }

      def opB[T <: VPoint](a: T, b: T): T = {
        getMin(curB, a, b)
      }

      val nextF = remaining.reduce(opF[VSinglePoint])
      val nextB = remaining.reduce(opB[VSinglePoint])

      val dF = distance(curF, nextF)
      val dB = distance(curB, nextB)

      if (dF < dB) {
        remaining.remove(remaining.indexOf(nextF))
        forwards += nextF
      }
      else {
        remaining.remove(remaining.indexOf(nextB))
        backwards += nextB
      }
    }

    (forwards ++ backwards.reverse).toList
  }
}