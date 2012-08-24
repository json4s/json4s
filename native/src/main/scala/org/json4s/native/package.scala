package org.json4s

package object native {

  import java.io.Reader
  import scala.text.Document

  def parse(s: String): JValue = JsonParser.parse(s)
  def parseOpt(s: String): Option[JValue] = JsonParser.parseOpt(s)

  def render(value: JValue): Document = JsonAST.render(value)
  def compact(d: Document): String = Printer.compact(d)
  def pretty(d: Document): String = Printer.pretty(d)

  implicit def jvalue2jvalueWithExtraction(jv: JValue) = new JValueExt(jv)
}