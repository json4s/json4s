package org.json4s
package native

import text.Document

trait JsonMethods extends org.json4s.JsonMethods[Document] {

  def parse(s: String): JValue = JsonParser.parse(s)
  def parseOpt(s: String): Option[JValue] = JsonParser.parseOpt(s)

  def render(value: JValue): Document = JsonAST.render(value)
  def compact(d: Document): String = Printer.compact(d)
  def pretty(d: Document): String = Printer.pretty(d)


}

object JsonMethods extends JsonMethods
