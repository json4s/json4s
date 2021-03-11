package org.json4s
package native

import org.json4s.JsonAST.JField
import org.json4s.native.Document._
import org.json4s.prefs.EmptyValueStrategy

trait JsonMethods extends org.json4s.JsonMethods[Document] {

  def parse[A: AsJsonInput](in: A, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): JValue =
    JsonParser.parse(in, useBigDecimalForDouble = useBigDecimalForDouble, useBigIntForLong = useBigIntForLong)

  override def parseOpt[A: AsJsonInput](
    in: A,
    useBigDecimalForDouble: Boolean = false,
    useBigIntForLong: Boolean = true
  ): Option[JValue] = {
    try {
      JsonParser
        .parse(
          s = AsJsonInput.asJsonInput(in).toReader(),
          useBigDecimalForDouble = useBigDecimalForDouble,
          useBigIntForLong = useBigIntForLong
        )
        .toOption
    } catch {
      case _: Exception =>
        None
    }
  }

  /**
   * Renders JSON.
   * @see Printer#compact
   * @see Printer#pretty
   */
  def render(
    value: JValue,
    alwaysEscapeUnicode: Boolean = false,
    emptyValueStrategy: EmptyValueStrategy = EmptyValueStrategy.default
  ): Document =
    emptyValueStrategy.replaceEmpty(value) match {
      case null => text("null")
      case JBool(true) => text("true")
      case JBool(false) => text("false")
      case JDouble(n) => text(StreamingJsonWriter.handleInfinity(n))
      case JDecimal(n) => text(n.toString)
      case JLong(n) => text(n.toString)
      case JInt(n) => text(n.toString)
      case JNull => text("null")
      case JNothing => sys.error("can't render 'nothing'")
      case JString(null) => text("null")
      case JString(s) => text("\"" + ParserUtil.quote(s, alwaysEscapeUnicode) + "\"")
      case JArray(arr) =>
        text("[") :: series(trimArr(arr).map(render(_, alwaysEscapeUnicode, emptyValueStrategy))) :: text("]")
      case JSet(set) =>
        text("[") :: series(trimArr(set).map(render(_, alwaysEscapeUnicode, emptyValueStrategy))) :: text("]")
      case JObject(obj) =>
        val nested = break :: fields(trimObj(obj).map({ case (n, v) =>
          text("\"" + ParserUtil.quote(n, alwaysEscapeUnicode) + "\":") :: render(
            v,
            alwaysEscapeUnicode,
            emptyValueStrategy
          )
        }))
        text("{") :: nest(2, nested) :: break :: text("}")
    }

  private def trimArr(xs: Iterable[JValue]) = xs.withFilter(_ != JNothing)
  private def trimObj(xs: List[JField]) = xs.filter(_._2 != JNothing)
  private def series(docs: Iterable[Document]) = punctuate(text(","), docs)
  private def fields(docs: List[Document]) = punctuate(text(",") :: break, docs)

  private def punctuate(p: Document, docs: Iterable[Document]): Document =
    if (docs.isEmpty) empty
    else docs.reduceLeft((d1, d2) => d1 :: p :: d2)

  def compact(d: Document): String = Printer.compact(d)
  def pretty(d: Document): String = Printer.pretty(d)

}

object JsonMethods extends native.JsonMethods
