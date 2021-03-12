package org.json4s
package native

/**
 * Printer converts JSON to String.
 * Before printing a <code>JValue</code> needs to be rendered into [[Document]].
 * <p>
 * Example:<pre>
 * pretty(render(json))
 * </pre>
 *
 * @see org.json4s.JsonAST#render
 */
object Printer extends Printer
trait Printer {
  import java.io._

  /**
   * Compact printing (no whitespace etc.)
   */
  def compact(d: Document): String = compact(d, new StringWriter).toString

  /**
   * Compact printing (no whitespace etc.)
   */
  def compact[A <: Writer](d: Document, out: A): A = {
    def layout(docs: List[Document]): Unit = docs match {
      case Nil =>
      case DocText(s) :: rs => out.write(s); layout(rs)
      case DocCons(d1, d2) :: rs => layout(d1 :: d2 :: rs)
      case DocBreak :: rs => layout(rs)
      case DocNest(_, d) :: rs => layout(d :: rs)
      case DocGroup(d) :: rs => layout(d :: rs)
      case DocNil :: rs => layout(rs)
    }

    layout(List(d))
    out.flush
    out
  }

  /**
   * Pretty printing.
   */
  def pretty(d: Document): String = pretty(d, new StringWriter).toString

  /**
   * Pretty printing.
   */
  def pretty[A <: Writer](d: Document, out: A): A = {
    d.format(0, out)
    out
  }
}
