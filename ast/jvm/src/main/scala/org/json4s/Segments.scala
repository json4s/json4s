package org.json4s

/* A pool of pre-allocated char arrays.
 */
private[json4s] object Segments {
  import java.util.concurrent.ArrayBlockingQueue
  import java.util.concurrent.atomic.AtomicInteger

  private[json4s] var segmentSize: Int = ParserUtil.defaultSegmentSize
  private[this] val maxNumOfSegments = 10000
  private[this] val segmentCount = new AtomicInteger(0)
  private[this] val segments = new ArrayBlockingQueue[Segment](maxNumOfSegments)
  private[json4s] def clear(): Unit = segments.clear()

  def apply(): Segment = {
    val s = acquire
    // Give back a disposable segment if pool is exhausted.
    if (s != null) s else DisposableSegment(new Array(segmentSize))
  }

  private[this] def acquire: Segment = {
    val curCount = segmentCount.get
    val createNew =
      if (segments.size == 0 && curCount < maxNumOfSegments)
        segmentCount.compareAndSet(curCount, curCount + 1)
      else false

    if (createNew) RecycledSegment(new Array(segmentSize)) else segments.poll
  }

  def release(s: Segment): Unit = s match {
    case _: RecycledSegment => segments.offer(s)
    case _ =>
  }

  private[this] final case class RecycledSegment(seg: Array[Char]) extends Segment
  private[this] final case class DisposableSegment(seg: Array[Char]) extends Segment
}

private[json4s] sealed abstract class Segment extends Product with Serializable {
  val seg: Array[Char]
}
