package org.json4s.native

import java.io.Writer
import org.json4s.native.Document.FmtState
import scala.annotation.tailrec

case object DocNil extends Document
case object DocBreak extends Document
case class DocText(txt: String) extends Document
case class DocGroup(doc: Document) extends Document
case class DocNest(indent: Int, doc: Document) extends Document
case class DocCons(hd: Document, tl: Document) extends Document

/**
 * A basic pretty-printing library, based on Lindig's strict version
 * of Wadler's adaptation of Hughes' pretty-printer.
 *
 * derived from [[https://github.com/scala/scala/blob/v2.11.8/src/library/scala/text/Document.scala]]
 * @author Michel Schinz
 */
sealed abstract class Document extends Product with Serializable {
  def ::(hd: Document): Document = DocCons(hd, this)
  def ::(hd: String): Document = DocCons(DocText(hd), this)
  def :/:(hd: Document): Document = hd :: DocBreak :: this
  def :/:(hd: String): Document = hd :: DocBreak :: this

  /**
   * Format this document on `writer` and try to set line
   * breaks so that the result fits in `width` columns.
   */
  def format(width: Int, writer: Writer): Unit = {
    @tailrec
    def fits(w: Int, state: List[FmtState]): Boolean = state match {
      case _ if w < 0 =>
        false
      case List() =>
        true
      case FmtState(_, _, DocNil) :: z =>
        fits(w, z)
      case FmtState(i, b, DocCons(h, t)) :: z =>
        fits(w, FmtState(i, b, h) :: FmtState(i, b, t) :: z)
      case FmtState(_, _, DocText(t)) :: z =>
        fits(w - t.length(), z)
      case FmtState(i, b, DocNest(ii, d)) :: z =>
        fits(w, FmtState(i + ii, b, d) :: z)
      case FmtState(_, false, DocBreak) :: z =>
        fits(w - 1, z)
      case FmtState(_, true, DocBreak) :: _ =>
        true
      case FmtState(i, _, DocGroup(d)) :: z =>
        fits(w, FmtState(i, false, d) :: z)
    }

    def spaces(n: Int): Unit = {
      var rem = n
      while (rem >= 16) { writer write "                "; rem -= 16 }
      if (rem >= 8) { writer write "        "; rem -= 8 }
      if (rem >= 4) { writer write "    "; rem -= 4 }
      if (rem >= 2) { writer write "  "; rem -= 2 }
      if (rem == 1) { writer write " " }
    }

    @tailrec
    def fmt(k: Int, state: List[FmtState]): Unit = state match {
      case List() => ()
      case FmtState(_, _, DocNil) :: z =>
        fmt(k, z)
      case FmtState(i, b, DocCons(h, t)) :: z =>
        fmt(k, FmtState(i, b, h) :: FmtState(i, b, t) :: z)
      case FmtState(i @ _, _, DocText(t)) :: z =>
        writer write t
        fmt(k + t.length(), z)
      case FmtState(i, b, DocNest(ii, d)) :: z =>
        fmt(k, FmtState(i + ii, b, d) :: z)
      case FmtState(i, true, DocBreak) :: z =>
        writer write "\n"
        spaces(i)
        fmt(i, z)
      case FmtState(i @ _, false, DocBreak) :: z =>
        writer write " "
        fmt(k + 1, z)
      case FmtState(i, b @ _, DocGroup(d)) :: z =>
        val fitsFlat = fits(width - k, FmtState(i, false, d) :: z)
        fmt(k, FmtState(i, !fitsFlat, d) :: z)
      case _ =>
        ()
    }

    fmt(0, FmtState(0, false, DocGroup(this)) :: Nil)
  }
}

object Document {
  private final case class FmtState(
    i: Int,
    b: Boolean,
    d: Document
  )

  /** The empty document */
  def empty: Document = DocNil

  /** A break, which will either be turned into a space or a line break */
  def break: Document = DocBreak

  /** A document consisting of some text literal */
  def text(s: String): Document = DocText(s)

  /**
   * A group, whose components will either be printed with all breaks
   * rendered as spaces, or with all breaks rendered as line breaks.
   */
  def group(d: Document): Document = DocGroup(d)

  /** A nested document, which will be indented as specified. */
  def nest(i: Int, d: Document): Document = DocNest(i, d)
}
