package org.json4s

import scala.annotation.{switch, tailrec}

object ParserUtil {

  class ParseException(message: String, cause: Exception) extends Exception(message, cause)

  private[this] val EOF = -1.asInstanceOf[Char]

  def quote(s: String, alwaysEscapeUnicode: Boolean): String =
    quote(
      s = s,
      appender = new java.lang.StringBuilder,
      alwaysEscapeUnicode = alwaysEscapeUnicode
    ).toString

  private[json4s] def quote[T <: java.lang.Appendable](s: String, appender: T, alwaysEscapeUnicode: Boolean): T = { // hot path
    var i = 0
    val l = s.length
    while (i < l) {
      (s(i): @annotation.switch) match {
        case '"' => appender.append("\\\"")
        case '\\' => appender.append("\\\\")
        case '\b' => appender.append("\\b")
        case '\f' => appender.append("\\f")
        case '\n' => appender.append("\\n")
        case '\r' => appender.append("\\r")
        case '\t' => appender.append("\\t")
        case c =>
          val shouldEscape = if (alwaysEscapeUnicode) {
            c >= 0x80
          } else {
            (c >= '\u0000' && c <= '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')
          }
          if (shouldEscape)
            appender.append("\\u%04X".format(c: Int))
          else appender.append(c)
      }
      i += 1
    }
    appender
  }

  def unquote(string: String): String =
    unquote(new Buffer(new java.io.StringReader(string), false))

  private[json4s] def unquote(buf: Buffer): String = {
    def unquote0(buf: Buffer, base: String): String = {
      val s = new java.lang.StringBuilder(base)
      var c = '\\'
      while (c != '"') {
        if (c == '\\') {
          (buf.next(): @switch) match {
            case '"' => s.append('"')
            case '\\' => s.append('\\')
            case '/' => s.append('/')
            case 'b' => s.append('\b')
            case 'f' => s.append('\f')
            case 'n' => s.append('\n')
            case 'r' => s.append('\r')
            case 't' => s.append('\t')
            case 'u' =>
              val chars = Array(buf.next(), buf.next(), buf.next(), buf.next())
              val codePoint = Integer.parseInt(new String(chars), 16)
              s.appendCodePoint(codePoint)
            case _ => s.append('\\')
          }
        } else s.append(c)
        c = buf.next()
      }
      s.toString
    }

    buf.eofIsFailure = true
    buf.mark()
    var c = buf.next()
    while (c != '"') {
      if (c == '\\') {
        val s = unquote0(buf, buf.substring)
        buf.eofIsFailure = false
        return s
      }
      c = buf.next()
    }
    buf.eofIsFailure = false
    buf.substring
  }

  /* Buffer used to parse JSON.
   * Buffer is divided to one or more segments (preallocated in Segments pool).
   */
  private[json4s] class Buffer(in: java.io.Reader, closeAutomatically: Boolean) {
    private[this] var offset = 0
    private[this] var curMark = -1
    private[this] var curMarkSegment = -1
    var eofIsFailure = false
    private[this] var segments: Vector[Segment] = Vector(Segments.apply())
    private[this] var segment: Array[Char] = segments.head.seg
    private[this] var cur = 0 // Pointer which points current parsing location
    private[this] var curSegmentIdx = 0 // Pointer which points current segment

    def mark(): Unit = { curMark = cur; curMarkSegment = curSegmentIdx }
    def back(): Unit = cur = cur - 1

    def next(): Char = {
      if (cur == offset && read() < 0) {
        if (eofIsFailure) throw new ParseException("unexpected eof", null) else EOF
      } else {
        val c = segment(cur)
        cur += 1
        c
      }
    }

    def substring: String = {
      if (curSegmentIdx == curMarkSegment) new String(segment, curMark, cur - curMark - 1)
      else { // slower path for case when string is in two or more segments
        var parts: List[(Int, Int, Array[Char])] = Nil
        var i = curSegmentIdx
        while (i >= curMarkSegment) {
          val s = segments(i).seg
          val start = if (i == curMarkSegment) curMark else 0
          val end = if (i == curSegmentIdx) cur else s.length + 1
          parts = (start, end, s) :: parts
          i = i - 1
        }
        val len = parts.map(p => p._2 - p._1 - 1).sum
        val chars = new Array[Char](len)

        @tailrec
        def loop(xs: List[(Int, Int, Array[Char])], pos: Int): Unit = {
          xs match {
            case (start, end, b) :: tail =>
              val partLen = end - start - 1
              System.arraycopy(b, start, chars, pos, partLen)
              loop(tail, pos + partLen)
            case _ =>
          }
        }
        loop(parts, 0)
        new String(chars)
      }
    }

    def near: String = new String(segment, (cur - 20) max 0, 20 min cur)

    def release(): Unit = segments.foreach(Segments.release)

    private[json4s] def automaticClose(): Unit = if (closeAutomatically) in.close()

    private[this] def read(): Int = {
      if (offset >= segment.length) {
        val newSegment = Segments.apply()
        offset = 0
        segment = newSegment.seg
        segments = segments :+ newSegment
        curSegmentIdx = segments.length - 1
      }

      val length = in.read(segment, offset, segment.length - offset)
      cur = offset
      offset += length
      length
    }
  }

  private[json4s] def defaultSegmentSize: Int = 1000

  private[this] val BrokenDouble = BigDecimal("2.2250738585072012e-308")
  private[json4s] def parseDouble(s: String) = {
    val d = BigDecimal(s)
    if (d == BrokenDouble) sys.error("Error parsing 2.2250738585072012e-308")
    else d.doubleValue
  }

}
