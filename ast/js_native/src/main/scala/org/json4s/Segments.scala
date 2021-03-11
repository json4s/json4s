package org.json4s

private[json4s] object Segments {
  def apply(): Segment = {
    Segment(new Array(ParserUtil.defaultSegmentSize))
  }

  def release(s: Segment): Unit = ()
}

private[json4s] final case class Segment(val seg: Array[Char])
