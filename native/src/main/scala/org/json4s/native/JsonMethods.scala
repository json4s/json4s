package org.json4s
package native

import text.Document
import text.Document._
import io.Source

trait JsonMethods extends org.json4s.JsonMethods[Document] {

  def parse(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue = in match {
    case StringInput(s) => JsonParser.parse(s, useBigDecimalForDouble)
    case ReaderInput(rdr) => JsonParser.parse(rdr, useBigDecimalForDouble)
    case StreamInput(stream) => JsonParser.parse(Source.fromInputStream(stream).bufferedReader(), useBigDecimalForDouble)
    case FileInput(file) => JsonParser.parse(Source.fromFile(file).bufferedReader(), useBigDecimalForDouble)
  }

  def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] = in match {
    case StringInput(s) => JsonParser.parseOpt(s, useBigDecimalForDouble)
    case ReaderInput(rdr) => JsonParser.parseOpt(rdr, useBigDecimalForDouble)
    case StreamInput(stream) => JsonParser.parseOpt(Source.fromInputStream(stream).bufferedReader(), useBigDecimalForDouble)
    case FileInput(file) => JsonParser.parseOpt(Source.fromFile(file).bufferedReader(), useBigDecimalForDouble)
  }

  /** Renders JSON.
   * @see Printer#compact
   * @see Printer#pretty
   */
  def render(value: JValue)(implicit formats: Formats = DefaultFormats): Document =
    formats.emptyValueStrategy.replaceEmpty(value) match {
      case null          => text("null")
      case JBool(true)   => text("true")
      case JBool(false)  => text("false")
      case JDouble(n)    => text(n.toString)
      case JDecimal(n)   => text(n.toString)
      case JInt(n)       => text(n.toString)
      case JNull         => text("null")
      case JNothing      => sys.error("can't render 'nothing'")
      case JString(null) => text("null")
      case JString(s)    => text("\""+ParserUtil.quote(s)+"\"")
      case JArray(arr)   => text("[") :: series(trimArr(arr).map(render)) :: text("]")
      case JObject(obj)  =>
        val nested = break :: fields(trimObj(obj).map({case (n,v) => text("\""+ParserUtil.quote(n)+"\":") :: render(v)}))
        text("{") :: nest(2, nested) :: break :: text("}")
    }

  private def trimArr(xs: List[JValue]) = xs.filter(_ != JNothing)
  private def trimObj(xs: List[JField]) = xs.filter(_._2 != JNothing)
  private def series(docs: List[Document]) = punctuate(text(","), docs)
  private def fields(docs: List[Document]) = punctuate(text(",") :: break, docs)

  private def punctuate(p: Document, docs: List[Document]): Document =
    if (docs.length == 0) empty
    else docs.reduceLeft((d1, d2) => d1 :: p :: d2)


  def compact(d: Document): String = Printer.compact(d)
  def pretty(d: Document): String = Printer.pretty(d)


}

object JsonMethods extends native.JsonMethods
