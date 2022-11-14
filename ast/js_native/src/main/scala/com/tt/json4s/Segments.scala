package com.tt.json4s

private[json4s] object Segments {
  private[json4s] var segmentSize: Int = ParserUtil.defaultSegmentSize
  def apply(): Segment = {
    Segment(new Array(segmentSize))
  }

  def release(s: Segment): Unit = ()

  private[json4s] def clear(): Unit = ()
}

private[json4s] final case class Segment(seg: Array[Char])
